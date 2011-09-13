package dan2097.org.bitbucket.reactionextraction;

import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.DT_THE;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.MOLECULE_Container;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.PERCENT_Container;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.UNNAMEDMOLECULE_Container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;
import com.google.common.collect.BiMap;

import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.IndigoHolder;
import dan2097.org.bitbucket.utility.Utils;

import nu.xom.Element;
import nu.xom.Nodes;

public class ExperimentalStepParser {
	private static Logger LOG = Logger.getLogger(ExperimentalStepParser.class);
	
	private final ExperimentalStep experimentalStep;
	private final BiMap<Element, Chemical> moleculeToChemicalMap;
	private final Chemical targetCompound;

	/*A yield phrase*/
	private static final String yieldPhraseProduct = "self::node()/descendant-or-self::ActionPhrase[@type='Yield']//*[self::MOLECULE or self::UNNAMEDMOLECULE]";
	/*A phrase (typically synthesize) containing the returned molecule near the beginning followed by something like "is/was synthesised"*/
	private static final String synthesizePhraseProduct = "self::node()/descendant-or-self::NounPhrase[following-sibling::*[1][local-name()='VerbPhrase'][VBD|VBP|VBZ][VB-SYNTHESIZE]]/*[self::MOLECULE or self::UNNAMEDMOLECULE]";
	
	static final Pattern matchProductTextualAnaphora = Pattern.compile("(crude|desired|title[d]?|final|aimed) (compound|product)", Pattern.CASE_INSENSITIVE);
	private static final Indigo indigo = IndigoHolder.getInstance();
	
	public ExperimentalStepParser(ExperimentalStep experimentalStep, BiMap<Element, Chemical> moleculeToChemicalMap, Chemical targetCompound) {
		this.experimentalStep = experimentalStep;
		this.moleculeToChemicalMap = moleculeToChemicalMap;
		this.targetCompound = targetCompound;
	}

	/**
	 * Master method for extracting reactions from a list of paragraphs
	 * @param paragraphs
	 * @return
	 */
	List<Reaction> extractReactions() {
		List<Reaction> reactions = new ArrayList<Reaction>();
		List<Paragraph> paragraphs = experimentalStep.getParagraphs();
		for (Paragraph paragraph : paragraphs) {
			Reaction currentReaction = new Reaction();
			paragraph.segmentIntoSections(moleculeToChemicalMap);
			Map<Element, PhraseType> phraseMap = paragraph.getPhraseMap();
			boolean reagentsExpectedAfterProduct = false;
			for (Entry<Element, PhraseType> entry: phraseMap.entrySet()) {
				Element phrase = entry.getKey();
				Reaction tempReaction = new Reaction();
				Set<Element> reagents;
				boolean inSynthesis = entry.getValue().equals(PhraseType.synthesis);
				if (inSynthesis){
					reagents = findAllReagents(phrase);
				}
				else{
					reagents = Collections.emptySet();
				}
				Set<Element> products = new LinkedHashSet<Element>();
				Set<Element> productAfterReagants = identifyYieldedProduct(phrase);
				productAfterReagants.addAll(reagentsWithAYield(reagents));
				if (productAfterReagants.size() >0 && currentReaction.getReactants().size()==0 && isBackReference(productAfterReagants)){
					productAfterReagants.clear();
				}
				if (productAfterReagants.size() >0 ){
					reagentsExpectedAfterProduct = false;
				}
				products.addAll(productAfterReagants);
				Set<Element> productBeforeReagents = identifyProductBeforeReagents(phrase);
				if (productBeforeReagents.size() >0 ){
					reagentsExpectedAfterProduct = true;
				}
				products.addAll(productBeforeReagents);
				reagents.removeAll(products);
				Set<Element> chemicals = new LinkedHashSet<Element>();
				chemicals.addAll(products);
				chemicals.addAll(reagents);
				resolveBackReferencesAndChangeRoleIfNecessary(chemicals, reactions);
				for (Element reagent : reagents) {
					ChemicalRoleAssigner.assignRoleToChemical(reagent, moleculeToChemicalMap.get(reagent));
				}
				for (Element chemical : chemicals) {
					Chemical chemChem = moleculeToChemicalMap.get(chemical);
					if (ChemicalRole.product.equals(chemChem.getRole())){
						interpretPercentAsAyield(chemical, chemChem);
						tempReaction.addProduct(chemChem);
					}
					else if (ChemicalRole.reactant.equals(chemChem.getRole())){
						tempReaction.addReactant(chemChem);
					}
					else if (ChemicalRole.solvent.equals(chemChem.getRole())
							|| ChemicalRole.catalyst.equals(chemChem.getRole())){
						tempReaction.addSpectator(chemChem);
					}
					else if (!ChemicalType.falsePositive.equals(chemChem.getType())){
						LOG.debug("Role not assigned to: " +chemChem.getName());
					}
				}
		
				currentReaction.importReaction(tempReaction);
				if (!currentReaction.getProducts().isEmpty() && !reagentsExpectedAfterProduct){
					currentReaction.setInput(paragraph);
					reactions.add(currentReaction);
					currentReaction = new Reaction();
				}
			}
			if (currentReaction.getProducts().size()>0 || currentReaction.getReactants().size()>0){
				currentReaction.setInput(paragraph);
				reactions.add(currentReaction);
			}
		}
		if (targetCompound !=null){
			if (!compoundIsProductOfAReaction(reactions, targetCompound)){
				if (!addTitleCompoundToLastReactionWithReactantsIfHasNoProduct(reactions)){
					LOG.trace("Failed to assign: " + targetCompound.getName() + " to a reaction!");
				}
			}
		}
		for (Reaction reaction : reactions) {
			ChemicalSenseApplication chemicalSenseApplication = new ChemicalSenseApplication(reaction);
			chemicalSenseApplication.mergeProductsByInChI();
			chemicalSenseApplication.reassignMisCategorisedReagents();
		}
		return reactions;
	}
	
	private Set<Element> findAllReagents(Element el) {
		List<Element> mols = XOMTools.getDescendantElementsWithTagNames(el, new String[]{MOLECULE_Container, UNNAMEDMOLECULE_Container});
		return new LinkedHashSet<Element>(mols);
	}
	
	/**
	 * Identifies products using yieldPhraseProduct
	 * @param phrase
	 * @return 
	 */
	private Set<Element> identifyYieldedProduct(Element phrase) {
		Set<Element> products = new LinkedHashSet<Element>();
		Nodes yieldPhraseMolecules = phrase.query(yieldPhraseProduct);
		boolean foundProductWithQuantity =false;
		for (int i = 0; i < yieldPhraseMolecules.size(); i++) {
			Element synthesizedMolecule= (Element) yieldPhraseMolecules.get(i);
			Chemical chem = moleculeToChemicalMap.get(synthesizedMolecule);
			if (chem.getType().equals(ChemicalType.falsePositive)){
				continue;
			}
			boolean hasQuantity = (chem.getAmountValue() !=null || chem.getEquivalents() !=null || chem.getMassValue() !=null || chem.getPercentYield() !=null);
			if ((foundProductWithQuantity && !hasQuantity) || isKnownSolvent(chem)){
				continue;//skip erroneous characterisation chemicals
			}
			if (hasQuantity){
				foundProductWithQuantity =true;
			}
			products.add(synthesizedMolecule);
			if (chem.getRole()==null){
				chem.setXpathUsedToIdentify(yieldPhraseProduct);
				chem.setRole(ChemicalRole.product);
			}
		}
		return products;
	}
	
	boolean isKnownSolvent(Chemical chem) {
		if (chem.getInchi() !=null && AprioriKnowledge.getInstance().isKnownSolventInChI(chem.getInchi())){
			return true;
		}
		return false;
	}

	/**
	 * Returns those for which the corresponding molecule has a percent yield
	 * @param reagents
	 * @return
	 */
	private Set<Element> reagentsWithAYield(Set<Element> reagents) {
		Set<Element> reagentsWithYield = new LinkedHashSet<Element>();
		for (Element reagent : reagents) {
			Chemical chem = moleculeToChemicalMap.get(reagent);
			if (moleculeToChemicalMap.get(reagent).getPercentYield()!=null){
				reagentsWithYield.add(reagent);
				chem.setXpathUsedToIdentify("//YIELD");
				chem.setRole(ChemicalRole.product);
			}
		}
		return reagentsWithYield;
	}

	private boolean isBackReference(Set<Element> yieldedCompounds) {
		if (yieldedCompounds.size()==1){
			Chemical cm = moleculeToChemicalMap.get(yieldedCompounds.iterator().next());
			if (cm.getType().equals(ChemicalType.definiteReference)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Identifies products using synthesizePhraseProduct
	 * @param phrase
	 * @return 
	 */
	private Set<Element> identifyProductBeforeReagents(Element phrase) {
		Set<Element> products = new LinkedHashSet<Element>();
		Nodes synthesizePhraseMolecules = phrase.query(synthesizePhraseProduct);
		for (int i = 0; i < synthesizePhraseMolecules.size(); i++) {
			Element synthesizedMolecule= (Element) synthesizePhraseMolecules.get(i);
			Chemical chem = moleculeToChemicalMap.get(synthesizedMolecule);
			if (chem.getType().equals(ChemicalType.falsePositive) || isKnownSolvent(chem)){
				continue;
			}
			products.add(synthesizedMolecule);
			if (chem.getRole()==null){
				chem.setXpathUsedToIdentify(synthesizePhraseProduct);
				chem.setRole(ChemicalRole.product);
			}
		}
		return products;
	}
	
	private void resolveBackReferencesAndChangeRoleIfNecessary(Set<Element> chemicals, List<Reaction> reactions) {
		for (Element chemical : chemicals) {
			Chemical chemChem = moleculeToChemicalMap.get(chemical);
			if (chemChem.getType() ==ChemicalType.definiteReference){
				attemptToResolveBackReference(chemChem, reactions);
			}
		}
	}

	boolean attemptToResolveBackReference(Chemical chemical, List<Reaction> reactionsToConsider) {
		if (matchProductTextualAnaphora.matcher(chemical.getName()).matches()){
			if (targetCompound!=null){
				chemical.setSmiles(targetCompound.getSmiles());
				chemical.setInchi(targetCompound.getInchi());
			}
			chemical.setRole(ChemicalRole.product);
			return true;
		}
		if (chemical.getSmarts()!=null){
			List<Chemical> chemicalsToMatchAgainst = getProductChemsFromReactions(reactionsToConsider);
			boolean success = attemptToResolveViaSmartsMatch(chemical.getSmarts(), chemical, chemicalsToMatchAgainst);
			if (success){
				chemical.setRole(ChemicalRole.reactant);
				return true;
			}
			if (FunctionalGroupDefinitions.functionalGroupToSmartsMap.containsKey(chemical.getName().toLowerCase())){
				//The <name of compound> could also be specific
				Element molecule = moleculeToChemicalMap.inverse().get(chemical);
				Element previous = Utils.getPreviousElement(molecule);
				if (previous !=null && previous.getLocalName().equals(DT_THE)){
					chemical.setType(ChemicalType.exact);
					return true;
				}
			}
		}
		else if (chemical.getSmiles()!=null){
			List<Chemical> chemicalsToMatchAgainst = getProductChemsFromReactions(reactionsToConsider);
			if (ChemicalRole.product.equals(chemical.getRole()) && targetCompound !=null){
				chemicalsToMatchAgainst.add(targetCompound);
			}
			String smarts = generateAromaticSmiles(chemical.getSmiles());
			boolean success = attemptToResolveViaSmartsMatch(smarts, chemical, chemicalsToMatchAgainst);
			if (success){
				if (!ChemicalRole.product.equals(chemical.getRole())){
					chemical.setRole(ChemicalRole.reactant);
				}
				return true;
			}
			//The <name of compound> could also be specific
			Element molecule = moleculeToChemicalMap.inverse().get(chemical);
			Element previous = Utils.getPreviousElement(molecule);
			if (previous !=null && previous.getLocalName().equals(DT_THE)){
				chemical.setType(ChemicalType.exact);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * If the chemical does not currently have a yield look for a percent that could be a yield and apply it the chemical
	 * @param molOrUnnamedMolEl
	 * @param chemical
	 */
	static void interpretPercentAsAyield(Element molOrUnnamedMolEl, Chemical chemical) {
		if (chemical.getPercentYield()==null){
			List<Element> percents = XOMTools.getDescendantElementsWithTagName(molOrUnnamedMolEl, PERCENT_Container);
			if (percents.size() ==1){
				String value = percents.get(0).getFirstChildElement(ChemicalTaggerTags.CD).getValue();
				try{ 
					double d = Double.parseDouble(value);
					chemical.setPercentYield(d);
				}
				catch (NumberFormatException e) {
					LOG.debug("Percent was not a numeric percentage");
				}
			}
		}
	}
	
	private List<Chemical> getProductChemsFromReactions(List<Reaction> reactionsToConsider) {
		List<Chemical> products = new ArrayList<Chemical>();
		for (Reaction reaction : reactionsToConsider) {
			products.addAll(reaction.getProducts());
		}
		return products;
	}
	
	private String generateAromaticSmiles(String smiles) {
		IndigoObject chem = indigo.loadMolecule(smiles);
		chem.aromatize();
		return chem.smiles();
	}
	
	private boolean attemptToResolveViaSmartsMatch(String smarts, Chemical backReference, List<Chemical> chemicalsToMatchAgainst) {
		IndigoObject query = indigo.loadSmarts(smarts);
		List<Chemical> chemicalMatches = new ArrayList<Chemical>();
		for (Chemical chemical : chemicalsToMatchAgainst) {
			if (chemical.getSmiles()!=null){
				IndigoObject substructureMatcher = indigo.substructureMatcher(indigo.loadMolecule(chemical.getSmiles()));
				if (substructureMatcher.match(query) !=null){
					chemicalMatches.add(chemical);
				}
			}
		}
		if (chemicalMatches.size()==1){
			Chemical anaphoraChem = chemicalMatches.get(0);
			backReference.setSmiles(anaphoraChem.getSmiles());
			backReference.setInchi(anaphoraChem.getInchi());
			return true;
		}
		return false;
	}
	
	/**
	 * Uses InChIs to check whether the compound has been the product of any of the given reactions
	 * @param reactions
	 * @param compound
	 * @return
	 */
	private boolean compoundIsProductOfAReaction(List<Reaction> reactions, Chemical potentialProduct) {
		String inchi = potentialProduct.getInchi();
		if (inchi==null){
			return false;
		}
		for (Reaction reaction : reactions) {
			for (Chemical product : reaction.getProducts()) {
				if (inchi.equals(product.getInchi())){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true or false depending on whether a suitable reaction was found to add the title compound to
	 * @param reactions
	 * @return
	 */
	private boolean addTitleCompoundToLastReactionWithReactantsIfHasNoProduct(List<Reaction> reactions) {
		for (int i = reactions.size()-1; i >=0; i--) {
			Reaction reaction = reactions.get(i);
			if (reaction.getReactants().size()>0){
				boolean hasReactantWithSmiles =false;
				for (Chemical reactant : reaction.getReactants()) {
					if (reactant.getSmiles()!=null){
						hasReactantWithSmiles =true;
						break;
					}
				}
				if (!hasReactantWithSmiles){
					continue;
				}
				if (reaction.getProducts().size()==0){
					reaction.addProduct(targetCompound);
					return true;
				}
				else if (productCouldBeTheTitleCompound(reaction)){
					Chemical product = reaction.getProducts().get(0);
					product.setSmiles(targetCompound.getSmiles());
					product.setInchi(targetCompound.getInchi());
					return true;
				}
				else{
					return false;
				}
			}
		}
		return false;
	}

	private boolean productCouldBeTheTitleCompound(Reaction reaction) {
		if(reaction.getProducts().size()==1){
			Chemical product = reaction.getProducts().get(0);
			if (product.getSmiles()==null){
				Element el = moleculeToChemicalMap.inverse().get(product);
				if (el.getLocalName().equals(UNNAMEDMOLECULE_Container)){
					List<Element> references = XOMTools.getDescendantElementsWithTagName(el, ChemicalTaggerTags.REFERENCETOCOMPOUND_Container);
					if (references.size()==0){
						return true;
					}
				}
			}
			else if (product.getType().equals(ChemicalType.chemicalClass)){
				return true;
			}
		}
		return false;
	}
}

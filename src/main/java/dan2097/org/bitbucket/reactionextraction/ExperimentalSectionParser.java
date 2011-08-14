package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.IndigoHolder;
import dan2097.org.bitbucket.utility.Utils;

public class ExperimentalSectionParser {
	private static Logger LOG = Logger.getLogger(ExperimentalSectionParser.class);
	private final ExperimentalSection experimentalSection;
	private final BiMap<Element, Chemical>  moleculeToChemicalMap = HashBiMap.create();
	private final Map<String, Chemical> aliasToChemicalMap;
	private Chemical currentTargetCompound;
	private Chemical ultimateTargetCompound;
	private final List<Reaction> sectionReactions = new ArrayList<Reaction>();
	/*A yield phrase*/
	private final String yieldPhraseProduct = "self::node()/descendant-or-self::ActionPhrase[@type='Yield']//*[self::MOLECULE or self::UNNAMEDMOLECULE]";
	/*A phrase (typically synthesize) containing the returned molecule near the beginning followed by something like "is/was synthesised"*/
	private final String synthesizePhraseProduct = "self::node()/descendant-or-self::NounPhrase[following-sibling::*[1][local-name()='VerbPhrase'][VBD|VBP|VBZ][VB-SYNTHESIZE]]/*[self::MOLECULE or self::UNNAMEDMOLECULE]";
	private final Indigo indigo = IndigoHolder.getInstance();
	static final Pattern matchProductTextualAnaphora = Pattern.compile("(crude|desired|title|final) (compound|product)", Pattern.CASE_INSENSITIVE);

	public ExperimentalSectionParser(ExperimentalSection experimentalSection, Map<String, Chemical> aliasToChemicalMap) {
		if (experimentalSection ==null || aliasToChemicalMap==null){
			throw new IllegalArgumentException("Null input parameter");
		}
		this.experimentalSection = experimentalSection;
		this.aliasToChemicalMap = aliasToChemicalMap;
	}

	public void parseForReactions(){
		if (experimentalSection.getTargetChemicalName()!=null){
			ultimateTargetCompound = Utils.createChemicalFromName(experimentalSection.getTargetChemicalName());
		}
		for (ExperimentalStep step : experimentalSection.getExperimentalSteps()) {
			if (step.getTargetChemicalName()!=null){
				currentTargetCompound = Utils.createChemicalFromName(step.getTargetChemicalName());
			}
			else{
				currentTargetCompound = ultimateTargetCompound;
			}
			for (Paragraph paragraph : step.getParagraphs()) {
				List<Element> moleculeEls = findAllMolecules(paragraph);
				for (Element moleculeEl : moleculeEls) {
					Chemical cm = generateChemicalFromMoleculeElAndLocalInformation(moleculeEl);
					moleculeToChemicalMap.put(moleculeEl, cm);
					ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, cm);
					attemptToResolveAnaphora(moleculeEl, cm);
					aliasToChemicalMap.putAll(findAliasDefinitions(moleculeEl, cm.getType()));
				}
				List<Element> unnamedMoleculeEls = findAllUnnamedMolecules(paragraph);
				for (Element unnamedMoleculeEl : unnamedMoleculeEls) {
					Chemical cm = generateChemicalFromMoleculeElAndLocalInformation(unnamedMoleculeEl);
					moleculeToChemicalMap.put(unnamedMoleculeEl, cm);
					attemptToResolveAnaphora(unnamedMoleculeEl, cm);
					ChemicalTypeAssigner.assignTypeToChemical(unnamedMoleculeEl, cm);
				}
			}
			List<Reaction> reactions = determineReactions(step.getParagraphs());
			for (Reaction reaction : reactions) {
				new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
				sectionReactions.add(reaction);
			}
		}
	}

	private void attemptToResolveAnaphora(Element molOrUnnamedEl, Chemical cm) {
		List<Element> references = XOMTools.getDescendantElementsWithTagName(molOrUnnamedEl, ChemicalTaggerTags.REFERENCETOCOMPOUND_Container);
		if (references.size()!=1){
			if (references.size() >0){
				LOG.debug("Multiple referenceToCompounds present in : " +molOrUnnamedEl.toXML());
			}
			return;
		}
		String identifier = getIdentifierFromReference(references.get(0));
		Chemical referencedChemical = aliasToChemicalMap.get(identifier);
		if (referencedChemical !=null){
			cm.setSmiles(referencedChemical.getSmiles());
			cm.setInchi(referencedChemical.getInchi());
		}
		else{
			LOG.trace("Failed to resolve anaphora: " + identifier );
		}
	}

	/**
	 * Extracts the textual identifier from a referenceEl
	 * @param referenceEl
	 * @return
	 */
	String getIdentifierFromReference(Element referenceEl) {
		List<Element> identifierEls = XOMTools.getChildElementsWithTagNames(referenceEl, new String[]{CD, CD_ALPHANUM, NN_IDENTIFIER});
		StringBuilder sb = new StringBuilder();
		for (Element identifierEl : identifierEls) {
			if (sb.length()!=0){
				sb.append(' ');
			}
			sb.append(identifierEl.getValue());
		}
		return sb.toString();
	}

	/**
	 * Retrieves the reactions found by this experimental section parser
	 * @return
	 */
	public List<Reaction> getSectionReactions() {
		return sectionReactions;
	}

	/**
	 * Finds chemical name or identifier to chemical relationships
	 * @param moleculeEl
	 * @param type
	 * @return
	 */
	Map<String, Chemical> findAliasDefinitions(Element moleculeEl, ChemicalType type) {
		Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();
		if (type!=ChemicalType.exact && type!=ChemicalType.definiteReference){
			return aliasToChemicalMap;
		}
		aliasToChemicalMap.putAll(extractSynonymousChemicalNameAliases(moleculeEl));
		List<Element> references = XOMTools.getDescendantElementsWithTagName(moleculeEl, ChemicalTaggerTags.REFERENCETOCOMPOUND_Container);
		if (references.size()==1){
			String identifier = getIdentifierFromReference(references.get(0));
			aliasToChemicalMap.put(identifier, moleculeToChemicalMap.get(moleculeEl));
		}
		else if (references.size() >1){
			LOG.debug("Multiple referenceToCompounds present in : " +moleculeEl.toXML());
		}
		return aliasToChemicalMap;
	}

	/**
	 * Determines where the moleculeEl contains two OSCAR-CM parents, one of which has no resolvable structure
	 * Such cases are assumed to be defining aliases.
	 * The returned map will typically be of size 0 or 1
	 * @param moleculeEl
	 * @return
	 */
	private Map<String, Chemical> extractSynonymousChemicalNameAliases(Element moleculeEl) {
		Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();
		List<Element> oscarCMsAndMixtures = XOMTools.getChildElementsWithTagNames(moleculeEl, new String[]{OSCARCM_Container, MIXTURE_Container});
		if (oscarCMsAndMixtures.size()==2 && oscarCMsAndMixtures.get(0).getLocalName().equals(OSCARCM_Container)){
			//typically only the first OscarCm is ever used. This method deals with the case where the second oscarCm is a synonym
			Element firstOscarcm = oscarCMsAndMixtures.get(0);
			Element secondOscarcm;
			if(oscarCMsAndMixtures.get(1).getLocalName().equals(MIXTURE_Container)){
				secondOscarcm = findSynonymnOscarCmFromMixture(oscarCMsAndMixtures.get(1));
				if (secondOscarcm == null){
					return aliasToChemicalMap;
				}
			}
			else{
				secondOscarcm = oscarCMsAndMixtures.get(1);
				if (!oscarCMisBracketted(secondOscarcm)){
					return aliasToChemicalMap;
				}
			}
			String name1 = ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(firstOscarcm);
			String smiles1 = Utils.resolveNameToSmiles(name1);
			String name2 = ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(secondOscarcm);
			String smiles2 =Utils.resolveNameToSmiles(name2);

			if (smiles1 !=null && smiles2 ==null){
				Chemical cm = new Chemical(name2);
				cm.setSmiles(smiles1);
				cm.setInchi(Utils.resolveNameToSmiles(name1));
				aliasToChemicalMap.put(name2, cm);
				LOG.trace(name1 +" is the same as " + name2 +" " +moleculeEl.getParent().toXML());
			}
			else if (smiles1 ==null && smiles2 !=null){
				Chemical cm = new Chemical(name1);
				cm.setSmiles(smiles2);
				cm.setInchi(Utils.resolveNameToSmiles(name2));
				aliasToChemicalMap.put(name1, cm);
				LOG.trace(name1 +" is the same as " + name2 +" " +moleculeEl.getParent().toXML());
			}
		}
		return aliasToChemicalMap;
	}

	private boolean oscarCMisBracketted(Element oscarcm) {
		Elements children = oscarcm.getChildElements();
		if (children.size() >=3 &&
				children.get(0).getLocalName().equals(LRB) &&
				children.get(children.size()-1).getLocalName().equals(RRB)){
			return true;
		}
		return false;
	}

	private Element findSynonymnOscarCmFromMixture(Element oscarCmOrMixture) {
		if(oscarCmOrMixture.getLocalName().equals(OSCARCM_Container)){
			return oscarCmOrMixture;
		}
		Element lrb = oscarCmOrMixture.getFirstChildElement(LRB);
		if (lrb !=null){
			Element oscarcm = (Element) XOMTools.getNextSibling(lrb);
			if (oscarcm !=null && oscarcm.getLocalName().equals(OSCARCM_Container)){
				Element delimiter = (Element) XOMTools.getNextSibling(oscarcm);
				if (delimiter !=null && (delimiter.getLocalName().equals(COMMA) || delimiter.getLocalName().equals(COLON))){
					return oscarcm;
				}
			}
		}
		return null;
	}

	private Chemical generateChemicalFromMoleculeElAndLocalInformation(Element moleculeEl) {
		String name = ChemTaggerOutputNameExtraction.findMoleculeName(moleculeEl);
		Chemical chem = Utils.createChemicalFromName(name);
		Chemical referencedChemical = aliasToChemicalMap.get(name);
		if (referencedChemical != null){
			chem.setSmiles(referencedChemical.getSmiles());
			chem.setInchi(referencedChemical.getInchi());
		}
		String smarts = FunctionalGroupDefinitions.getSmartsFromChemicalName(name);
		chem.setSmarts(smarts);
		if (smarts !=null && FunctionalGroupDefinitions.functionalClassToSmartsMap.containsKey(name)){
			chem.setSmiles(null);
			chem.setInchi(null);
		}
		ChemicalPropertyDetermination.determineProperties(chem, moleculeEl);
		return chem;
	}

	/**
	 * Creates a list of all MOLECULE elements in the given paragraph
	 * @return
	 */
	private List<Element> findAllMolecules(Paragraph paragraph) {
		Document chemicalTaggerResult =paragraph.getTaggedSentencesDocument();
		return XOMTools.getDescendantElementsWithTagName(chemicalTaggerResult.getRootElement(), MOLECULE_Container);
	}

	/**
	 * Creates a list of all UNNAMEDMOLECULE elements in the given paragraph
	 * @return
	 */
	private List<Element> findAllUnnamedMolecules(Paragraph paragraph) {
		Document chemicalTaggerResult =paragraph.getTaggedSentencesDocument();
		return XOMTools.getDescendantElementsWithTagName(chemicalTaggerResult.getRootElement(), UNNAMEDMOLECULE_Container);
	}

	/**
	 * Master method for extracting reactions from a list of paragraphs
	 * @param paragraphs
	 * @return
	 */
	private List<Reaction> determineReactions(List<Paragraph> paragraphs) {
		List<Reaction> reactions = new ArrayList<Reaction>();
		for (Paragraph paragraph : paragraphs) {
			Reaction currentReaction = new Reaction();
			paragraph.segmentIntoSections(moleculeToChemicalMap);
			Map<Element, PhraseType> phraseMap = paragraph.getPhraseMap();
			boolean reagentsExpectedAfterProduct = false;
			for (Element phrase : phraseMap.keySet()) {
				Reaction tempReaction = new Reaction();
				Set<Element> reagents;
				boolean inSynthesis = phraseMap.get(phrase).equals(PhraseType.synthesis);
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
		if (currentTargetCompound !=null){
			if (!compoundIsProductOfAReaction(reactions, currentTargetCompound)){
				if (!addTitleCompoundToLastReactionWithReactantsIfHasNoProduct(reactions)){
					LOG.trace("Failed to assign: " + currentTargetCompound.getName() + " to a reaction!");
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

	/**
	 * If the chemical does not currently have a yield look for a percent that could be a yield
	 * @param molOrUnnamedMolEl
	 * @param chemical
	 */
	void interpretPercentAsAyield(Element molOrUnnamedMolEl, Chemical chemical) {
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

	private boolean isBackReference(Set<Element> yieldedCompounds) {
		if (yieldedCompounds.size()==1){
			Chemical cm = moleculeToChemicalMap.get(yieldedCompounds.iterator().next());
			if (cm.getType().equals(ChemicalType.definiteReference)){
				return true;
			}
		}
		return false;
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
			if (foundProductWithQuantity && !hasQuantity){
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
			if (chem.getType().equals(ChemicalType.falsePositive)){
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
			if (currentTargetCompound!=null){
				chemical.setSmiles(currentTargetCompound.getSmiles());
				chemical.setInchi(currentTargetCompound.getInchi());
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
			if (ChemicalRole.product.equals(chemical.getRole()) && currentTargetCompound !=null){
				chemicalsToMatchAgainst.add(currentTargetCompound);
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
				if (reaction.getProducts().size()==0){
					reaction.addProduct(currentTargetCompound);
					return true;
				}
				else if (productCouldBeTheTitleCompound(reaction)){
					Chemical product = reaction.getProducts().get(0);
					product.setSmiles(currentTargetCompound.getSmiles());
					product.setInchi(currentTargetCompound.getInchi());
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

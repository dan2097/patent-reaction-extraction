package dan2097.org.bitbucket.reactionextraction;

import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

public class ExperimentalStepParser {
	private static Logger LOG = Logger.getLogger(ExperimentalStepParser.class);
	
	private static Pattern matchFirstChemicalToBeReplacedBefore = Pattern.compile("((replac|substitut)[e]?ing( of)?|(substitution|replacement) of)( the)?$", Pattern.CASE_INSENSITIVE);
	private static Pattern matchFirstChemicalToBeReplacedInbetween = Pattern.compile("for|with|by", Pattern.CASE_INSENSITIVE);
	private static Pattern matchSecondChemicalToBeReplacedInbetween = Pattern.compile("((((is |was )?(used|employed) )?((to )?take the place of|in (the )?place of|instead of))|(replac|substitut)[e]?ing)( the)?", Pattern.CASE_INSENSITIVE);

	private final ExperimentalStep experimentalStep;
	private final BiMap<Element, Chemical> moleculeToChemicalMap;
	private final Chemical targetCompound;
	/**Typically title and target compound will be identical but only the target compound can be implicitly added to a reaction
	 * Due to confusion in the experimental section creator the title compound may be mentioned before the final step*/
	private final Chemical titleCompound;

	/*A yield phrase*/
	private static final String yieldPhraseProduct = "self::node()/descendant-or-self::ActionPhrase[@type='Yield']//*[self::MOLECULE or self::UNNAMEDMOLECULE]";

	static final Pattern matchProductTextualAnaphora = Pattern.compile("(crude|desired|title[d]?|final|aimed|expected|anticipated) (compound|product)", Pattern.CASE_INSENSITIVE);
	private static final Indigo indigo = IndigoHolder.getInstance();
	
	public ExperimentalStepParser(ExperimentalStep experimentalStep, BiMap<Element, Chemical> moleculeToChemicalMap, Chemical targetCompound, Chemical titleCompound) {
		this.experimentalStep = experimentalStep;
		this.moleculeToChemicalMap = moleculeToChemicalMap;
		this.targetCompound = targetCompound;
		this.titleCompound = titleCompound;
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
			markReplacedChemicalsAsFalsePositives(paragraph.getTaggedSentencesDocument());
			Reaction currentReaction = new Reaction();
			Map<Element, PhraseType> phraseMap = paragraph.generatePhraseToTypeMapping(moleculeToChemicalMap);
			boolean reagentsExpectedAfterProduct = false;
			for (Entry<Element, PhraseType> entry: phraseMap.entrySet()) {
				Element phrase = entry.getKey();
				Reaction tempReaction = new Reaction();
				boolean inSynthesis = entry.getValue().equals(PhraseType.synthesis);
				Set<Element> reagents = inSynthesis ? findAllReagents(phrase) : Collections.<Element> emptySet();
				Set<Element> products = new LinkedHashSet<Element>();
				Set<Element> productAfterReagents = identifyYieldedProduct(phrase);
				productAfterReagents.addAll(reagentsWithAYield(reagents));
				if (currentReaction.getReactants().size()==0 ){
					//A reaction with no reagents and a backreferenced "product" probably means its the product of a previous reaction
					removeBackReferencedCompoundFromProductListIfPresent(productAfterReagents);
				}
				if (productAfterReagents.size() >0 ){
					reagentsExpectedAfterProduct = false;
				}
				products.addAll(productAfterReagents);
				Set<Element> productBeforeReagents = identifyProductBeforeReagents(phrase);
				if (productBeforeReagents.size() >0 ){
					reagentsExpectedAfterProduct = true;
				}
				products.addAll(productBeforeReagents);
				reagents.removeAll(products);
				Set<Element> chemicals = new LinkedHashSet<Element>();
				chemicals.addAll(products);
				chemicals.addAll(reagents);
				resolveLocalBackReferencesAndChangeRoleIfNecessary(chemicals, reactions);
				for (Element reagent : reagents) {
					Chemical reagentChem = moleculeToChemicalMap.get(reagent);
					if (reagentChem.getEntityType()== ChemicalType.falsePositive){
						LOG.trace(reagentChem.getName() +" is believed to be a false positive and has been ignored");
					}
					else if (reagentChem.getRole() ==null){//only assign a role if one has not already been explicitly assigned
						reagentChem.setRole(ChemicalRoleAssigner.determineChemicalRole(reagent, reagentChem));
					}
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
					else if (!ChemicalType.falsePositive.equals(chemChem.getEntityType())){
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
				if (!addTargetCompoundToLastReactionWithReactantsIfHasNoProduct(reactions)){
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
	
	void markReplacedChemicalsAsFalsePositives(Document taggedParagraph) {
		List<Element> moleculesToIgnore = new ArrayList<Element>();
		LinkedList<Element> stack = new LinkedList<Element>();
		stack.add(taggedParagraph.getRootElement());
		StringBuilder sb = new StringBuilder();
		Element tempMoleculeThatWasReplaced = null;
		boolean seenMolecule = false;
		while (stack.size()>0){
			Element currentElement =stack.removeLast();
			Elements children =currentElement.getChildElements();
			if (children.size()==0){
				if (sb.length()!=0){
					sb.append(' ');
				}
				sb.append(currentElement.getValue());
			}
			if (currentElement.getLocalName().equals(MOLECULE_Container) || currentElement.getLocalName().equals(UNNAMEDMOLECULE_Container)){
				String beforeMoleculeString = sb.toString();
				if (seenMolecule && matchSecondChemicalToBeReplacedInbetween.matcher(beforeMoleculeString).matches()){
					moleculesToIgnore.add(currentElement);
				}
				else if (tempMoleculeThatWasReplaced ==null){
					if (matchFirstChemicalToBeReplacedBefore.matcher(beforeMoleculeString).find()){
						tempMoleculeThatWasReplaced = currentElement;
					}
				}
				else if (matchFirstChemicalToBeReplacedInbetween.matcher(beforeMoleculeString).matches()){
					moleculesToIgnore.add(tempMoleculeThatWasReplaced);
					tempMoleculeThatWasReplaced = null;
				}
				sb = new StringBuilder();
				seenMolecule =true;
			}
			else{
				for (int i = children.size()-1; i >=0; i--) {
					stack.add(children.get(i));
				}
			}
		}
		for (Element molecule : moleculesToIgnore) {
			moleculeToChemicalMap.get(molecule).setEntityType(ChemicalType.falsePositive);
		}
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
			if (chem.getEntityType().equals(ChemicalType.falsePositive)){
				continue;
			}
			boolean hasQuantity = (chem.getAmountValue() !=null || chem.getEquivalents() !=null || chem.getMassValue() !=null || chem.getPercentYield() !=null);
			ChemicalRole believedRole = ChemicalRoleAssigner.determineChemicalRole(synthesizedMolecule, chem);
			if ((foundProductWithQuantity && !hasQuantity) || ReactionExtractionMethods.isKnownSolvent(chem) || believedRole.equals(ChemicalRole.solvent) || believedRole.equals(ChemicalRole.catalyst)){
				continue;//skip erroneous characterisation chemicals
			}
			if (hasQuantity){
				foundProductWithQuantity =true;
			}
			products.add(synthesizedMolecule);
			chem.setRole(ChemicalRole.product);
		}
		return products;
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
				chem.setRole(ChemicalRole.product);
			}
		}
		return reagentsWithYield;
	}

	private void removeBackReferencedCompoundFromProductListIfPresent(Set<Element> yieldedCompounds) {
		if (yieldedCompounds.size()==1){
			Chemical cm = moleculeToChemicalMap.get(yieldedCompounds.iterator().next());
			if (cm.getEntityType().equals(ChemicalType.definiteReference)){
				cm.setRole(null);
				yieldedCompounds.clear();
			}
		}
	}
	
	/**
	 * Identifies products which are indicated as having being synthesized/prepared
	 * @param phrase
	 * @return 
	 */
	private Set<Element> identifyProductBeforeReagents(Element phrase) {
		Set<Element> products = new LinkedHashSet<Element>();
		List<Element> synthesizePhraseProductMolecules = identifySynthesizedProductMolecules(phrase);
		for (Element synthesizedMolecule : synthesizePhraseProductMolecules) {
			Chemical chem = moleculeToChemicalMap.get(synthesizedMolecule);
			ChemicalRole believedRole = ChemicalRoleAssigner.determineChemicalRole(synthesizedMolecule, chem);
			if (chem.getEntityType().equals(ChemicalType.falsePositive) || ReactionExtractionMethods.isKnownSolvent(chem) || believedRole.equals(ChemicalRole.solvent) || believedRole.equals(ChemicalRole.catalyst)){
				continue;
			}
			products.add(synthesizedMolecule);
			if (chem.getRole()==null){
				chem.setRole(ChemicalRole.product);
			}
		}
		return products;
	}
	
	/**
	 * Finds molecules in a phrase that have been synthesized
	 * e.g. Acetic anhydride (was/is) (synthesized/prepared) from methyl acetate and carbon monoxid
	 * @param phrase
	 * @return
	 */
	private List<Element> identifySynthesizedProductMolecules(Element phrase) {
		List<Element> nounPhrases = new ArrayList<Element>();
		if (phrase.getLocalName().equals(NOUN_PHRASE_Container)){
			nounPhrases.add(phrase);
		}
		nounPhrases.addAll(XOMTools.getDescendantElementsWithTagName(phrase, NOUN_PHRASE_Container));
		for (Element nounPhrase : nounPhrases) {
			Element adjacentVerbPhrase = (Element) XOMTools.getNextSibling(nounPhrase);
			if (adjacentVerbPhrase !=null && adjacentVerbPhrase.getLocalName().equals(VERBPHRASE_Container)){
				List<Element> verbs = XOMTools.getChildElementsWithTagNames(adjacentVerbPhrase, new String[]{VBD, VBP, VBZ});
				for (Element verb  : verbs) {
					Element synthesizeVerb = (Element) XOMTools.getNextSibling(verb);
					if (synthesizeVerb !=null && synthesizeVerb.getLocalName().equals(VB_SYNTHESIZE) && !synthesizeVerb.getValue().toLowerCase().startsWith("react")){
						return XOMTools.getDescendantElementsWithTagNames(nounPhrase, new String[]{MOLECULE_Container, UNNAMEDMOLECULE_Container});
					}
				}
			}
		}
		return new ArrayList<Element>();
	}

	private void resolveLocalBackReferencesAndChangeRoleIfNecessary(Set<Element> chemicals, List<Reaction> reactions) {
		for (Element chemical : chemicals) {
			Chemical chemChem = moleculeToChemicalMap.get(chemical);
			if (chemChem.getEntityType() ==ChemicalType.definiteReference){
				if (chemChem.getSmiles() ==null && matchProductTextualAnaphora.matcher(chemChem.getName()).matches()){
					if (titleCompound!=null){
						chemChem.setChemicalIdentifierPair(titleCompound.getChemicalIdentifierPair());
					}
					chemChem.setRole(ChemicalRole.product);
				}
				else if (XOMTools.getDescendantElementsWithTagNames(chemical, new String[]{REFERENCETOCOMPOUND_Container, PROCEDURE_Container}).size()==0){
					//back referencing will not been attempted previously
					String smarts = chemChem.getSmarts();
					if (smarts == null && chemChem.getSmiles()!=null){
						smarts = generateAromaticSmiles(chemChem.getSmiles());
					}
					if (smarts != null){
						List<Chemical> chemicalsToMatchAgainst = getProductChemsFromReactions(reactions);
						ChemicalRole role = chemChem.getRole();
						if (ChemicalRole.product == role && targetCompound !=null){
							chemicalsToMatchAgainst.add(targetCompound);
						}
						List<Chemical> matches = findMatchesUsingSmarts(smarts, chemicalsToMatchAgainst);
						if (matches.size()==1){
							Chemical referencedChem = matches.get(0);
							if (referencedChem.getInchi()==null || !referencedChem.getInchi().equals(chemChem.getInchi())){
								chemChem.setChemicalIdentifierPair(referencedChem.getChemicalIdentifierPair());
								if (ChemicalRole.product != role){
									chemChem.setRole(ChemicalRole.reactant);
								}
							}
							else{
								//If it resolves to exactly the same compound its not really a proper reference
								chemChem.setEntityType(ChemicalType.exact);
							}
							continue;
						}
					}
					//The  compound could also be specific
					Element previous = Utils.getPreviousElement(chemical);
					if (previous !=null && previous.getLocalName().equals(DT_THE)){
						if (chemChem.getSmarts() !=null){
							if (FunctionalGroupDefinitions.functionalGroupToSmartsMap.containsKey(chemChem.getName().toLowerCase())){
								chemChem.setEntityType(ChemicalType.exact);
							}
						}
						else if (chemChem.getSmiles() !=null){
							chemChem.setEntityType(ChemicalType.exact);
						}
					}
				}
			}
		}
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
	
	/**
	 * Matches the given smarts against the given chemicals returning those chemicals that match
	 * @param smarts
	 * @param chemicalsToMatchAgainst
	 * @return
	 */
	private List<Chemical> findMatchesUsingSmarts(String smarts, List<Chemical> chemicalsToMatchAgainst) {
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
		return chemicalMatches;
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
	 * Returns true or false depending on whether a suitable reaction was found to add the target compound to
	 * @param reactions
	 * @return
	 */
	private boolean addTargetCompoundToLastReactionWithReactantsIfHasNoProduct(List<Reaction> reactions) {
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
					product.setChemicalIdentifierPair(targetCompound.getChemicalIdentifierPair());
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
			else if (product.getEntityType().equals(ChemicalType.chemicalClass)){
				return true;
			}
		}
		return false;
	}
}

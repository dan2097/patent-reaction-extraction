package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ggasoftware.indigo.IndigoObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import dan2097.org.bitbucket.utility.InchiNormaliser;
import dan2097.org.bitbucket.utility.Utils;

import static dan2097.org.bitbucket.utility.Utils.*;

public class ExperimentalSectionParser {
	private static Logger LOG = Logger.getLogger(ExperimentalSectionParser.class);
	private final BiMap<Element, Chemical>  moleculeToChemicalMap = HashBiMap.create();
	private final Map<String, Chemical> aliasToChemicalMap;
	private final Chemical titleCompound;
	private final List<Element> paragraphEls;
	private final List<Reaction> reactions = new ArrayList<Reaction>();
	/*A yield phrase*/
	private final String yieldPhraseProduct = "self::node()/descendant-or-self::ActionPhrase[@type='Yield']//*[self::MOLECULE or self::UNNAMEDMOLECULE]";
	/*A phrase (typically synthesize) containing the returned molecule near the beginning followed by something like "is/was synthesised"*/
	private final String synthesizePhraseProduct = "self::node()/descendant-or-self::NounPhrase[following-sibling::*[1][local-name()='VerbPhrase'][VBD|VBP|VBZ][VB-SYNTHESIZE]]/*[self::MOLECULE or self::UNNAMEDMOLECULE]";
	
	public ExperimentalSectionParser(Chemical titleCompound, List<Element> paragraphEls, Map<String, Chemical> aliasToChemicalMap) {
		this.titleCompound = titleCompound;
		this.paragraphEls = paragraphEls;
		this.aliasToChemicalMap = aliasToChemicalMap;
	}

	public void parseForReactions(){
		if (titleCompound!=null){
			List<Paragraph> paragraphs = new ArrayList<Paragraph>();
			for (Element p : paragraphEls) {
				Paragraph para = new Paragraph(p);
				if (!para.getTaggedString().equals("")){
					paragraphs.add(para);
					List<Element> moleculeEls = findAllMolecules(para);
					for (Element moleculeEl : moleculeEls) {
						Chemical cm = generateChemicalFromMoleculeElAndLocalInformation(moleculeEl);
						moleculeToChemicalMap.put(moleculeEl, cm);
						ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, cm);
						aliasToChemicalMap.putAll(findAliasDefinitions(moleculeEl, cm.getType()));
					}
				}
			}
			reactions.addAll(determineReactions(paragraphs));
		}
	}

	/**
	 * Retrieves the reactions found by this experimental section parser
	 * @return
	 */
	public List<Reaction> getReactions() {
		return reactions;
	}

	/**
	 * Determines where the moleculeEl contains two OSCAR-CM parents, one of which has no resolvable structure
	 * Such cases are assumed to be defining aliases.
	 * The returned map will typically be of size 0 or 1
	 * @param moleculeEl
	 * @param type
	 * @return
	 */
	Map<String, Chemical> findAliasDefinitions(Element moleculeEl, ChemicalType type) {
		Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();
		if (type!=ChemicalType.exact && type!=ChemicalType.exactReference){
			return aliasToChemicalMap;
		}
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
			String name1 = findMoleculeNameFromOscarCM(firstOscarcm);
			String smiles1 = Utils.resolveNameToSmiles(name1);
			String name2 = findMoleculeNameFromOscarCM(secondOscarcm);
			String smiles2 =Utils.resolveNameToSmiles(name2);

			if (smiles1 !=null && smiles2 ==null){
				Chemical cm = new Chemical(name2);
				cm.setSmiles(smiles1);
				aliasToChemicalMap.put(name2, cm);
				LOG.trace(name1 +" is the same as " + name2 +" " +moleculeEl.getParent().toXML());
			}
			else if (smiles1 ==null && smiles2 !=null){
				Chemical cm = new Chemical(name1);
				cm.setSmiles(smiles2);
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
		String name = findMoleculeName(moleculeEl);
		Chemical chem = new Chemical(name);
		chem.setSmiles(Utils.resolveNameToSmiles(name));
		String rawInchi = Utils.resolveNameToInchi(name);
		if (rawInchi!=null){
			chem.setInchi(InchiNormaliser.normaliseInChI(rawInchi));
		}
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
	 * Finds the chemical name of the chemical described by a chemical tagger MOLECULE_Container/UNNAMEDMOLECULE_Container
	 * @param molecule
	 * @return
	 */
	private String findMoleculeName(Element molecule) {
		String elName= molecule.getLocalName();
		if (elName.equals(MOLECULE_Container)) {
			Element oscarCM = molecule.getFirstChildElement(OSCARCM_Container);
			if (oscarCM ==null){
				throw new IllegalArgumentException("malformed Molecule, no child OSCAR-CM: " + molecule.toXML());
			}
			return findMoleculeNameFromOscarCM(oscarCM);
		}
		else if (elName.equals(UNNAMEDMOLECULE_Container)) {
			return findMoleculeNameFromEl(molecule);
		}
		throw new IllegalArgumentException("Unexpected tag type:" + elName +" The following are allowed" +
				MOLECULE_Container + ", "+ UNNAMEDMOLECULE_Container);
	}
	
	
	/**
	 * Finds the chemical name of the chemical described by a chemical tagger MOLECULE tag
	 * @param molecule
	 * @return
	 */
	private String findMoleculeNameFromOscarCM(Element oscarCM) {
		if (oscarCM == null){
			throw new IllegalArgumentException("Input oscarCM was null");
		}
		Elements multiWordNameWords = oscarCM.getChildElements(OSCAR_CM);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < multiWordNameWords.size(); i++) {
			builder.append(multiWordNameWords.get(i).getValue());
			if (i < multiWordNameWords.size()-1) {
				builder.append(" ");
			}
		}
		return builder.toString();	
	}
	
	/**
	 * Returns the space concatenated value of the tags except those tags that are quantity tags of children thereof
	 * @param molecule
	 * @return
	 */
	private String findMoleculeNameFromEl(Element molecule) {
		StringBuilder builder = new StringBuilder();
		LinkedList<Element> stack = new LinkedList<Element>();
		stack.add(molecule);
		while (!stack.isEmpty()) {
			Element currentEl =stack.removeFirst();
			Elements els = currentEl.getChildElements();
			if (els.size()>0){
				for (int i = 0; i < els.size(); i++) {
					Element elToConsider =els.get(i);
					if (!elToConsider.getLocalName().equals(QUANTITY_Container)){
						stack.add(elToConsider);
					}
				}
			}
			else{
				builder.append(currentEl.getValue());
				builder.append(' ');
			}
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}


	/**
	 * Creates a list of all MOLECULE or UNNAMEDMOLECULE elements
	 * @return
	 */
	private List<Element> findAllMolecules(Paragraph paragraph) {
		List <Element> mols = new ArrayList<Element>();
		Document chemicalTaggerResult =paragraph.getTaggedSentencesDocument();
		Nodes molecules = chemicalTaggerResult.query("//*[self::MOLECULE or self::UNNAMEDMOLECULE]");
		for (int i = 0; i < molecules.size(); i++) {
			Element molecule = (Element) molecules.get(i);
			mols.add(molecule);
		}
		return mols;
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
				Set<Element> productAfterReagants = identifyYieldedProduct(phrase, inSynthesis);
				if (productAfterReagants.size() >0 && currentReaction.getReactants().size()==0 && isBackReference(productAfterReagants)){
					productAfterReagants.clear();
				}
				if (productAfterReagants.size() >0 ){
					reagentsExpectedAfterProduct = false;
				}
				Set<Element> productBeforeReagents = identifyProductBeforeReagents(phrase);
				if (productBeforeReagents.size() >0 ){
					reagentsExpectedAfterProduct = true;
				}
				Set<Element> products = new LinkedHashSet<Element>(productAfterReagants);
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
						tempReaction.addProduct(chemChem);
					}
					else if (ChemicalRole.reactant.equals(chemChem.getRole())){
						tempReaction.addReactant(chemChem);
					}
					else if (ChemicalRole.solvent.equals(chemChem.getRole())){
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
		addImplicitTitleCompoundToFinalReactionIfRequired(reactions);
		for (Reaction reaction : reactions) {
			new ChemicalSenseApplication(reaction).reassignMisCategorisedReagents();
		}
		return reactions;
	}

	private boolean isBackReference(Set<Element> yieldedCompounds) {
		if (yieldedCompounds.size()==1){
			Chemical cm = moleculeToChemicalMap.get(yieldedCompounds.iterator().next());
			if (cm.getType().equals(ChemicalType.exactReference)){
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
	 * @param inSynthesis 
	 * @return 
	 */
	private Set<Element> identifyYieldedProduct(Element phrase, boolean inSynthesis) {
		Set<Element> products = new LinkedHashSet<Element>();
		Nodes yieldPhraseMolecules = phrase.query(yieldPhraseProduct);
		boolean foundProductWithQuantity =false;
		for (int i = 0; i < yieldPhraseMolecules.size(); i++) {
			Element synthesizedMolecule= (Element) yieldPhraseMolecules.get(i);
			Chemical chem = moleculeToChemicalMap.get(synthesizedMolecule);
			if (chem.getType().equals(ChemicalType.falsePositive)){
				continue;
			}
			boolean hasQuantity = (chem.getAmountValue() !=null || chem.getMassValue() !=null || chem.getPercentYield() !=null);
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
			if (chemChem.getType() ==ChemicalType.exactReference){
				attemptToResolveBackReference(chemChem, reactions);
			}
		}
		
	}

	boolean attemptToResolveBackReference(Chemical chemical, List<Reaction> reactionsToConsider) {
		if (chemical.getName().equalsIgnoreCase("title compound")){
			chemical.setSmiles(titleCompound.getSmiles());
			chemical.setInchi(titleCompound.getInchi());
			chemical.setRole(ChemicalRole.product);
			return true;
		}
		if (chemical.getSmarts()!=null){
			boolean success = attemptToResolveViaSmartsMatch(chemical.getSmarts(), chemical, reactionsToConsider);
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
			boolean success = attemptToResolveViaSmartsMatch(chemical.getSmiles(), chemical, reactionsToConsider);
			if (success){
				chemical.setRole(ChemicalRole.reactant);
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

	private boolean attemptToResolveViaSmartsMatch(String smarts, Chemical backReference, List<Reaction> reactionsToConsider) {
		IndigoObject query = indigo.loadSmarts(smarts);
		List<Chemical> chemicalMatches = new ArrayList<Chemical>();
		for (Reaction reaction : reactionsToConsider) {
			List<Chemical> products = reaction.getProducts();
			for (Chemical chemical : products) {
				if (chemical.getSmiles()!=null){
					IndigoObject substructureMatcher = indigo.substructureMatcher(indigo.loadMolecule(chemical.getSmiles()));
					if (substructureMatcher.match(query) !=null){
						chemicalMatches.add(chemical);
					}
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

	private void addImplicitTitleCompoundToFinalReactionIfRequired(List<Reaction> reactions) {
		if (reactions.size()>0){
			Reaction lastReaction = reactions.get(reactions.size()-1);
			if (lastReaction.getProducts().size()==0){//product was probably implicitly the title compound
				boolean titleCompoundUsedAsProduct =false;
				if (titleCompound.getInchi()!=null){
					reactionLoop: for (int i = 0; i < reactions.size()-1; i++) {
						Reaction reaction =reactions.get(i);
						for (Chemical product : reaction.getProducts()) {
							if (titleCompound.getInchi().equals(product.getInchi())){
								titleCompoundUsedAsProduct =true;
								break reactionLoop;
							}
						}
					}
				}
				if (!titleCompoundUsedAsProduct){
					lastReaction.addProduct(titleCompound);
				}
			}
		}
	}
}

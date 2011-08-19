package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.Utils;

public class ExperimentalSectionParser {
	private static Logger LOG = Logger.getLogger(ExperimentalSectionParser.class);
	private final ExperimentalSection experimentalSection;
	private final BiMap<Element, Chemical>  moleculeToChemicalMap = HashBiMap.create();
	private final PreviousReactionData previousReactionData;
	private final List<Reaction> sectionReactions = new ArrayList<Reaction>();

	public ExperimentalSectionParser(ExperimentalSection experimentalSection, PreviousReactionData previousReactionData) {
		if (experimentalSection ==null || previousReactionData==null){
			throw new IllegalArgumentException("Null input parameter");
		}
		this.experimentalSection = experimentalSection;
		this.previousReactionData = previousReactionData;
	}

	public void parseForReactions(){
		Chemical ultimateTargetCompound = null;
		Chemical currentStepTargetCompound = null;
		Map<String, Chemical> aliasToChemicalMap = previousReactionData.getAliasToChemicalMap();
		if (experimentalSection.getTargetChemicalNamePair()!=null){
			ChemicalNameAliasPair nameAliasPair = experimentalSection.getTargetChemicalNamePair();
			ultimateTargetCompound = Utils.createChemicalFromName(nameAliasPair.getChemicalName());
			if (nameAliasPair.getAlias() !=null){
				aliasToChemicalMap.put(nameAliasPair.getAlias(), ultimateTargetCompound);
			}
		}
		List<ExperimentalStep> steps = experimentalSection.getExperimentalSteps();
		for (int i = 0; i < steps.size(); i++) {
			ExperimentalStep step = steps.get(i);
			if (step.getTargetChemicalNamePair()!=null){
				ChemicalNameAliasPair nameAliasPair = step.getTargetChemicalNamePair();
				currentStepTargetCompound = Utils.createChemicalFromName(nameAliasPair.getChemicalName());
				if (nameAliasPair.getAlias() !=null){
					aliasToChemicalMap.put(nameAliasPair.getAlias(), currentStepTargetCompound);
				}
			}
			else if (i == steps.size()-1){
				//last step can be implicitly the ultimate target compound
				currentStepTargetCompound = ultimateTargetCompound;
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
			ExperimentalStepParser stepParser = new ExperimentalStepParser(step, moleculeToChemicalMap, currentStepTargetCompound);
			List<Reaction> reactions = stepParser.extractReactions();
			for (Reaction reaction : reactions) {
				new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
				sectionReactions.add(reaction);
			}
			if (experimentalSection.getProcedureElement() !=null){
				recordReactionsInPreviousReactionData(reactions, step);
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
		Chemical referencedChemical = previousReactionData.getAliasToChemicalMap().get(identifier);
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
		Chemical referencedChemical = previousReactionData.getAliasToChemicalMap().get(name);
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
	 * Associate the given reactions with the current section and step's procedure identifiers
	 * 
	 * @param reactions
	 * @param step 
	 */
	private void recordReactionsInPreviousReactionData(List<Reaction> reactions, ExperimentalStep step) {
		if (experimentalSection.getProcedureElement()==null){
			throw new RuntimeException("procedure element should not be null if this method is called");
		}
		String sectionIdentifier = getSectionIdentifier(experimentalSection.getProcedureElement());
		if (sectionIdentifier ==null){
			LOG.debug(experimentalSection.getProcedureElement().toXML() +" was not understood as section identifier");
			return;
		}
		String stepIdentifier =null;
		if (step.getProcedureEl()!=null){
			stepIdentifier = getStepIdentifier(step.getProcedureEl(), sectionIdentifier);
			if (stepIdentifier ==null){
				LOG.debug(step.getProcedureEl().toXML() +" was not understood as step identifier");
				return;
			}
		}
		previousReactionData.addReactions(reactions, sectionIdentifier, stepIdentifier);
	}

	/**
	 * Returns value of a cd/cdalphanum/nnidentifier if this element has only one of them
	 * else null
	 * @param procedureEl
	 * @return
	 */
	String getSectionIdentifier(Element procedureEl) {
		List<Element> sectionIdentifiers = XOMTools.getDescendantElementsWithTagNames(procedureEl, new String[]{NN_IDENTIFIER, CD, CD_ALPHANUM});
		if (sectionIdentifiers.size()==1){
			return sectionIdentifiers.get(0).getValue();
		}
		return null;
	}
	
	/**
	 * Returns value of a cd/cdalphanum/nnidentifier if this element has only one of them
	 * If there are two returns the second is the first matches the sectionIdentifier
	 * else null
	 * @param procedureEl
	 * @param sectionIdentifier
	 * @return
	 */
	String getStepIdentifier(Element procedureEl, String sectionIdentifier) {
		List<Element> stepIdentifiers = XOMTools.getDescendantElementsWithTagNames(procedureEl, new String[]{NN_IDENTIFIER, CD, CD_ALPHANUM});
		if (stepIdentifiers.size()==1){
			return stepIdentifiers.get(0).getValue();
		}
		if (stepIdentifiers.size()==2 && sectionIdentifier.equals(stepIdentifiers.get(0).getValue())){
			return stepIdentifiers.get(1).getValue();
		}
		return null;
	}

}

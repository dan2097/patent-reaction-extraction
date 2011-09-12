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

import uk.ac.cam.ch.wwmm.opsin.StringTools;
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
		if (experimentalSection.getTargetChemicalNamePair()!=null){
			ChemicalAliasPair nameAliasPair = experimentalSection.getTargetChemicalNamePair();
			ultimateTargetCompound = nameAliasPair.getChemical();
			if (nameAliasPair.getAlias() !=null){
				previousReactionData.getAliasToChemicalMap().put(nameAliasPair.getAlias(), ultimateTargetCompound);
			}
		}
		List<ExperimentalStep> steps = experimentalSection.getExperimentalSteps();
		for (int i = 0; i < steps.size(); i++) {
			ExperimentalStep step = steps.get(i);
			if (step.getTargetChemicalNamePair()!=null){
				ChemicalAliasPair nameAliasPair = step.getTargetChemicalNamePair();
				currentStepTargetCompound = nameAliasPair.getChemical();
				if (nameAliasPair.getAlias() !=null){
					previousReactionData.getAliasToChemicalMap().put(nameAliasPair.getAlias(), currentStepTargetCompound);
				}
			}
			else if (i == steps.size()-1){
				//last step can be implicitly the ultimate target compound
				currentStepTargetCompound = ultimateTargetCompound;
			}
			processMoleculeToChemicalAndStringToChemicalMappings(step.getParagraphs());
			ExperimentalStepParser stepParser = new ExperimentalStepParser(step, moleculeToChemicalMap, currentStepTargetCompound);
			List<Reaction> reactions = stepParser.extractReactions();
			for (Reaction reaction : reactions) {
				new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
				sectionReactions.add(reaction);
			}
			recordReactionsInPreviousReactionData(reactions, step);
		}
	}

	/**
	 * Adds molecule to to chemical mapping for all molecule and unnamed molecules
	 * Adds string to chemical mappings as appropriate
	 * Resolves structures using such mappings
	 * @param paragraphs
	 */
	private void processMoleculeToChemicalAndStringToChemicalMappings(List<Paragraph> paragraphs) {
		Map<String, Chemical> aliasToChemicalMap = previousReactionData.getAliasToChemicalMap();
		for (Paragraph paragraph : paragraphs) {
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
	}

	private void attemptToResolveAnaphora(Element molOrUnnamedEl, Chemical cm) {
		if (cm.getType()!=null){
			if (cm.getSmiles()!=null && !ChemicalType.definiteReference.equals(cm.getType())){
				//for molecules with known smiles that do not appear to be a back reference do not attempt to resolve the structure by back reference
				return;
			}
		}
		List<Element> references = XOMTools.getDescendantElementsWithTagName(molOrUnnamedEl, ChemicalTaggerTags.REFERENCETOCOMPOUND_Container);
		if (references.size()==1){
			attemptToResolveReferenceToCompound(references.get(0), cm);
		}
		else if (references.size() >0){
			LOG.debug("Multiple referenceToCompounds present in : " +molOrUnnamedEl.toXML());
		}
		List<Element> procedures = XOMTools.getDescendantElementsWithTagName(molOrUnnamedEl, ChemicalTaggerTags.PROCEDURE_Container);
		if (procedures.size()==1){
			attemptToResolveReferenceToProcedure(procedures.get(0), cm);
		}
		else if (procedures.size() >0){
			LOG.debug("Multiple procedures present in : " +molOrUnnamedEl.toXML());
		}
	}

	private void attemptToResolveReferenceToCompound(Element reference, Chemical cm) {
		String identifier = getIdentifierFromReference(reference);
		Chemical referencedChemical = previousReactionData.getAliasToChemicalMap().get(identifier);
		if (referencedChemical !=null){
			cm.setSmiles(referencedChemical.getSmiles());
			cm.setInchi(referencedChemical.getInchi());
		}
		else{
			LOG.trace("Failed to resolve reference to compound: " + identifier );
		}
	}
	
	/**
	 * We make the assumption that a reference to a procedure means that the chemical is the
	 * product of that procedure. Resolution is achieved using previousReactionData
	 * @param procedure
	 * @param cm
	 */
	private void attemptToResolveReferenceToProcedure(Element procedureEl, Chemical cm) {
		SectionAndStepIdentifier sectionAndStepIdentifier = getSectionAndStepIdentifier(procedureEl);
		if (sectionAndStepIdentifier!=null){
			Chemical referencedChemical = previousReactionData.getProductOfReaction(sectionAndStepIdentifier.getSectionIdentifier(), sectionAndStepIdentifier.getStepIdentifier());
			if (referencedChemical !=null){
				cm.setSmiles(referencedChemical.getSmiles());
				cm.setInchi(referencedChemical.getInchi());
				return;
			}
		}
		LOG.trace("Failed to resolve reference to procecdure: " + procedureEl.toXML() );
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
			List<String> nameComponents1 = ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(firstOscarcm);
			String smiles1 = Utils.resolveNameToSmiles(nameComponents1);
			String name1 = StringTools.stringListToString(nameComponents1, " ");
			List<String> nameComponents2 = ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(secondOscarcm);
			String smiles2 =Utils.resolveNameToSmiles(nameComponents2);
			String name2 = StringTools.stringListToString(nameComponents2, " ");

			if (smiles1 !=null && smiles2 ==null){
				Chemical cm = new Chemical(name2);
				cm.setSmiles(smiles1);
				cm.setInchi(Utils.resolveNameToInchi(nameComponents1));
				aliasToChemicalMap.put(name2, cm);
				LOG.trace(name1 +" is the same as " + name2 +" " +moleculeEl.getParent().toXML());
			}
			else if (smiles1 ==null && smiles2 !=null){
				Chemical cm = new Chemical(name1);
				cm.setSmiles(smiles2);
				cm.setInchi(Utils.resolveNameToInchi(nameComponents2));
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
		List<String> nameComponents = ChemTaggerOutputNameExtraction.findMoleculeName(moleculeEl);
		Chemical chem = Utils.createChemicalFromName(nameComponents);
		String name = chem.getName();
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
			throw new RuntimeException("procedure element should never be null after section creation");
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
	 * Gets the identifier (or cd) from a procedure expected to describe a section
	 * null if more than 1 identifier found
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
	 * Gets the identifier (or cd) from a procedure expected to describe a step in a particular secion
	 * null if more than 2 identifiers found or the first identifier isn't the section identifier
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
	
	/**
	 * Gets the identifier (or cd) from a procedure expected to describe a step
	 * null if more than 2 identifiers found
	 * @param procedureEl
	 * @param sectionIdentifier
	 * @return
	 */
	SectionAndStepIdentifier getSectionAndStepIdentifier(Element procedureEl) {
		List<Element> stepIdentifiers = XOMTools.getDescendantElementsWithTagNames(procedureEl, new String[]{NN_IDENTIFIER, CD, CD_ALPHANUM});
		if (stepIdentifiers.size()==1){
			Element stepIdentifier = stepIdentifiers.get(0);
			if (isSectionIdentifier(stepIdentifier)){
				return new SectionAndStepIdentifier(stepIdentifier.getValue(), null);
			}
			else{
				String sectionIdentifier = getSectionIdentifier(experimentalSection.getProcedureElement());
				return new SectionAndStepIdentifier(sectionIdentifier, stepIdentifier.getValue());
			}
		}
		if (stepIdentifiers.size()==2){
			boolean firstIsSectionIdentifier = isSectionIdentifier(stepIdentifiers.get(0));
			boolean secondIsSectionIdentifier = isSectionIdentifier(stepIdentifiers.get(1));
			if (firstIsSectionIdentifier && !secondIsSectionIdentifier){
				return new SectionAndStepIdentifier(stepIdentifiers.get(0).getValue(), stepIdentifiers.get(1).getValue());
			}
			if (!firstIsSectionIdentifier && secondIsSectionIdentifier){
				return new SectionAndStepIdentifier(stepIdentifiers.get(1).getValue(), stepIdentifiers.get(0).getValue());
			}
			if (!firstIsSectionIdentifier && !secondIsSectionIdentifier){
				return new SectionAndStepIdentifier(stepIdentifiers.get(0).getValue(), stepIdentifiers.get(1).getValue());
			}
			//both being section identifiers is not intentionally not interpretable
		}
		return null;
	}
	
	/**
	 * Is the identifier preceded by a suitable qualifier to indicate that this is a section identifier
	 * @param stepIdentifier
	 * @return
	 */
	boolean isSectionIdentifier(Element stepIdentifier){
		Element qualifier = (Element) XOMTools.getPreviousSibling(stepIdentifier);
		if (qualifier !=null && 
				(qualifier.getLocalName().equals(NN_EXAMPLE) || qualifier.getLocalName().equals(NN_METHOD))
				&& !Utils.isSynonymnOfStep(qualifier.getValue())){
			return true;
		}
		return false;
	}

}

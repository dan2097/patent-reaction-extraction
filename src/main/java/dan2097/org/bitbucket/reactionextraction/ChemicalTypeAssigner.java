package dan2097.org.bitbucket.reactionextraction;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import dan2097.org.bitbucket.utility.Utils;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import nu.xom.Element;

public class ChemicalTypeAssigner {
	private static final String FALSE_POSITIVE_REGEXES_LOCATION = "/dan2097/org/bitbucket/reactionextraction/falsePositiveRegexes.txt";
	
	private static final Pattern matchPluralEnding = Pattern.compile(".*[abcdefghklmnpqrtwy]s$");
	private static final Pattern matchSurfacePreQualifier = Pattern.compile("on|onto", Pattern.CASE_INSENSITIVE);
	private static final Pattern matchSurfaceQualifier = Pattern.compile("surface|interface", Pattern.CASE_INSENSITIVE);
	private static final Pattern matchClassQualifier = Pattern.compile("(compound|derivative)[s]?", Pattern.CASE_INSENSITIVE);
	private static final Pattern matchFragmentQualifier = Pattern.compile("group[s]?|atom[s]?|functional|ring[s]?|chain[s]?|bond[s]?|bridge[s]?|contact[s]?|complex", Pattern.CASE_INSENSITIVE);
	public static final List<Pattern> falsePositivePatterns = new ArrayList<Pattern>();
	
	static{
		Set<String> regexes = Utils.fileToStringSet(FALSE_POSITIVE_REGEXES_LOCATION);
		for (String regex : regexes) {
			falsePositivePatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
		}
	}

	/**
	 * Determines an entity type for a chemical entity based on the chemical name itself and local textual information
	 * @param mol
	 * @param chem
	 */
	public static ChemicalEntityType determineEntityTypeOfChemical(Element mol, Chemical chem) {
		String chemicalName = chem.getName();
		if (isFalsePositive(chemicalName, mol)){
			return ChemicalEntityType.falsePositive;
		}
		ChemicalEntityType entityType = determineTypeFromSurroundingText(mol);
		if (entityType == null){
			entityType = determineTypeFromChemicalName(chemicalName, chem.getSmiles() != null);
		}

		if (!ChemicalEntityType.falsePositive.equals(chem.getEntityType()) && (hasQualifyingIdentifier(mol) || isTextualAnaphora(chemicalName))){
			return ChemicalEntityType.definiteReference;
		}
		if (entityType != null){
			return entityType;
		}
		if (hasNoQuantitiesOrStructureAndUninterpretableByOpsinParser(mol, chem)){
			return ChemicalEntityType.falsePositive;
		}
		else{
			return ChemicalEntityType.exact;
		}
	}

	public static boolean isFalsePositive(String chemicalName, Element mol) {
		if (ATMOSPHEREPHRASE_Container.equals(((Element) mol.getParent()).getLocalName())){
			return true;
		}
		if (APPARATUS_Container.equals(((Element) mol.getParent()).getLocalName())){
			return true;
		}
		for (Pattern pat : falsePositivePatterns) {
			if (pat.matcher(chemicalName).matches()){
				return true;
			}
		}
		return false;
	}

	/**
	 * Looks at the word before the first OSCARCM and after the MOLECULE
	 * to determine whether the chemical type
	 * Returns null if type cannot be determined from the surrounding text
	 * @param mol
	 * @return 
	 */
	static ChemicalEntityType determineTypeFromSurroundingText(Element mol) {
		Element nextEl = getElementAfterLastOSCARCM(mol);
		if (nextEl != null){//examine the head noun
			if (matchSurfaceQualifier.matcher(nextEl.getValue()).matches()){
				return ChemicalEntityType.falsePositive;
			}
			else if (matchFragmentQualifier.matcher(nextEl.getValue()).matches()){
				return ChemicalEntityType.fragment;
			}
		}
		Element previousEl = getElementBeforeFirstOSCARCM(mol);
		if (previousEl != null){
			String previousElVal = previousEl.getValue();
			if (matchSurfacePreQualifier.matcher(previousElVal).matches()){
				return ChemicalEntityType.falsePositive;
			}
			else if (previousElVal.equalsIgnoreCase("a") || previousElVal.equalsIgnoreCase("an")){
				return ChemicalEntityType.chemicalClass;
			}
			else if (previousEl.getLocalName().equals(DT_THE)){
				return ChemicalEntityType.definiteReference;
			}
		}
		if (nextEl != null && matchClassQualifier.matcher(nextEl.getValue()).matches()){
			return ChemicalEntityType.chemicalClass;
		}
		return null;
	}

	/**
	 * Attempts to assign a type using the output of the OPSIN document extractor is possible
	 * or failing that from whether the name has a plural ending
	 * Returns null if the type cannot be determined from just the chemical name
	 * @param chemicalName
	 * @param resolvableToSmiles 
	 * @return 
	 */
	private static ChemicalEntityType determineTypeFromChemicalName(String chemicalName, boolean resolvableToSmiles) {
		if (FunctionalGroupDefinitions.getFunctionalClassSmartsFromChemicalName(chemicalName) != null){
			return ChemicalEntityType.chemicalClass;
		}
		List<IdentifiedChemicalName> identifiedNames = new DocumentToStructures(chemicalName).extractNames();
		if (identifiedNames.size() == 1 && identifiedNames.get(0).getTextValue().equals(chemicalName)){
			switch (identifiedNames.get(0).getNameType()) {
			case family:
				return ChemicalEntityType.chemicalClass;
			case part:
				return ChemicalEntityType.fragment;
			default:
				//"polymer" and "complete" are insufficient to classify without surrounding text
				break;
			}
		}
		else if (!resolvableToSmiles && matchPluralEnding.matcher(chemicalName).matches()){
			return ChemicalEntityType.chemicalClass;
		}
		return null;
	}

	private static boolean hasQualifyingIdentifier(Element mol) {
		return XOMTools.getDescendantElementsWithTagNames(mol, new String[]{REFERENCETOCOMPOUND_Container, PROCEDURE_Container}).size() > 0;
	}

	private static boolean isTextualAnaphora(String chemicalName) {
		return ExperimentalStepParser.matchProductTextualAnaphora.matcher(chemicalName).matches();
	}

	private static boolean hasNoQuantitiesOrStructureAndUninterpretableByOpsinParser(Element mol, Chemical chem) {
		return (chem.getSmiles() == null &&
				chem.getInchi() == null &&
				XOMTools.getDescendantElementsWithTagName(mol, QUANTITY_Container).size() == 0 &&
				!ReactionExtractionMethods.isKnownTrivialNameWithNoCT(chem) &&
				Utils.getSystematicChemicalNamesFromText(chem.getName()).size() == 0);
	}

	private static Element getElementAfterLastOSCARCM(Element mol) {
		List<Element> oscarcms = XOMTools.getDescendantElementsWithTagNames(mol, new String[]{OSCARCM_Container});
		if (oscarcms.size() > 0){
			return Utils.getNextElement(oscarcms.get(oscarcms.size() - 1));
		}
		else{
			return Utils.getNextElement(mol);
		}
	}
	
	private static Element getElementBeforeFirstOSCARCM(Element mol) {
		List<Element> oscarcms = XOMTools.getDescendantElementsWithTagNames(mol, new String[]{OSCARCM_Container});
		if (oscarcms.size() > 0){
			return Utils.getPreviousElement(oscarcms.get(0));
		}
		else{
			return Utils.getPreviousElement(mol);
		}
	}
}

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
	private static Pattern matchPluralEnding = Pattern.compile(".*[abcdefghklmnpqrtwy]s$", Pattern.CASE_INSENSITIVE);
	private static Pattern matchSurfacePreQualifier = Pattern.compile("on|onto", Pattern.CASE_INSENSITIVE);
	private static Pattern matchSurfaceQualifier = Pattern.compile("surface|interface", Pattern.CASE_INSENSITIVE);
	private static Pattern matchClassQualifier = Pattern.compile("(compound|derivative)[s]?", Pattern.CASE_INSENSITIVE);
	private static Pattern matchFragmentQualifier = Pattern.compile("group[s]?|atom[s]?|functional|ring[s]?|chain[s]?|bond[s]?|bridge[s]?|contact[s]?|complex", Pattern.CASE_INSENSITIVE);
	public static List<Pattern> falsePositivePatterns = new ArrayList<Pattern>();
	private static String FALSE_POSITIVE_REGEXES_LOCATION = "/dan2097/org/bitbucket/reactionextraction/falsePositiveRegexes.txt";
	
	static{
		Set<String> regexes = Utils.fileToStringSet(FALSE_POSITIVE_REGEXES_LOCATION);
		for (String regex : regexes) {
			falsePositivePatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
		}
	}

	/**
	 * Assigns a preliminary type to each chemical based on the chemical name itself and local textual information
	 * @param mol
	 * @param chem
	 */
	static void assignTypeToChemical(Element mol, Chemical chem) {
		String chemicalName = chem.getName();
		if (isFalsePositive(chem, mol)){
			chem.setType(ChemicalType.falsePositive);
		}
		else if (!determineTypeFromSurroundingText(mol, chem)){
			determineTypeFromChemicalName(chemicalName, chem);
		}
		
		if (!ChemicalType.falsePositive.equals(chem.getType()) && (hasQualifyingIdentifier(mol) || isTextualAnaphora(chemicalName))){
			chem.setType(ChemicalType.definiteReference);
		}
		if (chem.getType()==null){
			if (hasNoQuantitiesOrStructureAndUninterpretableByOpsinParser(mol, chem)){
				chem.setType(ChemicalType.falsePositive);
			}
			else{
				chem.setType(ChemicalType.exact);
			}
		}
	}

	/**
	 * Attempts to assign a type using the output of the OPSIN document extractor is possible
	 * or failing that from whether the name has a plural ending
	 * Returns true or false depending on whether the type was set from just the chemical name
	 * @param chemicalName
	 * @param chem
	 * @return 
	 */
	private static boolean determineTypeFromChemicalName(String chemicalName, Chemical chem) {
		List<IdentifiedChemicalName> identifiedNames = new DocumentToStructures(chemicalName).extractNames();
		if (identifiedNames.size()==1 && identifiedNames.get(0).getTextValue().equals(chemicalName)){
			switch (identifiedNames.get(0).getNameType()) {
			case family:
				chem.setType(ChemicalType.chemicalClass);
				return true;
			case part:
				chem.setType(ChemicalType.fragment);
				return true;
			default:
				//"polymer" and "complete" are insufficient to classify without surrounding text
				break;
			}
		}
		else if (matchPluralEnding.matcher(chemicalName).matches()){
			chem.setType(ChemicalType.chemicalClass);
			return true;
		}
		else if (FunctionalGroupDefinitions.functionalClassToSmartsMap.get(chemicalName.toLowerCase())!=null){
			chem.setType(ChemicalType.chemicalClass);
			return true;
		}
		return false;
	}

	/**
	 * Looks at the word before the first OSCARCM and after the MOLECULE
	 * to determine whether the chemical type
	 * Returns true or false depending on whether the type was set from the surrounding text
	 * @param mol
	 * @param chem
	 * @return 
	 */
	private static boolean determineTypeFromSurroundingText(Element mol, Chemical chem) {
		Element nextEl = Utils.getNextElement(mol);
		if (nextEl !=null){//examine the head noun
			if (matchSurfaceQualifier.matcher(nextEl.getValue()).matches()){
				chem.setType(ChemicalType.falsePositive);
				return true;
			}
			else if (matchFragmentQualifier.matcher(nextEl.getValue()).matches()){
				chem.setType(ChemicalType.fragment);
				return true;
			}
		}
		Element previousEl = getElementBeforeFirstOSCARCM(mol);
		if (previousEl !=null){
			if (matchSurfacePreQualifier.matcher(previousEl.getValue()).matches()){
				chem.setType(ChemicalType.falsePositive);
				return true;
			}
			else if (previousEl.getLocalName().equals(DT)){
				chem.setType(ChemicalType.chemicalClass);
				return true;
			}
			else if (previousEl.getLocalName().equals(DT_THE)){
				chem.setType(ChemicalType.definiteReference);
				return true;
			}
		}
		if (nextEl !=null && matchClassQualifier.matcher(nextEl.getValue()).matches()){
			chem.setType(ChemicalType.chemicalClass);
			return true;
		}
		return false;
	}

	private static boolean hasQualifyingIdentifier(Element mol) {
		return XOMTools.getDescendantElementsWithTagNames(mol, new String[]{REFERENCETOCOMPOUND_Container, PROCEDURE_Container}).size()>0;
	}

	private static boolean isTextualAnaphora(String chemicalName) {
		return ExperimentalStepParser.matchProductTextualAnaphora.matcher(chemicalName).matches();
	}

	private static boolean hasNoQuantitiesOrStructureAndUninterpretableByOpsinParser(Element mol, Chemical chem) {
		return (chem.getSmiles() == null &&
				chem.getInchi() == null &&
				XOMTools.getDescendantElementsWithTagName(mol, QUANTITY_Container).size()==0 &&
				Utils.getSystematicChemicalNamesFromText(chem.getName()).size()==0);
	}

	private static boolean isFalsePositive(Chemical chem, Element mol) {
		String chemicalName = chem.getName();
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
	
	private static Element getElementBeforeFirstOSCARCM(Element mol) {
		List<Element> oscarcms = XOMTools.getDescendantElementsWithTagNames(mol, new String[]{OSCARCM_Container});
		if (oscarcms.size()>0){
			return Utils.getPreviousElement(oscarcms.get(0));
		}
		else{
			return Utils.getPreviousElement(mol);
		}
	}
}

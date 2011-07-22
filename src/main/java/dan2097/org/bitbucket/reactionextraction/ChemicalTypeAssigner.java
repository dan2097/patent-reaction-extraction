package dan2097.org.bitbucket.reactionextraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.Utils;

import nu.xom.Element;

public class ChemicalTypeAssigner {
	private static Pattern matchPluralEnding = Pattern.compile(".*[abcdefghklmnpqrtwy]s$", Pattern.CASE_INSENSITIVE);
	private static Pattern matchSurfacePreQualifier = Pattern.compile("on|onto", Pattern.CASE_INSENSITIVE);
	private static Pattern matchSurfaceQualifier = Pattern.compile("surface|interface", Pattern.CASE_INSENSITIVE);
	private static Pattern matchClassQualifier = Pattern.compile("(compound|derivative)[s]?", Pattern.CASE_INSENSITIVE);
	private static Pattern matchFragmentQualifier = Pattern.compile("group[s]?|atom[s]?|functional|ring[s]?|chain[s]?|bond[s]?|bridge[s]?|contact[s]?|complex", Pattern.CASE_INSENSITIVE);
	private static Pattern matchTextualAnaphora= Pattern.compile("(crude|title|final) (compound|product)", Pattern.CASE_INSENSITIVE);
	private static List<Pattern> falsePositivePatterns = new ArrayList<Pattern>();
	private static String FALSE_POSITIVE_REGEXES_LOCATION = "/dan2097/org/bitbucket/reactionextraction/falsePositiveRegexes.txt";
	
	static{
		InputStream is = ChemicalTypeAssigner.class.getResourceAsStream(FALSE_POSITIVE_REGEXES_LOCATION);
		if (is ==null){
			throw new RuntimeException("Failed to read " +FALSE_POSITIVE_REGEXES_LOCATION);
		}
		try{
			List<String> lines = IOUtils.readLines(is);
			for (String line : lines) {
				if (line.startsWith("#") || line.equals("")){
					continue;
				}
				falsePositivePatterns.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
			}
		}
		catch (IOException e ) {
			throw new RuntimeException("Failed to read " + FALSE_POSITIVE_REGEXES_LOCATION, e);
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
		else if (matchPluralEnding.matcher(chemicalName).matches()){
			chem.setType(ChemicalType.chemicalClass);
		}
		else{
			Element nextEl = Utils.getNextElement(mol);
			if (nextEl !=null){//examine the head noun
				if (matchSurfaceQualifier.matcher(nextEl.getValue()).matches()){
					chem.setType(ChemicalType.falsePositive);
				}
				else if (matchFragmentQualifier.matcher(nextEl.getValue()).matches()){
					chem.setType(ChemicalType.fragment);
				}
			}
			if (chem.getType()==null){
				Element previousEl = getElementBeforeFirstOSCARCM(mol);
				if (previousEl !=null){
					if (matchSurfacePreQualifier.matcher(previousEl.getValue()).matches()){
						chem.setType(ChemicalType.falsePositive);
					}
					else if (previousEl.getLocalName().equals(ChemicalTaggerTags.DT)){
						chem.setType(ChemicalType.chemicalClass);
					}
					else if (previousEl.getLocalName().equals(ChemicalTaggerTags.DT_THE)){
						chem.setType(ChemicalType.definiteReference);
					}
				}
			}
			if (chem.getType()==null){
				if (nextEl !=null &&  matchClassQualifier.matcher(nextEl.getValue()).matches()){
					chem.setType(ChemicalType.chemicalClass);
				}
				else if (FunctionalGroupDefinitions.functionalClassToSmartsMap.get(chemicalName.toLowerCase())!=null){
					chem.setType(ChemicalType.chemicalClass);
				}
			}
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

	private static boolean hasQualifyingIdentifier(Element mol) {
		return XOMTools.getDescendantElementsWithTagName(mol, ChemicalTaggerTags.REFERENCETOCOMPOUND_Container).size()>0;
	}

	private static boolean isTextualAnaphora(String chemicalName) {
		return matchTextualAnaphora.matcher(chemicalName).matches();
	}

	private static boolean hasNoQuantitiesOrStructureAndUninterpretableByOpsinParser(Element mol, Chemical chem) {
		return (chem.getSmiles() == null &&
				chem.getInchi() == null &&
				XOMTools.getDescendantElementsWithTagName(mol, ChemicalTaggerTags.QUANTITY_Container).size()==0 &&
				Utils.getSystematicChemicalNamesFromText(chem.getName()).size()==0);
	}

	private static boolean isFalsePositive(Chemical chem, Element mol) {
		String chemicalName = chem.getName();
		if (ChemicalTaggerTags.ATMOSPHEREPHRASE_Container.equals(((Element) mol.getParent()).getLocalName())){
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
		List<Element> oscarcms = XOMTools.getDescendantElementsWithTagNames(mol, new String[]{ChemicalTaggerTags.OSCARCM_Container});
		if (oscarcms.size()>0){
			return Utils.getPreviousElement(oscarcms.get(0));
		}
		else{
			return Utils.getPreviousElement(mol);
		}
	}
}

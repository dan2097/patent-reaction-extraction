package dan2097.org.bitbucket.reactionextraction;

import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.MOLECULE_Container;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.OSCARCM_Container;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.OSCAR_ASE;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.OSCAR_CM;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.REFERENCETOCOMPOUND_Container;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.UNNAMEDMOLECULE_Container;

import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Elements;

public class ChemTaggerOutputNameExtraction {

	
	/**
	 * Finds the chemical name of the chemical described by a chemical tagger MOLECULE_Container/UNNAMEDMOLECULE_Container
	 * @param molecule
	 * @return
	 */
	static String findMoleculeName(Element molecule) {
		String elName= molecule.getLocalName();
		if (elName.equals(MOLECULE_Container)) {
			Element oscarCM = molecule.getFirstChildElement(OSCARCM_Container);
			if (oscarCM ==null){
				throw new IllegalArgumentException("malformed Molecule, no child OSCAR-CM: " + molecule.toXML());
			}
			return findMoleculeNameFromOscarCM(oscarCM);
		}
		else if (elName.equals(UNNAMEDMOLECULE_Container)) {
			return findMoleculeNameFromUnnamedMoleculeEl(molecule);
		}
		throw new IllegalArgumentException("Unexpected tag type:" + elName +" The following are allowed" +
				MOLECULE_Container + ", "+ UNNAMEDMOLECULE_Container);
	}
	
	
	/**
	 * Finds the chemical name of the chemical described by a chemical tagger MOLECULE tag
	 * @param molecule
	 * @return
	 */
	static String findMoleculeNameFromOscarCM(Element oscarCM) {
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
	 * Returns the space concatenated values of the first set of adjective* noun+
	 * @param molecule
	 * @return
	 */
	static String findMoleculeNameFromUnnamedMoleculeEl(Element unnamedMoleculeEl) {
		StringBuilder builder = new StringBuilder();
		Elements children = unnamedMoleculeEl.getChildElements();
		boolean foundStartOfName = false;
		for (int i = 0; i < children.size(); i++) {
			Element elToConsider = children.get(i);
			String localName = elToConsider.getLocalName();
			if (localName.startsWith("JJ") || localName.startsWith("NN")
					|| localName.equals(REFERENCETOCOMPOUND_Container)
					|| localName.equals(OSCAR_ASE)){
				foundStartOfName =true;
				builder.append(getStringContent(elToConsider));
			}
			else if (foundStartOfName) {
				break;
			}
		}
		if (builder.length()==0){
			return null;
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	
	/**
	 * Uses a stack to iteratively enumerate and space concatenate the given element and its descendents in document order
	 * @param startingEl
	 * @return
	 */
	private static StringBuilder getStringContent(Element startingEl) {
		StringBuilder builder = new StringBuilder();
		LinkedList<Element> stack = new LinkedList<Element>();
		stack.add(startingEl);
		while (!stack.isEmpty()) {
			Element currentEl =stack.removeFirst();
			Elements els = currentEl.getChildElements();
			if (els.size()>0){
				for (int i = 0; i < els.size(); i++) {
					stack.add(els.get(i));
				}
			}
			else{
				builder.append(currentEl.getValue());
				builder.append(' ');
			}
		}
		return builder;
	}
}

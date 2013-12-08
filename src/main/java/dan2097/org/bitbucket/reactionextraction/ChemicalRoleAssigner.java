package dan2097.org.bitbucket.reactionextraction;

import nu.xom.Element;
import nu.xom.Node;

import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.Utils;

class ChemicalRoleAssigner {
	private static final AprioriKnowledge chemicalKnowledge = AprioriKnowledge.getInstance();

	/**
	 * Determines the chemical role by:
	 * examining the chemical structure against known catalysts/InChIs
	 * surrounding textual clues
	 * heuristics based on the occurrence of amounts/yield/equivalents
	 * @param chemicalEl
	 * @param chemical
	 * @return
	 */
	static ChemicalRole determineChemicalRole(Element chemicalEl, Chemical chemical) {
		String lcName = chemical.getName().toLowerCase(); 
		if (chemicalKnowledge.isKnownCatalystInChI(chemical.getInchi())){
			return ChemicalRole.catalyst;
		}
		else if (isKnownTrivialCatalyst(lcName)){
			return ChemicalRole.catalyst;
		}
		else if (ChemicalTaggerAtrs.CATALYST_ROLE_VAL.equals(chemicalEl.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
			return ChemicalRole.catalyst;
		}
		else if (chemical.hasAmountOrEquivalentsOrYield()){
			return ChemicalRole.reactant;
		}
		else if (ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(chemicalEl.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
			return ChemicalRole.solvent;
		}
		else if (assignAsSolventDueToInKeyword(chemicalEl, chemical) ){
			return ChemicalRole.solvent;
		}
		else if (chemicalKnowledge.getSolventNames().contains(lcName)){
			return ChemicalRole.solvent;
		}
		else {
			return ChemicalRole.reactant;
		}
	}

	private static boolean isKnownTrivialCatalyst(String lcName) {
		if (lcName.contains("catalyst")){
			return true;
		}
		if (chemicalKnowledge.getCatalystNames().contains(lcName)){
			return true;
		}
		return false;
	}

	private static boolean assignAsSolventDueToInKeyword(Element chemicalEl, Chemical chemical) {
		if (chemicalEl.getLocalName().equals(ChemicalTaggerTags.UNNAMEDMOLECULE_Container) && chemical.getVolumeValue() == null){
			return false;
		}
		if (precededByIn(chemicalEl) || precededByInAMixtureOf(chemicalEl)){
			return true;
		}
		if (precededByInChemicalAnd(chemicalEl)){
			return true;
		}
		return false;
	}

	/**
	 * Matches cases of bar in phrases like 'in foo and bar,' or 'in a mixture of foo and bar' 
	 * @param chemicalEl
	 * @return
	 */
	private static boolean precededByInChemicalAnd(Element chemicalEl) {
		Element previous = Utils.getPreviousElement(chemicalEl);
		if (previous == null || !previous.getLocalName().equals(ChemicalTaggerTags.CC) || !previous.getValue().equalsIgnoreCase("and")){
			return false;
		}
		Element twoBefore = Utils.getPreviousElement(previous);
		if (twoBefore != null){
			Element molecule = getParent(twoBefore, ChemicalTaggerTags.MOLECULE_Container);
			if (molecule == null){
				 molecule = getParent(twoBefore, ChemicalTaggerTags.UNNAMEDMOLECULE_Container);
			}
			if (molecule != null && ((followedByStopOrComma(chemicalEl) && precededByIn(molecule)) || precededByInAMixtureOf(molecule))){
				return true;
			}
		}
		return false;
	}

	private static boolean followedByStopOrComma(Element chemicalEl) {
		Element next = Utils.getNextElement(chemicalEl);
		return (next != null && (next.getLocalName().equals(ChemicalTaggerTags.STOP) || next.getLocalName().equals(ChemicalTaggerTags.COMMA)));
	}

	/**
	 * The element and the parent element to look for
	 * @param element
	 * @param elementName
	 * @return
	 */
	private static Element getParent(Element element, String elementName) {
		Node parent = element.getParent();
		while (parent != null && parent instanceof Element){
			Element parentEl = (Element)parent;
			if (parentEl.getLocalName().equals(elementName)){
				return parentEl;
			}
			parent = parent.getParent();
		}
		return null;
	}

	private static boolean precededByIn(Element chemicalEl) {
		Element previous = Utils.getPreviousElement(chemicalEl);
		if (previous != null && previous.getLocalName().equals(ChemicalTaggerTags.IN_IN)){
			return true;
		}
		return false;
	}
	
	private static boolean precededByInAMixtureOf(Element chemicalEl) {
		Element previous = Utils.getPreviousElement(chemicalEl);
		if (previous != null && previous.getLocalName().equals(ChemicalTaggerTags.IN_OF)){
			previous = Utils.getPreviousElement(previous);
			if (previous != null && previous.getValue().equalsIgnoreCase("mixture")){
				previous = Utils.getPreviousElement(previous);
				if (previous != null && previous.getValue().equalsIgnoreCase("a")){
					previous = Utils.getPreviousElement(previous);
					if (previous != null && previous.getLocalName().equals(ChemicalTaggerTags.IN_IN)){
						return true;
					}
				}
			}
		}
		return false;
	}

}

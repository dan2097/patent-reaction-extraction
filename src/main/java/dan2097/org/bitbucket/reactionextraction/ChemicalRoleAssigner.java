package dan2097.org.bitbucket.reactionextraction;

import nu.xom.Element;
import nu.xom.Node;

import org.apache.log4j.Logger;

import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.Utils;

public class ChemicalRoleAssigner {

	private static Logger LOG = Logger.getLogger(ChemicalRoleAssigner.class);
	private static AprioriKnowledge chemicalKnowledge = AprioriKnowledge.getInstance();

	static void assignRoleToChemical(Element chemicalEl, Chemical chemical) {
		String lcName = chemical.getName().toLowerCase(); 
		if (chemical.getEntityType()== ChemicalType.falsePositive){
			LOG.trace(chemical.getName() +" is believed to be a false positive and has been ignored");
		}
		else if (chemical.getRole() !=null){//role already has been explicitly assigned
			return;
		}
		else if (chemicalKnowledge.isKnownCatalystInChI(chemical.getInchi())){
			chemical.setRole(ChemicalRole.catalyst);
		}
		else if (isKnownTrivialCatalyst(lcName)){
			chemical.setRole(ChemicalRole.catalyst);
		}
		else if (ChemicalTaggerAtrs.CATALYST_ROLE_VAL.equals(chemicalEl.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
			chemical.setRole(ChemicalRole.catalyst);
		}
		else if (chemical.hasAmountOrEquivalentsOrYield()){
			chemical.setRole(ChemicalRole.reactant);
		}
		else if (ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(chemicalEl.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
			chemical.setRole(ChemicalRole.solvent);
		}
		else if (assignAsSolventDueToInKeyword(chemicalEl, chemical) ){
			chemical.setRole(ChemicalRole.solvent);
		}
		else if (chemicalKnowledge.getSolventNames().contains(lcName)){
			chemical.setRole(ChemicalRole.solvent);
		}
		else {
			chemical.setRole(ChemicalRole.reactant);
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
		if (chemicalEl.getLocalName().equals(ChemicalTaggerTags.UNNAMEDMOLECULE_Container) && chemical.getVolumeValue()==null){
			return false;
		}
		if (precededByIn(chemicalEl)){
			return true;
		}
		if (followedByStop(chemicalEl) && precededByInChemicalAnd(chemicalEl)){
			return true;
		}
		return false;
	}

	private static boolean followedByStop(Element chemicalEl) {
		Element next = Utils.getNextElement(chemicalEl);
		return (next != null && next.getLocalName().equals(ChemicalTaggerTags.STOP));
	}

	private static boolean precededByInChemicalAnd(Element chemicalEl) {
		Element previous = Utils.getPreviousElement(chemicalEl);
		if (previous == null || !previous.getLocalName().equals(ChemicalTaggerTags.CC) || !previous.getValue().equalsIgnoreCase("and")){
			return false;
		}
		Element twoBefore = Utils.getPreviousElement(previous);
		if (twoBefore!=null){
			Element molecule = getParent(twoBefore, ChemicalTaggerTags.MOLECULE_Container);
			if (molecule ==null){
				 molecule = getParent(twoBefore, ChemicalTaggerTags.UNNAMEDMOLECULE_Container);
			}
			if (molecule !=null && precededByIn(molecule)){
				return true;
			}
		}
		return false;
	}

	/**
	 * The element and the parent element to look for
	 * @param element
	 * @param elementName
	 * @return
	 */
	private static Element getParent(Element element, String elementName) {
		Node parent = element.getParent();
		while (parent !=null && parent instanceof Element){
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

}

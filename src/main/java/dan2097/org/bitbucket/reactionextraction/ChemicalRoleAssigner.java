package dan2097.org.bitbucket.reactionextraction;

import org.apache.log4j.Logger;

import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import nu.xom.Element;

public class ChemicalRoleAssigner {

	private static Logger LOG = Logger.getLogger(ChemicalRoleAssigner.class);
	private static String dmapInChI ="InChI=1/C7H10N2/c1-9(2)7-3-5-8-6-4-7/h3-6H,1-2H3";

	static void assignRoleToChemical(Element chemicalEl, Chemical chemical) {
		if (chemical.getType()== ChemicalType.falsePositive){
			LOG.trace(chemical.getName() +" is believed to be a false positive and has been ignored");
		}
		else if (ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(chemicalEl.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
			chemical.setRole(ChemicalRole.solvent);
		}
		else if (dmapInChI.equals(chemical.getInchi())){
			chemical.setRole(ChemicalRole.catalyst);
		}
		else if (ChemicalTaggerAtrs.CATALYST_ROLE_VAL.equals(chemicalEl.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
			chemical.setRole(ChemicalRole.catalyst);
		}
		else if (chemical.getVolumeValue()!=null && chemical.getAmountValue()==null && chemical.hasImpreciseVolume()){
			//solvents will be liquids but typically with imprecise volume and no amount given
			chemical.setRole(ChemicalRole.solvent);
		}
		else {
			chemical.setRole(ChemicalRole.reactant);
		}
	}

}

package dan2097.org.bitbucket.reactionextraction;

import org.apache.log4j.Logger;

import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import nu.xom.Element;

public class ChemicalRoleAssigner {

	private static Logger LOG = Logger.getLogger(ChemicalRoleAssigner.class);

	static void assignRoleToChemical(Element chemicalEl, Chemical chemical) {
		if (chemical.getType()== ChemicalType.falsePositive){
			LOG.trace(chemical.getName() +" is believed to be a false positive and has been ignored");
		}
		else if (ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(chemicalEl.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
			chemical.setRole(ChemicalRole.solvent);
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

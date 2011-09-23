package dan2097.org.bitbucket.reactionextraction;

import java.util.List;

import dan2097.org.bitbucket.inchiTools.InchiDemerger;

public class ReactionExtractionMethods {
	private static AprioriKnowledge chemicalKnowledge = AprioriKnowledge.getInstance();
	/**
	 * Is the string equal in meaning to "step"
	 * e.g. "step", "stage"
	 * @param str
	 * @return
	 */
	public static boolean isSynonymnOfStep(String str) {
		return str.equalsIgnoreCase("step") || str.equalsIgnoreCase("stage");
	}

	/**
	 * Does the inchi of this chemical correspond to a known solvent(s)?
	 * @param chem
	 * @return
	 */
	public static boolean isKnownSolvent(Chemical chem) {
		if (chem.getInchi() !=null){
			List<String> inchis = new InchiDemerger(chem.getInchi()).generateDemergedInchis();
			for (String inchi : inchis) {
				if (!chemicalKnowledge.isKnownSolventInChI(inchi)){
					return false;
				}
			}
			return true;
		}
		return false;
	}

}

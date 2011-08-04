package dan2097.org.bitbucket.reactionextraction;

import java.util.HashSet;
import java.util.Set;

import dan2097.org.bitbucket.utility.InchiNormaliser;
import dan2097.org.bitbucket.utility.Utils;

public class AprioriKnowledge {

	static String KNOWN_SOLVENTS_LOCATION = "/dan2097/org/bitbucket/reactionextraction/knownSolventInChIs.txt";
	static String KNOWN_CATALYSTS_LOCATION = "/dan2097/org/bitbucket/reactionextraction/knownCatalystInChIs.txt";
	private final Set<String> solventInChIs = new HashSet<String>();
	private final Set<String> catalystInChIs = new HashSet<String>();
	
	private AprioriKnowledge() {
		Set<String> solventInchis = Utils.fileToStringSet(KNOWN_SOLVENTS_LOCATION);
		for (String inchi : solventInchis) {
			solventInChIs.add(InchiNormaliser.normaliseInChI(inchi));
		}
		Set<String> catalystInchis = Utils.fileToStringSet(KNOWN_CATALYSTS_LOCATION);
		for (String inchi : catalystInchis) {
			catalystInChIs.add(InchiNormaliser.normaliseInChI(inchi));
		}
	}
	 
	private static class SingletonHolder { 
		public static final AprioriKnowledge INSTANCE = new AprioriKnowledge();
	}
 
	public static AprioriKnowledge getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * Returns the set of known solvent InChIs
	 * @return
	 */
	public Set<String> getSolventInChIs() {
		return solventInChIs;
	}

	/**
	 * Convenience method for checking whether solventInChIs contains the given InChI
	 * @param inchi
	 * @return
	 */
	public boolean isKnownSolventInChI(String inchi) {
		return solventInChIs.contains(inchi);
	}
	
	/**
	 * Returns the set of known catalyst InChIs
	 * @return
	 */
	public Set<String> getCatalystInChIs() {
		return catalystInChIs;
	}

	/**
	 * Convenience method for checking whether catalystInChIs contains the given InChI
	 * @param inchi
	 * @return
	 */
	public boolean isKnownCatalystInChI(String inchi) {
		return catalystInChIs.contains(inchi);
	}
}

package dan2097.org.bitbucket.reactionextraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import dan2097.org.bitbucket.utility.InchiNormaliser;

public class AprioriKnowledge {

	static String KNOWN_SOLVENTS_LOCATION = "/dan2097/org/bitbucket/reactionextraction/knownSolventInChIs.txt";
	static String KNOWN_CATALYSTS_LOCATION = "/dan2097/org/bitbucket/reactionextraction/knownCatalystInChIs.txt";
	private final Set<String> solventInChIs = new HashSet<String>();
	private final Set<String> catalystInChIs = new HashSet<String>();

	private final Pattern matchTab = Pattern.compile("\\t");
	
	private AprioriKnowledge() {
		populateInChISet(solventInChIs, KNOWN_SOLVENTS_LOCATION);
		populateInChISet(catalystInChIs, KNOWN_CATALYSTS_LOCATION);
	}

	private void populateInChISet(Set<String> set, String fileLocation) {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(fileLocation);
		if (is ==null){
			throw new RuntimeException("Failed to read " +fileLocation);
		}
		try{
			List<String> lines = IOUtils.readLines(is);
			for (String line : lines) {
				if (line.startsWith("#") || line.equals("")){
					continue;
				}
				set.add(InchiNormaliser.normaliseInChI(matchTab.split(line)[0]));
			}
		}
		catch (IOException e ) {
			throw new RuntimeException("Failed to read " +fileLocation, e);
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

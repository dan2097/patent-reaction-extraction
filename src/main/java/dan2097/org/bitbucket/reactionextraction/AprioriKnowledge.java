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
	private final Set<String> solventInChIs = new HashSet<String>();

	private final Pattern matchTab = Pattern.compile("\\t");
	
	public AprioriKnowledge() {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(KNOWN_SOLVENTS_LOCATION);
		if (is ==null){
			throw new RuntimeException("Failed to read " +KNOWN_SOLVENTS_LOCATION);
		}
		try{
			List<String> lines = IOUtils.readLines(is);
			for (String line : lines) {
				if (line.startsWith("#") || line.equals("")){
					continue;
				}
				solventInChIs.add(InchiNormaliser.normaliseInChI(matchTab.split(line)[0]));
			}
		}
		catch (IOException e ) {
			throw new RuntimeException("Failed to read " +KNOWN_SOLVENTS_LOCATION, e);
		}
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
}

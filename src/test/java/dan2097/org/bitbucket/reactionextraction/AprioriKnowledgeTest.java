package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import dan2097.org.bitbucket.utility.InchiNormaliser;

public class AprioriKnowledgeTest {

	@Test
	public void solventsFileContentTest() throws IOException {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(AprioriKnowledge.KNOWN_SOLVENTS_LOCATION);
		assertNotNull(is);
		Set<String> solventInChIs = new HashSet<String>();
		List<String> lines = IOUtils.readLines(is);
		for (String line : lines) {
			if (line.startsWith("#") || line.equals("")){
				continue;
			}
			String solventInChI = InchiNormaliser.normaliseInChI(line.split("\\t")[0]);
			assertFalse(solventInChI + " appears multiple times", solventInChIs.contains(solventInChI));
			solventInChIs.add(solventInChI);
		}
	}
	
	@Test
	public void catalystFileContentTest() throws IOException {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(AprioriKnowledge.KNOWN_CATALYSTS_LOCATION);
		assertNotNull(is);
		Set<String> catalystInChIs = new HashSet<String>();
		List<String> lines = IOUtils.readLines(is);
		for (String line : lines) {
			if (line.startsWith("#") || line.equals("")){
				continue;
			}
			String catalystInChI = InchiNormaliser.normaliseInChI(line.split("\\t")[0]);
			assertFalse(catalystInChI + " appears multiple times", catalystInChIs.contains(catalystInChI));
			catalystInChIs.add(catalystInChI);
		}
	}
}

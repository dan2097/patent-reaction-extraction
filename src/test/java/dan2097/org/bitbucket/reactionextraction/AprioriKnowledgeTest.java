package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import dan2097.org.bitbucket.inchiTools.InchiNormaliser;

public class AprioriKnowledgeTest {

	@Test
	public void solventsFileContentTest() throws IOException {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(AprioriKnowledge.KNOWN_SOLVENTSINCHIS_LOCATION);
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
	public void solventsNamesFileContentTest() throws IOException {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(AprioriKnowledge.KNOWN_SOLVENTNAMES_LOCATION);
		assertNotNull(is);
		Set<String> solventNames = new HashSet<String>();
		List<String> lines = IOUtils.readLines(is);
		for (String line : lines) {
			if (line.startsWith("#") || line.equals("")){
				continue;
			}
			assertEquals("Solvent name should be lower case: " + line, true, line.toLowerCase().equals(line));
			assertFalse(line + " appears multiple times", solventNames.contains(line));
			solventNames.add(line);
		}
	}
	
	@Test
	public void catalystInChIsFileContentTest() throws IOException {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(AprioriKnowledge.KNOWN_CATALYSTSINCHIS_LOCATION);
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

	@Test
	public void catalystNamesFileContentTest() throws IOException {
		InputStream is = AprioriKnowledge.class.getResourceAsStream(AprioriKnowledge.KNOWN_CATALYSTSNAMES_LOCATION);
		assertNotNull(is);
		Set<String> catalystNames = new HashSet<String>();
		List<String> lines = IOUtils.readLines(is);
		for (String line : lines) {
			if (line.startsWith("#") || line.equals("")){
				continue;
			}
			assertEquals("Catalyst name should be lower case: " + line, true, line.toLowerCase().equals(line));
			assertFalse(line + " appears multiple times", catalystNames.contains(line));
			catalystNames.add(line);
		}
	}
}

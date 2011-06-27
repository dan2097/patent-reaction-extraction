package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nu.xom.Builder;
import nu.xom.Element;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import dan2097.org.bitbucket.utility.Utils;

import static org.mockito.Mockito.mock;

public class SynonymRecognitionTest {

	private static Builder xomBuilder;
	
	@BeforeClass
	static public void setUp(){
		xomBuilder = new Builder();
	}
	
	@AfterClass
	static public void cleanUp(){
		xomBuilder = null;
	}
	
	@Test
	public void simpleSynonymTest() {
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Element moleculeEl = stringToXom("<MOLECULE>" +
				"<OSCARCM><OSCAR-CM>LITDIPA</OSCAR-CM></OSCARCM>" +
				"<OSCARCM><_-LRB->(</_-LRB-><OSCAR-CM>Lithium</OSCAR-CM><OSCAR-CM>diisopropylamide</OSCAR-CM><_-RRB->)</_-RRB-></OSCARCM>" +
				"</MOLECULE>");
		//LITDIPA is a made up acronym
		String resolvableName = "Lithium diisopropylamide";
		String smiles = Utils.resolveNameToSmiles(resolvableName);
		assertNotNull(resolvableName +" should be resolvable", smiles);
		Map<String, Chemical> foundAliases = parser.findAliasDefinitions(moleculeEl, ChemicalType.exact);
		assertEquals(1, foundAliases.size());
		Entry<String, Chemical> entry = foundAliases.entrySet().iterator().next();
		assertEquals("LITDIPA", entry.getKey());
		assertEquals(smiles, entry.getValue().getSmiles());
	}
	
	@Test
	public void synonymWithQuantityTest() {
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Element moleculeEl = stringToXom("<MOLECULE>" +
				"<OSCARCM><OSCAR-CM>LITDIPA</OSCAR-CM></OSCARCM>" +
				"<MIXTURE>" +
					"<_-LRB->(</_-LRB->" +
						"<OSCARCM><OSCAR-CM>Lithium diisopropylamide</OSCAR-CM></OSCARCM>" +
						"<COMMA>,</COMMA>" +
						"<QUANTITY><MASS><CD>30</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY>" +
					"<_-RRB->)</_-RRB->" +
				"</MIXTURE>" +
				"</MOLECULE>");
		String resolvableName = "Lithium diisopropylamide";
		String smiles = Utils.resolveNameToSmiles(resolvableName);
		assertNotNull(resolvableName +" should be resolvable", smiles);
		Map<String, Chemical> foundAliases = parser.findAliasDefinitions(moleculeEl, ChemicalType.exact);
		assertEquals(1, foundAliases.size());
		Entry<String, Chemical> entry = foundAliases.entrySet().iterator().next();
		assertEquals("LITDIPA", entry.getKey());
		assertEquals(smiles, entry.getValue().getSmiles());
	}
	
	@Test
	public void notASynonymTest() {
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Element moleculeEl = stringToXom("<MOLECULE>" +
				"<OSCARCM><OSCAR-CM>silver</OSCAR-CM></OSCARCM>" +
				"<JJ>modified</JJ>" +
				"<OSCARCM><OSCAR-CM>alumina</OSCAR-CM></OSCARCM>" +
				"</MOLECULE>");
		String resolvableName = "silver";
		String smiles = Utils.resolveNameToSmiles(resolvableName);
		assertNotNull(resolvableName +" should be resolvable", smiles);
		Map<String, Chemical> foundAliases = parser.findAliasDefinitions(moleculeEl , ChemicalType.exact);
		assertEquals(0, foundAliases.size());
	}
	
	@Test
	public void notASynonymTest2() {
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Element moleculeEl = stringToXom("<MOLECULE>" +
				"<OSCARCM><OSCAR-CM>NMR</OSCAR-CM></OSCARCM>" +
				"<OSCARCM><_-LRB->(</_-LRB-><OSCAR-CM>dimethyl sulfoxide</OSCAR-CM><_-RRB->)</_-RRB-></OSCARCM>" +
				"</MOLECULE>");
		String resolvableName = "dimethyl sulfoxide";
		String smiles = Utils.resolveNameToSmiles(resolvableName);
		assertNotNull(resolvableName +" should be resolvable", smiles);
		Map<String, Chemical> foundAliases = parser.findAliasDefinitions(moleculeEl, ChemicalType.falsePositive);
		assertEquals(0, foundAliases.size());
	}

	private Element stringToXom(String stringXML) {
		try{
			return xomBuilder.build(stringXML, "").getRootElement();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

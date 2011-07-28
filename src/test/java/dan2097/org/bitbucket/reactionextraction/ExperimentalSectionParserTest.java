package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import nu.xom.Builder;
import nu.xom.Element;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ExperimentalSectionParserTest {
	
	private static Builder xomBuilder;
	private ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
	
	@BeforeClass
	static public void setUp(){
		xomBuilder = new Builder();
	}
	
	@AfterClass
	static public void cleanUp(){
		xomBuilder = null;
	}
	
	@Test
	public void testNameExtractionFromOscarCMWith1Child(){
		Element oscarCM = stringToXom(
				"<OSCARCM>" +
					"<OSCAR-CM>pyridine</OSCAR-CM>" +
				"</OSCARCM>");
		assertEquals("pyridine", parser.findMoleculeNameFromOscarCM(oscarCM));
	}
	
	@Test
	public void testNameExtractionFromOscarCMWith2Children(){
		Element oscarCM = stringToXom(
				"<OSCARCM>" +
					"<OSCAR-CM>ethyl</OSCAR-CM>" +
					"<OSCAR-CM>acetate</OSCAR-CM>" +
				"</OSCARCM>");
		assertEquals("ethyl acetate", parser.findMoleculeNameFromOscarCM(oscarCM));
	}
	
	@Test
	public void testNameExtractionFromOscarCMWithDashedChildren(){
		Element oscarCM = stringToXom(
				"<OSCARCM>" +
					"<OSCAR-CM>water</OSCAR-CM>" +
					"<DASH>-</DASH>" +
					"<OSCAR-CM>ethyl</OSCAR-CM>" +
					"<OSCAR-CM>acetate</OSCAR-CM>" +
				"</OSCARCM>");
		assertEquals("water ethyl acetate", parser.findMoleculeNameFromOscarCM(oscarCM));
	}
	
	@Test
	public void testNameExtractionFromOscarCMWithBrackettedChild(){
		Element oscarCM = stringToXom(
				"<OSCARCM>" +
					"<_-LRB->(</_-LRB->" +
					"<OSCAR-CM>cholesterol</OSCAR-CM>" +
					"<_-RRB->)</_-RRB->" +
				"</OSCARCM>");
		assertEquals("cholesterol", parser.findMoleculeNameFromOscarCM(oscarCM));
	}
	@Test
	public void testNameExtractionFromUnnamedMolecule1(){
		Element unnamedMoleculeEl = stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<JJ-COMPOUND>resultant</JJ-COMPOUND>" +
					"<NN-CHEMENTITY>precipitate</NN-CHEMENTITY>" +
					"<REFERENCETOCOMPOUND><CD-ALPHANUM>3a</CD-ALPHANUM></REFERENCETOCOMPOUND>" +
					"<QUANTITY><_-LRB->(</_-LRB-><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS><_-RRB->)</_-RRB-></QUANTITY>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("resultant precipitate 3a", parser.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}

	@Test
	public void testNameExtractionFromUnnnamedMolecule2(){
		Element unnamedMoleculeEl = stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<QUANTITY><MASS><CD>30</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY>" +
					"<IN-OF>of</IN-OF>" +
					"<NN-CHEMENTITY>compound</NN-CHEMENTITY>" +
					"<REFERENCETOCOMPOUND><CD>5</CD></REFERENCETOCOMPOUND>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("compound 5", parser.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule3(){
		Element unnamedMoleculeEl = stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<REFERENCETOCOMPOUND><CD-ALPHANUM>3a</CD-ALPHANUM></REFERENCETOCOMPOUND>" +
					"<MIXTURE><_-LRB->(</_-LRB-><FW>Sigma-Aldrich</FW><COMMA>,</COMMA><QUANTITY><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY><_-RRB->)</_-RRB-></MIXTURE>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("3a", parser.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}

	@Test
	public void testNameExtractionFromUnnamedMolecule4(){
		Element unnamedMoleculeEl = stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<JJ-COMPOUND>title</JJ-COMPOUND>" +
					"<NN-CHEMENTITY>compound</NN-CHEMENTITY>" +
					"<IN-AS>as</IN-AS>" +
					"<DT>an</DT>" +
					"<JJ-CHEM>off-white</JJ-CHEM>" +
					"<NN-STATE>solid</NN-STATE>" +
					"<QUANTITY><_-LRB->(</_-LRB-><MASS><CD>6.30</CD><NN-MASS>g</NN-MASS></MASS><COMMA>,</COMMA><PERCENT><CD>71</CD><NN-PERCENT>%</NN-PERCENT></PERCENT><_-RRB->)</_-RRB-></QUANTITY>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("title compound", parser.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule5(){
		Element unnamedMoleculeEl = stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<NN-CHEMENTITY>Compound</NN-CHEMENTITY>" +
					"<REFERENCETOCOMPOUND><NN-IDENTIFIER>III</NN-IDENTIFIER></REFERENCETOCOMPOUND>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("Compound III", parser.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule6(){
		Element unnamedMoleculeEl = stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<QUANTITY><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY>" +
					"<NN-CHEMENTITY>resin</NN-CHEMENTITY>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("resin", parser.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule7(){
		Element unnamedMoleculeEl = stringToXom(
				"<UNNAMEDMOLECULE>" +
				"<QUANTITY><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY>" +
				"<IN-OF>of</IN-OF>" +
				"<NN>fazazzale</NN>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("fazazzale", parser.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound1(){
		Element reference = stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<CD>1</CD>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("1", parser.getIdentifierFromReference(reference));
	}

	@Test
	public void testGetIdentifierFromReferenceToCompound2(){
		Element reference = stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<_-LRB->(</_-LRB->" +
					"<CD>1</CD>" +
					"<_-RRB->)</_-RRB->" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("1", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound3(){
		Element reference = stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<NN-IDENTIFIER>IX</NN-IDENTIFIER>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("IX", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound4(){
		Element reference = stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<LSQB>[</LSQB>" +
					"<CD-ALPHANUM>7a</CD-ALPHANUM>" +
					"<RSQB>]</RSQB>"+
				"</REFERENCETOCOMPOUND>");
		assertEquals("7a", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound5(){
		Element reference = stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<CD>3</CD>" +
					"<NN-IDENTIFIER>III</NN-IDENTIFIER>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("3 III", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound6(){
		Element reference = stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<NN-EXAMPLE>example</NN-EXAMPLE>" +
					"<CD>5</CD>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("5", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testPercentAsAPercentYield(){
		Element reference = stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<JJ-COMPOUND>title</JJ-COMPOUND>" +
					"<NN-CHEMENTITY>compound</NN-CHEMENTITY>" +
					"<QUANTITY>" +
						"<_-LRB->(</_-LRB->" +
						"<MASS>" +
							"<CD>21.3</CD>" +
							"<NN-MASS>g</NN-MASS>" +
						"</MASS>" +
						"<COMMA>,</COMMA>" +
						"<PERCENT>" +
							"<CD>83</CD>" +
							"<NN-PERCENT>%</NN-PERCENT>" +
						"</PERCENT>" +
						"<_-RRB->)</_-RRB->" +
					"</QUANTITY>" +
				"</UNNAMEDMOLECULE>");
		Chemical chem = new Chemical("title compound");
		assertEquals(null, chem.getPercentYield());
		parser.interpretPercentAsAyield(reference, chem);
		assertEquals(83, chem.getPercentYield(), 0.5);
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

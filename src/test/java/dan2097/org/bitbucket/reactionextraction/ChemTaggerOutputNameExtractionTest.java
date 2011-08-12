package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.assertEquals;
import nu.xom.Element;

import org.junit.Test;

public class ChemTaggerOutputNameExtractionTest {

	
	@Test
	public void testNameExtractionFromOscarCMWith1Child(){
		Element oscarCM = TestUtils.stringToXom(
				"<OSCARCM>" +
					"<OSCAR-CM>pyridine</OSCAR-CM>" +
				"</OSCARCM>");
		assertEquals("pyridine", ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(oscarCM));
	}
	
	@Test
	public void testNameExtractionFromOscarCMWith2Children(){
		Element oscarCM = TestUtils.stringToXom(
				"<OSCARCM>" +
					"<OSCAR-CM>ethyl</OSCAR-CM>" +
					"<OSCAR-CM>acetate</OSCAR-CM>" +
				"</OSCARCM>");
		assertEquals("ethyl acetate", ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(oscarCM));
	}
	
	@Test
	public void testNameExtractionFromOscarCMWithDashedChildren(){
		Element oscarCM = TestUtils.stringToXom(
				"<OSCARCM>" +
					"<OSCAR-CM>water</OSCAR-CM>" +
					"<DASH>-</DASH>" +
					"<OSCAR-CM>ethyl</OSCAR-CM>" +
					"<OSCAR-CM>acetate</OSCAR-CM>" +
				"</OSCARCM>");
		assertEquals("water ethyl acetate", ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(oscarCM));
	}
	
	@Test
	public void testNameExtractionFromOscarCMWithBrackettedChild(){
		Element oscarCM = TestUtils.stringToXom(
				"<OSCARCM>" +
					"<_-LRB->(</_-LRB->" +
					"<OSCAR-CM>cholesterol</OSCAR-CM>" +
					"<_-RRB->)</_-RRB->" +
				"</OSCARCM>");
		assertEquals("cholesterol", ChemTaggerOutputNameExtraction.findMoleculeNameFromOscarCM(oscarCM));
	}
	@Test
	public void testNameExtractionFromUnnamedMolecule1(){
		Element unnamedMoleculeEl = TestUtils.stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<JJ-COMPOUND>resultant</JJ-COMPOUND>" +
					"<NN-CHEMENTITY>precipitate</NN-CHEMENTITY>" +
					"<REFERENCETOCOMPOUND><CD-ALPHANUM>3a</CD-ALPHANUM></REFERENCETOCOMPOUND>" +
					"<QUANTITY><_-LRB->(</_-LRB-><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS><_-RRB->)</_-RRB-></QUANTITY>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("resultant precipitate 3a", ChemTaggerOutputNameExtraction.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}

	@Test
	public void testNameExtractionFromUnnnamedMolecule2(){
		Element unnamedMoleculeEl = TestUtils.stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<QUANTITY><MASS><CD>30</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY>" +
					"<IN-OF>of</IN-OF>" +
					"<NN-CHEMENTITY>compound</NN-CHEMENTITY>" +
					"<REFERENCETOCOMPOUND><CD>5</CD></REFERENCETOCOMPOUND>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("compound 5", ChemTaggerOutputNameExtraction.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule3(){
		Element unnamedMoleculeEl = TestUtils.stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<REFERENCETOCOMPOUND><CD-ALPHANUM>3a</CD-ALPHANUM></REFERENCETOCOMPOUND>" +
					"<MIXTURE><_-LRB->(</_-LRB-><FW>Sigma-Aldrich</FW><COMMA>,</COMMA><QUANTITY><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY><_-RRB->)</_-RRB-></MIXTURE>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("3a", ChemTaggerOutputNameExtraction.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}

	@Test
	public void testNameExtractionFromUnnamedMolecule4(){
		Element unnamedMoleculeEl = TestUtils.stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<JJ-COMPOUND>title</JJ-COMPOUND>" +
					"<NN-CHEMENTITY>compound</NN-CHEMENTITY>" +
					"<IN-AS>as</IN-AS>" +
					"<DT>an</DT>" +
					"<JJ-CHEM>off-white</JJ-CHEM>" +
					"<NN-STATE>solid</NN-STATE>" +
					"<QUANTITY><_-LRB->(</_-LRB-><MASS><CD>6.30</CD><NN-MASS>g</NN-MASS></MASS><COMMA>,</COMMA><PERCENT><CD>71</CD><NN-PERCENT>%</NN-PERCENT></PERCENT><_-RRB->)</_-RRB-></QUANTITY>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("title compound", ChemTaggerOutputNameExtraction.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule5(){
		Element unnamedMoleculeEl = TestUtils.stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<NN-CHEMENTITY>Compound</NN-CHEMENTITY>" +
					"<REFERENCETOCOMPOUND><NN-IDENTIFIER>III</NN-IDENTIFIER></REFERENCETOCOMPOUND>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("Compound III", ChemTaggerOutputNameExtraction.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule6(){
		Element unnamedMoleculeEl = TestUtils.stringToXom(
				"<UNNAMEDMOLECULE>" +
					"<QUANTITY><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY>" +
					"<NN-CHEMENTITY>resin</NN-CHEMENTITY>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("resin", ChemTaggerOutputNameExtraction.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
	
	@Test
	public void testNameExtractionFromUnnamedMolecule7(){
		Element unnamedMoleculeEl = TestUtils.stringToXom(
				"<UNNAMEDMOLECULE>" +
				"<QUANTITY><MASS><CD>50</CD><NN-MASS>g</NN-MASS></MASS></QUANTITY>" +
				"<IN-OF>of</IN-OF>" +
				"<NN>fazazzale</NN>" +
				"</UNNAMEDMOLECULE>");
		assertEquals("fazazzale", ChemTaggerOutputNameExtraction.findMoleculeNameFromUnnamedMoleculeEl(unnamedMoleculeEl));
	}
}

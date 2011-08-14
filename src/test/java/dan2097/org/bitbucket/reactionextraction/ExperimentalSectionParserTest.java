package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;

import nu.xom.Element;

import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ExperimentalSectionParserTest {

	private ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(ExperimentalSection.class), new HashMap<String, Chemical>());
	
	@Test
	public void testGetIdentifierFromReferenceToCompound1(){
		Element reference = TestUtils.stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<CD>1</CD>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("1", parser.getIdentifierFromReference(reference));
	}

	@Test
	public void testGetIdentifierFromReferenceToCompound2(){
		Element reference = TestUtils.stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<_-LRB->(</_-LRB->" +
					"<CD>1</CD>" +
					"<_-RRB->)</_-RRB->" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("1", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound3(){
		Element reference = TestUtils.stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<NN-IDENTIFIER>IX</NN-IDENTIFIER>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("IX", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound4(){
		Element reference = TestUtils.stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<LSQB>[</LSQB>" +
					"<CD-ALPHANUM>7a</CD-ALPHANUM>" +
					"<RSQB>]</RSQB>"+
				"</REFERENCETOCOMPOUND>");
		assertEquals("7a", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound5(){
		Element reference = TestUtils.stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<CD>3</CD>" +
					"<NN-IDENTIFIER>III</NN-IDENTIFIER>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("3 III", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testGetIdentifierFromReferenceToCompound6(){
		Element reference = TestUtils.stringToXom(
				"<REFERENCETOCOMPOUND>" +
					"<NN-EXAMPLE>example</NN-EXAMPLE>" +
					"<CD>5</CD>" +
				"</REFERENCETOCOMPOUND>");
		assertEquals("5", parser.getIdentifierFromReference(reference));
	}
	
	@Test
	public void testPercentAsAPercentYield(){
		Element reference = TestUtils.stringToXom(
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
}

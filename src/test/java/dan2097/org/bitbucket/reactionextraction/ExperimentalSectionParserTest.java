package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import nu.xom.Element;

import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ExperimentalSectionParserTest {

	private ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(ExperimentalSection.class), new PreviousReactionData());
	
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
	public void testGetSectionIdentifier1(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-METHOD>Method</NN-METHOD>" +
					"<CD>1</CD>" +
				"</PROCEDURE>");
		assertEquals("1", parser.getSectionIdentifier(procedureEl));
	}
	
	@Test
	public void testGetSectionIdentifier2(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-EXAMPLE>Example</NN-EXAMPLE>" +
					"<CD>3</CD>" +
					"<COLON>:</COLON>" +
					"<NN-METHOD>Step</NN-METHOD>" +
					"<CD>2</CD>" +
				"</PROCEDURE>");
		assertEquals(null, parser.getSectionIdentifier(procedureEl));
	}

	@Test
	public void testGetStepIdentifier1(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-METHOD>Step</NN-METHOD>" +
					"<CD>1</CD>" +
				"</PROCEDURE>");
		assertEquals("1", parser.getStepIdentifier(procedureEl, "2"));
	}

	@Test
	public void testGetStepIdentifier2(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-EXAMPLE>Example</NN-EXAMPLE>" +
					"<CD>3</CD>" +
					"<COLON>:</COLON>" +
					"<NN-METHOD>Step</NN-METHOD>" +
					"<CD>2</CD>" +
				"</PROCEDURE>");
		assertEquals(null, parser.getStepIdentifier(procedureEl, "2"));
	}

	@Test
	public void testGetStepIdentifier3(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-EXAMPLE>Example</NN-EXAMPLE>" +
					"<CD>3</CD>" +
					"<COLON>:</COLON>" +
					"<NN-METHOD>Step</NN-METHOD>" +
					"<CD>2</CD>" +
				"</PROCEDURE>");
		assertEquals("2", parser.getStepIdentifier(procedureEl, "3"));
	}
	
	@Test
	public void testGetStepAndSectionIdentifier1(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-EXAMPLE>Example</NN-EXAMPLE>" +
					"<CD>3</CD>" +
					"<COLON>:</COLON>" +
					"<NN-METHOD>Step</NN-METHOD>" +
					"<CD>2</CD>" +
				"</PROCEDURE>");
		SectionAndStepIdentifier sectionAndStepIdentifier = parser.getSectionAndStepIdentifier(procedureEl);
		assertNotNull(sectionAndStepIdentifier);
		assertEquals("3", sectionAndStepIdentifier.getSectionIdentifier());
		assertEquals("2", sectionAndStepIdentifier.getStepIdentifier());
	}
	
	@Test
	public void testGetStepAndSectionIdentifier2(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-EXAMPLE>Example</NN-EXAMPLE>" +
					"<CD>3</CD>" +
				"</PROCEDURE>");
		SectionAndStepIdentifier sectionAndStepIdentifier = parser.getSectionAndStepIdentifier(procedureEl);
		assertNotNull(sectionAndStepIdentifier);
		assertEquals("3", sectionAndStepIdentifier.getSectionIdentifier());
		assertEquals(null, sectionAndStepIdentifier.getStepIdentifier());
	}
	
	@Test
	public void testGetStepAndSectionIdentifier3(){
		Element procedureEl = TestUtils.stringToXom(
				"<PROCEDURE>" +
					"<NN-EXAMPLE>Example</NN-EXAMPLE>" +
					"<CD>4</CD>" +
					"<COLON>:</COLON>" +
					"<NN-METHOD>step</NN-METHOD>" +
					"<CD>3</CD>" +
					"<COLON>:</COLON>" +
					"<NN-METHOD>step</NN-METHOD>" +
					"<CD-ALPHANUM>2a</CD-ALPHANUM>" +
				"</PROCEDURE>");
		SectionAndStepIdentifier sectionAndStepIdentifier = parser.getSectionAndStepIdentifier(procedureEl);
		assertNull(sectionAndStepIdentifier);
	}
}

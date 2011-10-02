package dan2097.org.bitbucket.reactionextraction;
import static junit.framework.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;
import org.junit.Test;

import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLAtrs;
import dan2097.org.bitbucket.utility.XMLTags;

public class ExperimentalSectionsCreatorTest {

	@Test
	public void isHeadingTest() {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		assertEquals(true, sectionCreator.isHeading(new Element(XMLTags.HEADING)));
		assertEquals(false, sectionCreator.isHeading(new Element(XMLTags.P)));
		Element pHeading = new Element(XMLTags.P);
		pHeading.addAttribute(new Attribute("id", "h-5"));
		pHeading.appendChild("Step 1");
		assertEquals(true, sectionCreator.isHeading(pHeading));
	}
	
	@Test
	public void isSubHeading1() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE><NN-METHOD>Step</NN-METHOD><CD>5</CD></PROCEDURE></NounPhrase></Sentence></Document>");
		assertEquals(true, sectionCreator.isSubHeading(new Element(XMLTags.HEADING), taggedDoc.getRootElement()));
	}
	
	@Test
	public void isSubHeading2() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE><NN-METHOD>Example</NN-METHOD><CD>5</CD></PROCEDURE></NounPhrase></Sentence></Document>");
		assertEquals(false, sectionCreator.isSubHeading(new Element(XMLTags.HEADING), taggedDoc.getRootElement()));
	}
	
	@Test
	public void isSubHeading3() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE><NN-METHOD>Example</NN-METHOD><CD>5</CD></PROCEDURE></NounPhrase></Sentence></Document>");
		Element paragraph = new Element(XMLTags.P);
		paragraph.addAttribute(new Attribute(XMLAtrs.ID, "h-3"));
		assertEquals(true, sectionCreator.isSubHeading(paragraph, taggedDoc.getRootElement()));
	}
	
	@Test
	public void isSubHeading4() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE><CD>3</CD></PROCEDURE><_-RRB->)</_-RRB-></NounPhrase></Sentence></Document>");
		assertEquals(true, sectionCreator.isSubHeading(new Element(XMLTags.HEADING), taggedDoc.getRootElement()));
	}
	
	@Test
	public void extractHeadingCompoundName1(){
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		List<IdentifiedChemicalName> namesFound = sectionCreator.findCompoundNamesInHeading("pyridine");
		assertEquals(1, namesFound.size());
		assertEquals("pyridine", namesFound.get(0).getTextValue());
	}
	
	@Test
	public void extractHeadingCompoundName2(){
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		List<IdentifiedChemicalName> namesFound = sectionCreator.findCompoundNamesInHeading("ethanol condensation in silicon nanontubes");
		assertEquals(2, namesFound.size());
		assertEquals("ethanol", namesFound.get(0).getTextValue());
		assertEquals("silicon", namesFound.get(1).getTextValue());
	}
	
	@Test
	public void extractHeadingCompoundName3(){
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		List<IdentifiedChemicalName> namesFound = sectionCreator.findCompoundNamesInHeading("Benzene compound with toluene");
		assertEquals(1, namesFound.size());
		assertEquals("Benzene compound with toluene", namesFound.get(0).getTextValue());
	}
	
	@Test
	public void extractHeadingCompoundName4(){
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		List<IdentifiedChemicalName> namesFound = sectionCreator.findCompoundNamesInHeading("Example 1: 2-methylpyridine:");
		assertEquals(1, namesFound.size());
		assertEquals("2-methylpyridine", namesFound.get(0).getTextValue());
	}
	
	@Test
	public void isSelfStandingParagraphTest1() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><ActionPhrase type=\"Yield\"><NounPhrase><DT-THE>The</DT-THE><NN-CHEMENTITY>reaction</NN-CHEMENTITY></NounPhrase><VerbPhrase><VB-YIELD>afforded</VB-YIELD></VerbPhrase><NounPhrase><MOLECULE><OSCARCM><OSCAR-CM>1-(4-methoxybenzyl)piperidin-4-ol</OSCAR-CM></OSCARCM></MOLECULE></NounPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		assertEquals(true, sectionCreator.isSelfStandingParagraph(taggedDoc));
	}
	
	@Test
	public void isSelfStandingParagraphTest2() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><DT>Allowed</DT><NNS>substituents</NNS></NounPhrase><VerbPhrase><VBP>include</VBP></VerbPhrase><NounPhrase><MOLECULE><OSCARCM><OSCAR-CM>methyl</OSCAR-CM></OSCARCM></MOLECULE><CC>and</CC><MOLECULE><OSCARCM><OSCAR-CM>ethyl</OSCAR-CM></OSCARCM></MOLECULE></NounPhrase><STOP>.</STOP></Sentence></Document>");
		assertEquals(false, sectionCreator.isSelfStandingParagraph(taggedDoc));
	}
	
	@Test
	public void isSelfStandingParagraphTest3() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><ActionPhrase type=\"Yield\"><NounPhrase><DT-THE>The</DT-THE><NN-CHEMENTITY>reaction</NN-CHEMENTITY></NounPhrase><VerbPhrase><VB-YIELD>afforded</VB-YIELD></VerbPhrase><NounPhrase><DT-THE>the</DT-THE><UNNAMEDMOLECULE><JJ-COMPOUND>title</JJ-COMPOUND><NN-CHEMENTITY>compound</NN-CHEMENTITY></UNNAMEDMOLECULE></NounPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		assertEquals(false, sectionCreator.isSelfStandingParagraph(taggedDoc));//an anaphora is insufficient for this to be true
	}
	
	@Test
	public void hasHiddenHeading1() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE></PROCEDURE><MOLECULE></MOLECULE></NounPhrase><COLON>:</COLON></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}
	
	@Test
	public void hasHiddenHeading2() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><UNNAMEDMOLECULE><REFERENCETOCOMPOUND></REFERENCETOCOMPOUND></UNNAMEDMOLECULE><MOLECULE></MOLECULE></NounPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}
	
	@Test
	public void hasHiddenHeading3() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><MOLECULE></MOLECULE></NounPhrase><COLON>:</COLON></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}
	
	@Test
	public void hasHiddenHeading4() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE></PROCEDURE></NounPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}
	
	@Test
	public void hasHiddenHeading5CounterExample() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><UNNAMEDMOLECULE><NN-CHEMENTITY>Reagent</NN-CHEMENTITY><REFERENCETOCOMPOUND><CD>7</CD></REFERENCETOCOMPOUND></UNNAMEDMOLECULE><CC>and</CC><MOLECULE><OSCARCM><OSCAR-CM>dichloromethane</OSCAR-CM></OSCARCM></MOLECULE></NounPhrase></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNull(heading);
	}
	
	@Test
	public void hasHiddenHeading6() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><ActionPhrase type=\"Synthesize\"><NounPhrase><NN-SYNTHESIZE>Preparation</NN-SYNTHESIZE><PrepPhrase><IN-OF>of</IN-OF><NounPhrase><UNNAMEDMOLECULE><NN-CHEMENTITY>Compound</NN-CHEMENTITY><REFERENCETOCOMPOUND><CD-ALPHANUM>18(a)</CD-ALPHANUM></REFERENCETOCOMPOUND></UNNAMEDMOLECULE></NounPhrase></PrepPhrase></NounPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}
	
	@Test
	public void hasHiddenHeading7() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><ActionPhrase type=\"Synthesize\"><NounPhrase><PROCEDURE><NN-METHOD>Step</NN-METHOD><CD>1</CD></PROCEDURE></NounPhrase><COLON>:</COLON><NounPhrase><NN-SYNTHESIZE>Synthesis</NN-SYNTHESIZE><PrepPhrase><IN-OF>of</IN-OF><NounPhrase><MOLECULE><OSCARCM><OSCAR-CM>17,17-ethylenedioxyandrosta-1,4-dien-3-one</OSCAR-CM></OSCARCM><REFERENCETOCOMPOUND><_-LRB->(</_-LRB-><CD>2</CD><_-RRB->)</_-RRB-></REFERENCETOCOMPOUND></MOLECULE></NounPhrase></PrepPhrase></NounPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(4, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}
	
	@Test
	public void hasHiddenHeading8() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><ActionPhrase type=\"Synthesize\"><NounPhrase><PROCEDURE></PROCEDURE><NN-SYNTHESIZE>Synthesis</NN-SYNTHESIZE><PrepPhrase><IN-OF>of</IN-OF><NounPhrase><MOLECULE></MOLECULE></NounPhrase></PrepPhrase></NounPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}

	@Test
	public void hasHiddenHeading9() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE></PROCEDURE></NounPhrase><COLON>:</COLON><NounPhrase><MOLECULE></MOLECULE></NounPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(4, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}

	@Test
	public void hasHiddenHeading10() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><ActionPhrase type=\"Cool\"><NounPhrase><PROCEDURE><NN-METHOD>Step</NN-METHOD><CD-ALPHANUM>8F</CD-ALPHANUM></PROCEDURE></NounPhrase><COLON>:</COLON><PrepPhrase><TO>To</TO><NounPhrase><DT>a</DT><NN-CHEMENTITY>solution</NN-CHEMENTITY><PrepPhrase><IN-OF>of</IN-OF><NounPhrase></NounPhrase></PrepPhrase></NounPhrase></PrepPhrase><VerbPhrase><VB-COOL>cooled</VB-COOL></VerbPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(2, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
	}
	
	@Test
	public void hasHiddenHeading11() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE><NN-EXAMPLE>Example</NN-EXAMPLE><CD>3</CD></PROCEDURE></NounPhrase><STOP>.</STOP></Sentence><Sentence><NounPhrase></NounPhrase><STOP></STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(2, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
		assertEquals(2, taggedDoc.getRootElement().getChildElements("Sentence").size());
	}
	
	@Test
	public void hasHiddenHeading12() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE><CD>3</CD></PROCEDURE><_-RRB->)</_-RRB-></NounPhrase></Sentence><Sentence><ActionPhrase><NounPhrase><MOLECULE></MOLECULE></NounPhrase><VerbPhrase></VerbPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(1, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
		assertEquals(2, taggedDoc.getRootElement().getChildElements("Sentence").size());
	}

	@Test
	public void hasHiddenHeading13() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><PROCEDURE><_-LRB->(</_-LRB-><NN-IDENTIFIER>iii</NN-IDENTIFIER><_-RRB->)</_-RRB-></PROCEDURE></NounPhrase></Sentence><Sentence><ActionPhrase><NounPhrase><MOLECULE></MOLECULE></NounPhrase><VerbPhrase></VerbPhrase></ActionPhrase><STOP>.</STOP></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNotNull(heading);
		assertEquals(1, heading.getChildElements().size());
		assertEquals(0, taggedDoc.getRootElement().getFirstChildElement("Sentence").getChildElements().size());
		assertEquals(2, taggedDoc.getRootElement().getChildElements("Sentence").size());
	}
}

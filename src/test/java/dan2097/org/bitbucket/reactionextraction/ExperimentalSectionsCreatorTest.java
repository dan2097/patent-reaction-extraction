package dan2097.org.bitbucket.reactionextraction;
import static junit.framework.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Ignore;
import org.junit.Test;

import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLTags;

public class ExperimentalSectionsCreatorTest {

	@Test
	public void isHeadingTest() {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		assertEquals(true, sectionCreator.isHeading(new Element(XMLTags.HEADING)));
		assertEquals(false, sectionCreator.isHeading(new Element(XMLTags.P)));
		Element pHeading = new Element(XMLTags.P);
		pHeading.addAttribute(new Attribute("id", "h-5"));
		assertEquals(true, sectionCreator.isHeading(pHeading));
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
		assertEquals(2, heading.getChildElements().size());
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
	
	@Ignore
	@Test//chemical tagger should be fixed instead
	public void hasHiddenHeading10CounterExample() throws ValidityException, ParsingException, IOException {
		ExperimentalSectionsCreator sectionCreator = new ExperimentalSectionsCreator(new ArrayList<Element>());
		Document taggedDoc = Utils.buildXmlFromString("<Document><Sentence><NounPhrase><MOLECULE></MOLECULE></NounPhrase><STOP>;</STOP></Sentence><Sentence><NounPhrase><NN-CHEMENTITY>compound</NN-CHEMENTITY></NounPhrase><PrepPhrase><IN-WITH>with</IN-WITH><NounPhrase><MOLECULE></MOLECULE></NounPhrase></PrepPhrase></Sentence></Document>");
		Element heading = sectionCreator.findAndDetachHiddenHeadingContent(taggedDoc);
		assertNull(heading);
	}
}

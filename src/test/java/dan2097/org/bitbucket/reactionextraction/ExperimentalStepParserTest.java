package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dan2097.org.bitbucket.utility.ChemicalTaggerTags;

public class ExperimentalStepParserTest {

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
		ExperimentalStepParser.interpretPercentAsAyield(reference, chem);
		assertEquals(83, chem.getPercentYield(), 0.5);
	}
	
	@Test
	public void testIdentifyReplacedMolecule1(){
		Element paragraph = TestUtils.stringToXom(
			"<Sentence>" +
				"<VerbPhrase>" +
					"<VBG>replacing</VBG>" +
				"</VerbPhrase>" +
				"<NounPhrase>" +
					"<DT-THE>the</DT-THE>" +
					"<MOLECULE>" +
						"<OSCARCM>" +
							"<OSCAR-CM>cyclopropanoyl</OSCAR-CM>" +
							"<OSCAR-CM>chloride</OSCAR-CM>" +
						"</OSCARCM>" +
					"</MOLECULE>" +
				"</NounPhrase>" +
				"<PrepPhrase>" +
					"<IN-BY>by</IN-BY>" +
					"<NounPhrase>" +
						"<MOLECULE>" +
							"<OSCARCM>" +
								"<OSCAR-CM>acetyl</OSCAR-CM>" +
								"<OSCAR-CM>chloride</OSCAR-CM>" +
							"</OSCARCM>" +
						"</MOLECULE>" +
					"</NounPhrase>" +
				"</PrepPhrase>" +
			"</Sentence>");
		List<Element> molecules = XOMTools.getDescendantElementsWithTagName(paragraph, ChemicalTaggerTags.MOLECULE_Container);
		assertEquals("unit test is broken", 2, molecules.size());
		BiMap<Element, Chemical>  moleculeToChemicalMap = HashBiMap.create();
		Chemical chem1 = new Chemical("cyclopropanoyl chloride");
		Chemical chem2 = new Chemical("acetyl chloride");
		moleculeToChemicalMap.put(molecules.get(0), chem1);
		moleculeToChemicalMap.put(molecules.get(1), chem2);
		ExperimentalStepParser stepParser = new ExperimentalStepParser(mock(ExperimentalStep.class), moleculeToChemicalMap, mock(Chemical.class),  mock(Chemical.class));
		stepParser.markReplacedChemicalsAsFalsePositives(paragraph.getDocument());
		assertEquals(ChemicalType.falsePositive, chem1.getEntityType());
		assertEquals(null, chem2.getEntityType());
	}
	
	@Test
	public void testIdentifyReplacedMolecule2(){
		Element paragraph = TestUtils.stringToXom(
			"<Sentence>" +
				"<NounPhrase>" +
					"<MOLECULE>" +
						"<OSCARCM>" +
							"<OSCAR-CM>acetyl</OSCAR-CM>" +
							"<OSCAR-CM>chloride</OSCAR-CM>" +
						"</OSCARCM>" +
					"</MOLECULE>" +
				"</NounPhrase>" +
				"<VerbPhrase>" +
					"<VBD>was</VBD>" +
					"<VBN>used</VBN>" +
					"<RB>instead</RB>" +
					"<PrepPhrase>" +
						"<IN-OF>of</IN-OF>" +
						"<NounPhrase>" +
						"<MOLECULE>" +
							"<OSCARCM>" +
								"<OSCAR-CM>cyclopropanoyl</OSCAR-CM>" +
								"<OSCAR-CM>chloride</OSCAR-CM>" +
							"</OSCARCM>" +
						"</MOLECULE>" +
						"</NounPhrase>" +
					"</PrepPhrase>" +
				"</VerbPhrase>" +
			"</Sentence>");
		List<Element> molecules = XOMTools.getDescendantElementsWithTagName(paragraph, ChemicalTaggerTags.MOLECULE_Container);
		assertEquals("unit test is broken", 2, molecules.size());
		BiMap<Element, Chemical>  moleculeToChemicalMap = HashBiMap.create();
		Chemical chem1 = new Chemical("acetyl chloride");
		Chemical chem2 = new Chemical("cyclopropanoyl chloride");
		moleculeToChemicalMap.put(molecules.get(0), chem1);
		moleculeToChemicalMap.put(molecules.get(1), chem2);
		ExperimentalStepParser stepParser = new ExperimentalStepParser(mock(ExperimentalStep.class), moleculeToChemicalMap, mock(Chemical.class),  mock(Chemical.class));
		stepParser.markReplacedChemicalsAsFalsePositives(paragraph.getDocument());
		assertEquals(null, chem1.getEntityType());
		assertEquals(ChemicalType.falsePositive, chem2.getEntityType());
	}
}

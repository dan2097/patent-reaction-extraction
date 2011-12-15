package dan2097.org.bitbucket.chemicaltagging;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.chemicaltagger.OscarTokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.Token;

public class TrivialChemicalNameTaggerTest {

	@Test
	public void trivialNamesThatShouldBeTaggedTest() {
		TrivialChemicalNameTagger tagger = new TrivialChemicalNameTagger();
		String inputSentence = "Wilkinson's catalyst is a catalyst whilst girard reagent t is a reagent";
		List<Token> tokens = new OscarTokeniser().tokenise(inputSentence);
		List<String> tags = tagger.runTagger(tokens, inputSentence);
		assertEquals("Unexpected output from OSCAR tokeniser", 12, tags.size());
		assertEquals("OSCAR-CM", tags.get(0));
		assertEquals("OSCAR-CM", tags.get(1));
		assertEquals("nil", tags.get(2));
		assertEquals("nil", tags.get(3));
		assertEquals("nil", tags.get(4));
		assertEquals("nil", tags.get(5));
		assertEquals("OSCAR-CM", tags.get(6));
		assertEquals("OSCAR-CM", tags.get(7));
		assertEquals("OSCAR-CM", tags.get(8));
		assertEquals("nil", tags.get(9));
		assertEquals("nil", tags.get(10));
		assertEquals("nil", tags.get(11));
	}
	@Test
	public void systematicNamesShouldNotBeTaggedTest() {
		TrivialChemicalNameTagger tagger = new TrivialChemicalNameTagger();
		String inputSentence = "Ethane, ethyl acetate, pro pyl propionate and biphenyl are chemical entities";
		List<Token> tokens = new OscarTokeniser().tokenise(inputSentence);
		List<String> tags = tagger.runTagger(tokens, inputSentence);
		for (String tag : tags) {
			assertEquals("nil", tag);
		}
	}
}

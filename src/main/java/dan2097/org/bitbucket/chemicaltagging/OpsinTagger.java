package dan2097.org.bitbucket.chemicaltagging;

import java.util.ArrayList;
import java.util.List;

import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;

import uk.ac.cam.ch.wwmm.chemicaltagger.Tagger;
import uk.ac.cam.ch.wwmm.oscar.document.Token;

public class OpsinTagger implements Tagger {
	
	/***********************************************
	 * Runs the OPSIN document extractor over the given inputSentence and returns a list of tags
	 * @param tokenList
	 * @param inputSentence
	 * @return tagList
	 ***********************************************/
	public List<String> runTagger(List<Token> tokenList, String inputSentence) {
		List<String> tagList = new ArrayList<String>();
		String tag = "nil";
		for (int i = 0; i < tokenList.size(); i++) {
			tagList.add(tag);
		}
		List<IdentifiedChemicalName> identifiedNames = new DocumentToStructures(inputSentence).extractNames();
		for (IdentifiedChemicalName ne : identifiedNames) {
			Token startingToken = getTokenByStartIndex(ne.getStart(), tokenList);
			Token endingToken = getTokenByEndIndex(ne.getEnd(), tokenList);
			if (startingToken != null && endingToken != null){//in the rare case that OPSIN has a different tokenisation nothing can be done
				for (int i = startingToken.getIndex(); i <= endingToken.getIndex(); i++) {
					tagList.set(i, "OSCAR-CM");
				}
			}
		}

		return tagList;
	}
	/**
	 * Returns the token that starts at the given index, or null if no such
	 * token exists. 
	 * @param tokens
	 */
	public Token getTokenByStartIndex(int index, List<Token> tokens) {
		for (Token token : tokens) {
			if (token.getStart() == index) {
				return token;
			}
		}
		return null;
	}

	/**
	 * Returns the token that ends at the given index, or null if no such
	 * token exists. 
	 * @param tokens
	 */
	public Token getTokenByEndIndex(int index, List<Token> tokens) {
		for (Token token : tokens) {
			if (token.getEnd() == index) {
				return token;
			}
		}
		return null;
	}

	public List<String> getIgnoredTags() {
		return new ArrayList<String>();
	}
}

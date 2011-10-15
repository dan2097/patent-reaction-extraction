package dan2097.org.bitbucket.chemicaltagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.chemicaltagger.OscarTagger;
import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscarrecogniser.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;
import dan2097.org.bitbucket.utility.Utils;

public class CustomisedOscarTagger extends OscarTagger {
	
	private static String STOPWORDS_LOCATION = "/dan2097/org/bitbucket/chemicaltagging/stopWords.txt";
	private final Set<String> stopWords;

	public CustomisedOscarTagger(Oscar oscar) {
		super(oscar);
		stopWords = Utils.fileToStringSet(STOPWORDS_LOCATION);
	}
	
	/***********************************************
	 * Runs OSCAR over a list of tokens and returns a list of tags
	 * Differs from chemcial tagger's implementation due to inclusion of overlapping entities and exclusion of ont terms and stop words
	 * @param tokenList (List<Token>)
	 * @return tagList (List<String>)
	 ***********************************************/
	public List<String> runTagger(List<Token> tokenList, String inputSentence) {
		ChemicalEntityRecogniser recogniser = oscar.getRecogniser();
		TokenSequence tokenSequence = generateOscarTokenSequence(tokenList, inputSentence);
		List<NamedEntity> neList = recogniser.findNamedEntities(Arrays.asList(tokenSequence), ResolutionMode.MARK_BLOCKED);
        List<String> ignoreOscarList = Arrays.asList("CPR", "ONT");
		List<String> tagList = new ArrayList<String>();
		String nilTag = "nil";
		for (int i = 0; i < tokenList.size(); i++) {
			tagList.add(nilTag);
		}
		for (NamedEntity ne : neList) {
			String neTypeName = ne.getType().getName();
			if (!ignoreOscarList.contains(neTypeName)) {
				List<Token> tokens = ne.getTokens();
				for (Token token : tokens) {
					if (stopWords.contains(token.getSurface().toLowerCase()) && ne.getType().getName().equals("CM")){
						continue;
					}
					if (tagList.get(token.getIndex()).equals(nilTag)){
						tagList.set(token.getIndex(), "OSCAR-"+ neTypeName);
					}
				}
			}
		}
		return tagList;
	}
}

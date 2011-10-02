package dan2097.org.bitbucket.chemicaltagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;

import dan2097.org.bitbucket.utility.Utils;

import uk.ac.cam.ch.wwmm.chemicaltagger.OscarTagger;
import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscarrecogniser.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;

/*****************************************************
 * Uses the combination of both OSCAR and OPSIN to tag chemistry
 * 
 * @author lh359, dl387
 *****************************************************/
public class OscarAndOpsinTagger extends OscarTagger {
	
	private static String STOPWORDS_LOCATION = "/dan2097/org/bitbucket/chemicaltagging/stopWords.txt";
	private final Set<String> stopWords;

	public OscarAndOpsinTagger(Oscar oscar) {
		super(oscar);
		stopWords = Utils.fileToStringSet(STOPWORDS_LOCATION);
	}
	
	/***********************************************
	 * Runs OSCAR over a list of tokens.
	 * 
	 * @param tokenList (List<String>)
	 * @return tagList (List<String>)
	 ***********************************************/
	public List<String> runTagger(List<String> tokenList, String inputSentence) {
		ChemicalEntityRecogniser recogniser = oscar.getRecogniser();
		List<NamedEntity> neList = recogniser.findNamedEntities(convertToOscarTokenSequences(tokenList, inputSentence), ResolutionMode.MARK_BLOCKED);
        List<String> ignoreOscarList = Arrays.asList("cpr", "ont");
		List<String> oscarAndOpsinList = new ArrayList<String>();
		String tag = "nil";
		for (int i = 0; i < tokenList.size(); i++) {
			oscarAndOpsinList.add(tag);
		}
		for (NamedEntity ne : neList) {
			if (!ignoreOscarList.contains(ne.getType().getName().toLowerCase())) {
				List<Token> tokens = ne.getTokens();
                 
				for (Token token : tokens) {
					String tokenSurface = token.getSurface();
					if (!stopWords.contains(tokenSurface.toLowerCase()) && oscarAndOpsinList.get(token.getIndex()).equals(tag)){
						oscarAndOpsinList.set(token.getIndex(), "OSCAR-"+ne.getType().getName());
					}
				}
			}
		}
		List<IdentifiedChemicalName> identifiedNames = new DocumentToStructures(tokenList).extractNames();
		for (IdentifiedChemicalName ne : identifiedNames) {
			for (int i = ne.getWordPositionStartIndice(); i <= ne.getWordPositionEndIndice(); i++) {
				oscarAndOpsinList.set(i, "OSCAR-CM");
			}
		}

		return oscarAndOpsinList;
	}

}

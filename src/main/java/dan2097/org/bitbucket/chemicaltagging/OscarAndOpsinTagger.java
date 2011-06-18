package dan2097.org.bitbucket.chemicaltagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;

import uk.ac.cam.ch.wwmm.chemicaltagger.OscarTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.POSContainer;
import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;

/*****************************************************
 * Uses the combination of both OSCAR and OPSIN to tag chemistry
 * 
 * @author lh359, dl387
 *****************************************************/
public class OscarAndOpsinTagger extends OscarTagger {

	public OscarAndOpsinTagger(Oscar oscar) {
		super(oscar);
	}
	
	/***********************************************
	 * Runs OSCAR over a list of tokens.
	 * 
	 * @param posContainer  (POSContainer)
	 * @return posContainer (POSContainer)
	 ***********************************************/
	public POSContainer runTagger(POSContainer posContainer) {
		List<NamedEntity> neList = oscar.recogniseNamedEntities(posContainer.getTokenSequenceList());
        List<String> ignoreOscarList = Arrays.asList("cpr", "ont");
		List<String> tokenList = posContainer.getWordTokenList();
		List<String> oscarAndOpsinList = new ArrayList<String>();
		String tag = "nil";
		for (int i = 0; i < tokenList.size(); i++) {
			oscarAndOpsinList.add(tag);
		}
		for (NamedEntity ne : neList) {
			if (!ignoreOscarList.contains(ne.getType().getName().toLowerCase())) {
				List<Token> tokens = ne.getTokens();
                 
				for (Token token : tokens) {
					if (tokenList.get(token.getIndex()).contains(token.getSurface())) {
						oscarAndOpsinList.set(token.getIndex(), "OSCAR-"+ne.getType().getName());
					}
				}
			}
		}

		String[] words = tokenList.toArray(new String[tokenList.size()]);
		List<IdentifiedChemicalName> identifiedNames = DocumentToStructures.extractNames(words);
		for (IdentifiedChemicalName ne : identifiedNames) {
			for (int i = ne.getWordPositionStartIndice(); i <= ne.getWordPositionEndIndice(); i++) {
				oscarAndOpsinList.set(i, "OSCAR-CM");
			}
		}

		posContainer.setOscarTagList(oscarAndOpsinList);
		return posContainer;
	}

}

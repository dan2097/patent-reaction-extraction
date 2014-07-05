package dan2097.org.bitbucket.chemicaltagging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.chemicaltagger.Tagger;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import dan2097.org.bitbucket.utility.Utils;

public class TrivialChemicalNameTagger implements Tagger {

	private static final Pattern matchWhiteSpace = Pattern.compile("\\s+");
	private static final String DICTIONARY_LOCATION = "/dan2097/org/bitbucket/chemicaltagging/trivialNameDictionary.txt";
	
	private final Map<String, HashHolder> wordToHashHolderMap = new HashMap<String, HashHolder>();
	
	private static class HashHolder {
		private Map<String, HashHolder> wordToHashHolderMap;

		private boolean isTerminal;
		public HashHolder(boolean isTerminal) {
			this.isTerminal = isTerminal;
		}
		
		boolean isTerminal() {
			return isTerminal;
		}
		void setIsTerminal(boolean isTerminal) {
			this.isTerminal = isTerminal;
		}
		
		Map<String, HashHolder> getWordToHashHolderMap() {
			return wordToHashHolderMap;
		}

		void setWordToHashHolderMap(Map<String, HashHolder> wordToHashHolderMap) {
			this.wordToHashHolderMap = wordToHashHolderMap;
		}
	}
	
	public TrivialChemicalNameTagger(){
		Set<String> names = Utils.fileToStringSet(DICTIONARY_LOCATION);
		for (String name : names) {
			addToHashMaps(name);
			if (name.contains("-")){
				addToHashMaps(name.replaceAll("-", " - "));
			}
		}
	}

	private void addToHashMaps(String name) {
		String[] words = matchWhiteSpace.split(name);
		if (words.length > 0){
			HashHolder currentHashHolder;
			boolean isTerminal = words.length == 1;
			if (wordToHashHolderMap.containsKey(words[0])){
				currentHashHolder = wordToHashHolderMap.get(words[0]);
				if (isTerminal){
					currentHashHolder.setIsTerminal(true);
				}
			}
			else{
				currentHashHolder = new HashHolder(isTerminal);
				wordToHashHolderMap.put(words[0], currentHashHolder);
			}
			for (int i = 1; i < words.length; i++) {
				Map<String, HashHolder> wordToHashHolderMap = currentHashHolder.getWordToHashHolderMap();
				isTerminal = (words.length - 1 == i);
				if (wordToHashHolderMap != null){
					if (wordToHashHolderMap.containsKey(words[i])){
						currentHashHolder = wordToHashHolderMap.get(words[i]);
						if (isTerminal){
							currentHashHolder.setIsTerminal(true);
						}
					}
					else{
						currentHashHolder = new HashHolder(isTerminal);
						wordToHashHolderMap.put(words[i], currentHashHolder);
					}
				}
				else{
					wordToHashHolderMap = new HashMap<String, HashHolder>();
					currentHashHolder.setWordToHashHolderMap(wordToHashHolderMap);
					currentHashHolder = new HashHolder(isTerminal);
					wordToHashHolderMap.put(words[i], currentHashHolder);
				}
			}
		}
	}
	
	/***********************************************
	 * Runs the trivial chemical name tagger over the token list and tags matched tokens
	 * @param tokenList (List<Token>)
	 * @param inputSentence (String)
	 * @return tagList (List<String>)
	 ***********************************************/
	public List<String> runTagger(List<Token> tokenList, String inputSentence) {
		List<String> tagList = new ArrayList<String>();
		int len = tokenList.size();
		for (int i = 0; i < len; i++) {
			tagList.add("nil");
		}
		for (int i = 0; i < len; i++) {
			String tokenStr = tokenList.get(i).getSurface().toLowerCase(Locale.ROOT);
			HashHolder currentHashHolder = wordToHashHolderMap.get(tokenStr);
			if (currentHashHolder != null){
				if (currentHashHolder.isTerminal()){
					tagList.set(i, "OSCAR-CM");
				}
				for (int j = i + 1; j < len; j++) {
					Map<String, HashHolder> wordToHashHolderMap = currentHashHolder.getWordToHashHolderMap();
					if (wordToHashHolderMap != null){
						currentHashHolder = wordToHashHolderMap.get(tokenList.get(j).getSurface().toLowerCase(Locale.ROOT));
						if (currentHashHolder != null){
							if (currentHashHolder.isTerminal()){
								for (int wordToTagIndice = i; wordToTagIndice <= j; wordToTagIndice++) {
									tagList.set(wordToTagIndice, "OSCAR-CM");
								}
							}
						}
						else{
							break;
						}
					}
					else{
						break;
					}
				}
			}
		}
		return tagList;
	}

	public List<String> getIgnoredTags() {
		return new ArrayList<String>();
	}
}

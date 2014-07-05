package dan2097.org.bitbucket.reactionextraction;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



import com.google.common.collect.BiMap;

import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import dan2097.org.bitbucket.utility.XomUtils;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import dan2097.org.bitbucket.utility.Utils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

public class Paragraph {
	private final String untaggedString;
	private final Document taggedSentencesDocument;
	private final String identifier;
	private static final List<String> WORKUP_PHRASES = Arrays.asList("Concentrate", "Degass", "Dry", "Extract", "Filter", "Partition", "Precipitate", "Purify", "Recover", "Remove", "Wash", "Quench");
	private static final String[] CONTAINER_ELS = new String[]{ACTIONPHRASE_Container, UNMATCHED_Container, NOUN_PHRASE_Container, VERBPHRASE_Container, ATMOSPHEREPHRASE_Container, TIMEPHRASE_Container,TEMPPHRASE_Container, PREPPHRASE_Container, ROLEPREPPHRASE_Container};

	/**
	 * Creates a Paragraph from the given text
	 * A paragraph contains the results of running chemical tagger on the input text
	 * Preferably a unique identifier which will assist in tying the paragraphs to the original document should be given
	 * @param paragraphText
	 * @param identifier
	 */
	public Paragraph(String paragraphText, String identifier) {
		untaggedString = paragraphText;
		this.identifier = identifier;
		if (untaggedString.equals("")){
			taggedSentencesDocument = new Document(new Element("Document"));
		}
		else{
			taggedSentencesDocument = Utils.runChemicalTagger(untaggedString);
		}
	}

	/**
	 * The sentence as a XOM document as produced by chemical tagger.
	 * @return
	 */
	public Document getTaggedSentencesDocument() {
		return taggedSentencesDocument;
	}
	
	/**
	 * Gets the unique identifier for this paragraph (or null if not set)
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	Map<Element, PhraseType> generatePhraseToTypeMapping(BiMap<Element, Chemical> moleculeToChemicalMap) {
		Map<Element, PhraseType> phraseToAssignment = new LinkedHashMap<Element, PhraseType>();
		Nodes sentences = taggedSentencesDocument.query("//" +SENTENCE_Container);
		boolean inWorkup = false;
		for (int i = 0; i < sentences.size(); i++) {
			Element sentence = (Element) sentences.get(i);
			List<Element> phrases = getChildPhraseElements(sentence);
			for (Element phrase : phrases) {
				boolean workup = false;
				if (phrase.getLocalName().equals(ACTIONPHRASE_Container)){
					String phraseType = phrase.getAttributeValue(ChemicalTaggerAtrs.TYPE_ATR);
					if (WORKUP_PHRASES.contains(phraseType) && !phraseContainsMoleculeWithAmountEquivalentsOrYields(phrase, moleculeToChemicalMap)){
						workup = true;
					}
				}
				if (workup){
					phraseToAssignment.put(phrase, PhraseType.workup);
					inWorkup = true;
				}
				else{
					if (inWorkup && phraseContainsMoleculeWithAmountEquivalentsOrYields(phrase, moleculeToChemicalMap)){
						inWorkup = false;
					}
					
					if (inWorkup){
						phraseToAssignment.put(phrase, PhraseType.workup);
						if (phrase.getLocalName().equals(ACTIONPHRASE_Container)){
							String phraseType = phrase.getAttributeValue(ChemicalTaggerAtrs.TYPE_ATR);
							if (phraseType.equals("Synthesize") || phraseType.equals("Yield")){
								inWorkup = false;
							}
						}
					}
					else{
						phraseToAssignment.put(phrase, PhraseType.synthesis);
					}
				}
			}
		}
		return phraseToAssignment;
	}

	/**
	 * Finds all super nodes that are direct children of the given sentence element
	 * @param sentence
	 * @return
	 */
	private List<Element> getChildPhraseElements(Element sentence) {
		return XomUtils.getChildElementsWithTagNames(sentence, CONTAINER_ELS);
	}

	private boolean phraseContainsMoleculeWithAmountEquivalentsOrYields(Element phrase, BiMap<Element, Chemical> moleculeToChemicalMap) {
		Nodes molecules = phrase.query(".//*[self::MOLECULE or self::UNNAMEDMOLECULE]");
		for (int j = 0; j < molecules.size(); j++) {
			Chemical chem = moleculeToChemicalMap.get(molecules.get(j));
			if (chem.hasAmountOrEquivalentsOrYield()){
				return true;
			}
		}
		return false;
	}
}

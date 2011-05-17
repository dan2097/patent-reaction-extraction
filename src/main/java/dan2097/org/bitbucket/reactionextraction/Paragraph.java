package dan2097.org.bitbucket.reactionextraction;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import com.google.common.collect.BiMap;

import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLTags;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

public class Paragraph {
	private static Logger LOG = Logger.getLogger(Paragraph.class);
	private static final Pattern matchWhiteSpace = Pattern.compile("\\s+");
	private final String untaggedString;
	private final String taggedString;
	private final Document taggedSentencesDocument;
	private final Map<Element, PhraseType> phraseToAssignment = new LinkedHashMap<Element, PhraseType>();
	private static final List<String> WORKUP_PHRASES = Arrays.asList("Concentrate", "Degass", "Dry", "Extract", "Filter", "Partition", "Precipitate", "Purify", "Recover", "Remove", "Wash", "Quench");
	private static final String[] CONTAINER_ELS = new String[]{ACTIONPHRASE_Container, UNMATCHED_Container, NOUN_PHRASE_Container, VERBPHRASE_Container, ATMOSPHEREPHRASE_Container, TIMEPHRASE_Container,TEMPPHRASE_Container, PREPPHRASE_Container, ROLEPREPPHRASE_Container};

	public Paragraph(Element p) {
		if (!p.getLocalName().equals(XMLTags.P)){
			throw new IllegalArgumentException("A paragraph object must be created from a XOM <p> element");
		}
		untaggedString = getText(p);
		if (untaggedString.equals("")){
			taggedString ="";
			taggedSentencesDocument =new Document(new Element("Document"));
		}
		else{
			taggedString = Utils.tagString(untaggedString);
			//LOG.trace(taggedString);
			taggedSentencesDocument = Utils.runChemicalSentenceParsingOnTaggedString(taggedString);
			if (taggedSentencesDocument ==null){
				throw new RuntimeException("Chemical tagger failed to tag a text string indicating a bug in chemical tagger");
			}
			//if (LOG.isTraceEnabled()){LOG.trace(taggedSentencesDocument.toXML());};
		}
	}

	private String getText(Element p) {
		String text = p.getValue();
		//TODO handle superscripts/subscripts etc. differently?
		text = matchWhiteSpace.matcher(text).replaceAll(" ");
		return text.trim();
	}
	
	String getTaggedString() {
		return taggedString;
	}
	
	String getUnTaggedString() {
		return untaggedString;
	}

	/**
	 * The sentence as a XOM document as produced by chemical tagger.
	 * @return
	 */
	Document getTaggedSentencesDocument() {
		return taggedSentencesDocument;
	}

	 Map<Element, PhraseType> getPhraseMap() {
		return phraseToAssignment;
	}

	void segmentIntoSections(BiMap<Element, Chemical> moleculeToChemicalMap) {
		Nodes sentences = taggedSentencesDocument.query("//" +SENTENCE_Container);
		boolean inWorkup =false;
		for (int i = 0; i < sentences.size(); i++) {
			Element sentence = (Element) sentences.get(i);
			List<Element> phrases = getChildPhraseElements(sentence);
			for (Element phrase : phrases) {
				Boolean workup = false;
				if (phrase.getLocalName().equals(ACTIONPHRASE_Container)){
					String phraseType = phrase.getAttributeValue(ChemicalTaggerAtrs.TYPE_ATR);
					if (WORKUP_PHRASES.contains(phraseType)){
						workup =true;
					}
				}
				if (workup){
					phraseToAssignment.put(phrase, PhraseType.workup);
					inWorkup = true;
				}
				else{
					if (inWorkup && phraseContainsMoleculeWithAmount(phrase, moleculeToChemicalMap)){
						inWorkup =false;
					}
					
					if (inWorkup){
						phraseToAssignment.put(phrase, PhraseType.workup);
						if (phrase.getLocalName().equals(ACTIONPHRASE_Container)){
							String phraseType = phrase.getAttributeValue(ChemicalTaggerAtrs.TYPE_ATR);
							if (phraseType.equals("Synthesize") || phraseType.equals("Yield")){
								inWorkup =false;
							}
						}
					}
					else{
						phraseToAssignment.put(phrase, PhraseType.synthesis);
					}
				}
			}
		}
	}

	/**
	 * Finds all super nodes that are direct children of the given sentence element
	 * @param sentence
	 * @return
	 */
	private List<Element> getChildPhraseElements(Element sentence) {
		return XOMTools.getChildElementsWithTagNames(sentence, CONTAINER_ELS);
	}

	private boolean phraseContainsMoleculeWithAmount(Element phrase, BiMap<Element, Chemical> moleculeToChemicalMap) {
		Nodes molecules = phrase.query(".//*[self::MOLECULE or self::UNNAMEDMOLECULE]");
		for (int j = 0; j < molecules.size(); j++) {
			Chemical chem = moleculeToChemicalMap.get(molecules.get(j));
			if (chem.getAmountValue()!=null){
				return true;
			}
		}
		return false;
	}
}

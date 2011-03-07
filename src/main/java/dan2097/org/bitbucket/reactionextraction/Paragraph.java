package dan2097.org.bitbucket.reactionextraction;

import java.util.regex.Pattern;

import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLTags;
import nu.xom.Document;
import nu.xom.Element;

public class Paragraph {
	private static final Pattern matchWhiteSpace = Pattern.compile("\\s+");
	private final String untaggedString;
	private final String taggedString;
	private final Document taggedSentencesDocument;
	
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
			taggedSentencesDocument = Utils.runChemicalSentenceParsingOnTaggedString(taggedString);
			if (taggedSentencesDocument ==null){
				throw new RuntimeException("Chemical tagger failed to tag a text string indicating a bug in chemical tagger");
			}
		}
//		if (!isEmpty){//TODO remove this commented out code
//			Elements sentences = taggedSentencesDocument.getRootElement().getChildElements("Sentence");
//			boolean fail =false;
//			for (int i = 0; i < sentences.size(); i++) {
//				if (sentences.get(i).getValue().equals("")){
//					fail=true;
//				}
//			}
//			if (fail){
//				System.out.println("FAIL TEXT: " +text);
//			}
//		}
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
}

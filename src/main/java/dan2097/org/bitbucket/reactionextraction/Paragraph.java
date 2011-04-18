package dan2097.org.bitbucket.reactionextraction;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLTags;
import nu.xom.Document;
import nu.xom.Element;

public class Paragraph {
	private static Logger LOG = Logger.getLogger(Paragraph.class);
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
			LOG.trace(taggedString);
			taggedSentencesDocument = Utils.runChemicalSentenceParsingOnTaggedString(taggedString);
			if (taggedSentencesDocument ==null){
				throw new RuntimeException("Chemical tagger failed to tag a text string indicating a bug in chemical tagger");
			}
			if (LOG.isTraceEnabled()){LOG.trace(taggedSentencesDocument.toXML());};
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
}

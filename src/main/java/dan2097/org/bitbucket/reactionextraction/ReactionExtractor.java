package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import dan2097.org.bitbucket.utility.XMLTags;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import nu.xom.Document;
import nu.xom.Element;

public class ReactionExtractor {
	/**
	 * Convenience method to extract reactions from a USPTO patent as a XOM document
	 * @param usptoPatentDoc
	 * @return
	 */
	public List<Reaction> extractReactions(Document usptoPatentDoc){
		return extractReactions(getHeadingsAndParagraphsFromUSPTOPatent(usptoPatentDoc));
	}

	/**
	 * Extracts reactions from a list of headings and pargraphs.
	 * These should be ordered in the order they would be in the source document
	 * @param orderedHeadingsAndParagraphs
	 * @return
	 */
	public List<Reaction> extractReactions(List<Element> orderedHeadingsAndParagraphs){
		List<Element> headingsAndParagraphsCopy = new ArrayList<Element>();//defensively copy so as to allow modification and rearrangment of the XML
		for (Element element : orderedHeadingsAndParagraphs) {
			headingsAndParagraphsCopy.add(new Element(element));
		}
		ExperimentalSectionsCreator sectionsCreator = new ExperimentalSectionsCreator(headingsAndParagraphsCopy);
		List<ExperimentalSection> experimentalSections = sectionsCreator.createSections();
		List<Reaction> extractedReactions = new ArrayList<Reaction>();
		return extractedReactions;
	}

	private List<Element> getHeadingsAndParagraphsFromUSPTOPatent(Document usptoPatentDoc) {
		Element description = usptoPatentDoc.getRootElement().getFirstChildElement(XMLTags.DESCRIPTION);
		if (description ==null){
			throw new RuntimeException("Malformed USPTO patent, no \"description\" element found");
		}
		return XOMTools.getChildElementsWithTagNames(description, new String[]{XMLTags.HEADING, XMLTags.P});
	}
}

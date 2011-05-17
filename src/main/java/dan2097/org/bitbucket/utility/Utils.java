package dan2097.org.bitbucket.utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.ggasoftware.indigo.Indigo;

import dan2097.org.bitbucket.reactionextraction.Chemical;
import dan2097.org.bitbucket.reactionextraction.ExperimentalParser;
import dan2097.org.bitbucket.reactionextraction.ExperimentalSectionParser;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistryPOSTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistrySentenceParser;
import uk.ac.cam.ch.wwmm.chemicaltagger.POSContainer;
import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.opsin.OpsinDictionary;

public class Utils {
	
	private static Builder xomBuilder;
	private static ChemNameDictRegistry chemNameRegistery;
	public static Indigo indigo = new Indigo();
	static{
		chemNameRegistery = new ChemNameDictRegistry();
		chemNameRegistery.register(new OpsinDictionary());
		XMLReader xmlReader;
		try{
			xmlReader = XMLReaderFactory.createXMLReader();
		}
		catch (Exception e) {
			throw new RuntimeException("No XML Reader could be initialised!");
		}
		try{
			xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		}
		catch (Exception e) {
			throw new RuntimeException("Your system's default XML Reader does not support disabling DTD loading! Maybe try updating your version of java?");
		}
		xomBuilder = new Builder(xmlReader);
	}
	/**
	 * Tags a string with parts of speech using chemical tagger. Where known the annotations will be more specific than those used for the Brown corpus
	 * @param text
	 * @return
	 */
	public static String tagString(String text) {
		POSContainer posContainer = ChemistryPOSTagger.getDefaultInstance().runTaggers(text);
		return posContainer.getTokenTagTupleAsString();
	}
	
	/**
	 * Given a tagged string returns the sentence as a hierarchy grouped by identified phrases of chemical significance
	 * @param tagged
	 * @return
	 */
	public static Document runChemicalSentenceParsingOnTaggedString(String tagged) {
		InputStream in;
		try {
			in = new ByteArrayInputStream(tagged.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Java VM is broken, UTF-8 should be supported!",e);
		}
		ChemistrySentenceParser chemistrySentenceParser = new ChemistrySentenceParser(in);
		chemistrySentenceParser.parseTags();
		return chemistrySentenceParser.makeXMLDocument();
	}

	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to SMILES
	 * @param name
	 * @return
	 */
	public static String resolveNameToSmiles(String name) {
		return chemNameRegistery.getShortestSmiles(name);
	}
	
	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to InChI
	 * @param name
	 * @return
	 */
	public static String resolveNameToInchi(String name) {
		Set<String> inchis = chemNameRegistery.getInchis(name);
		if (!inchis.isEmpty()){
			return inchis.iterator().next();
		}
		return null;
	}
	
	public static List<String> getSystematicChemicalNamesFromText(String text) {
		try{
			List<IdentifiedChemicalName> identifiedNames = DocumentToStructures.extractNames(text);
			List<String> names = new ArrayList<String>();
			for (IdentifiedChemicalName identifiedChemicalName : identifiedNames) {
				names.add(identifiedChemicalName.getChemicalName());
			}
			return names;
		}
		catch (Exception e) {
			return Collections.emptyList();
		}
	}

	/**
	 * Builds an XML document from an inputstream
	 * @param inputStream
	 * @return
	 * @throws ValidityException
	 * @throws ParsingException
	 * @throws IOException
	 */
	public static Document buildXmlFile(InputStream inputStream) throws ValidityException, ParsingException, IOException {
		return xomBuilder.build(inputStream);
	}
	
	public static ExperimentalParser extractReactions(Document doc){
		ExperimentalParser parser = new ExperimentalParser();
		List<Element> headings = XOMTools.getDescendantElementsWithTagName(doc.getRootElement(), XMLTags.HEADING);
		for (Element heading : headings) {
	    	Element headingElementToProcess = new Element(XMLTags.HEADING);
	    	headingElementToProcess.addAttribute(new Attribute(XMLAtrs.TITLE, heading.getValue()));
	    	List<Element> paragraphs = getNextAdjacentSiblingsOfType(heading, XMLTags.P);
	    	for (Element paragraph : paragraphs) {
		        Element paragraphToProcess = new Element(paragraph);
		        headingElementToProcess.appendChild(paragraphToProcess);
			}
	    	if (paragraphs.size()>0){
		    	parser.parseExperimentalSection(headingElementToProcess);
	    	}
		}
		return parser;
	}
	
	/**
	 * Returns an arrayList containing sibling elements of the given type after the given element.
	 * @param currentElem: the element to look for following siblings of
	 * @param type: the "localname" of the element type desired
	 * @return
	 */
	private static List<Element> getNextAdjacentSiblingsOfType(Element currentElem, String type) {
		List<Element> siblingElementsOfType= new ArrayList<Element>();
		Element parent =(Element) currentElem.getParent();
		if (parent==null){
			return siblingElementsOfType;
		}
		Node nextSibling = XOMTools.getNextSibling(currentElem);
		while (nextSibling !=null){
			if (nextSibling instanceof Element){
				if (((Element)nextSibling).getLocalName().equals(type)){
					siblingElementsOfType.add(((Element)nextSibling));
				}
				else{
					break;
				}
			}
			nextSibling = XOMTools.getNextSibling(nextSibling);
		}

		return siblingElementsOfType;
	}
	
	/**
	 * Gets the next element. This element need not be a sibling
	 * @param startingEl
	 * @return
	 */
	public static Element getNextElement(Element startingEl) {
		ParentNode parent = startingEl.getParent();
		if (parent == null || !(parent instanceof Element)){
			return null;
		}
		int index = parent.indexOf(startingEl);
		if (index +1 >=parent.getChildCount()){
			return getNextElement((Element) parent);//reached end of element
		}
		Node nextNode = parent.getChild(index+1);
		if (!(nextNode instanceof Element)){
			return null;
		}
		Element next =(Element) nextNode;
		Elements children =next.getChildElements();
		while (children.size()!=0){
			next =children.get(0);
			children =next.getChildElements();
		}
		return next;
	}

	/**
	 * Gets the previous element. This element need not be a sibling
	 * @param startingEl
	 * @return
	 */
	public static Element getPreviousElement(Element startingEl) {
		ParentNode parent = startingEl.getParent();
		if (parent == null || !(parent instanceof Element)){
			return null;
		}
		int index = parent.indexOf(startingEl);
		if (index ==0) {
			return getPreviousElement((Element) parent);//reached beginning of element
		}
		Node previousNode = parent.getChild(index-1);
		if (!(previousNode instanceof Element)){
			return null;
		}
		Element previous =(Element) previousNode;
		Elements children =previous.getChildElements();
		while (children.size()!=0){
			previous =children.get(children.size()-1);
			children =previous.getChildElements();
		}
		return previous;
	}
	
	public static Chemical extractChemicalFromHeading(String title) {
		if (title==null){
			throw new IllegalArgumentException("Input title text was null");
		}
		List<String> name = getSystematicChemicalNamesFromText(title);
		if (name.size()==1){
			return new Chemical(name.get(0));
		}
		return null;
	}
	
	/**
	 * Convenience method for creating an experimental section parser
	 * @param title
	 * @param content
	 * @return
	 */
	public static ExperimentalSectionParser createExperimentalSectionParser(String title, String content){
		Chemical titleCompound = extractChemicalFromHeading(title);
		List<Element> paragraphEls = new ArrayList<Element>();
		Element paragraph = new Element(XMLTags.P);
		paragraph.appendChild(content);
		paragraphEls.add(paragraph);
		return new ExperimentalSectionParser(titleCompound, paragraphEls, new HashMap<String, Chemical>());
	}
}

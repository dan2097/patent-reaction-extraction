package dan2097.org.bitbucket.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistrySentenceParser;
import uk.ac.cam.ch.wwmm.chemicaltagger.POSContainer;
import uk.ac.cam.ch.wwmm.opsin.NameToInchi;
import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureException;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;

import dan2097.org.bitbucket.reactionextraction.Chemical;
import dan2097.org.bitbucket.reactionextraction.ExperimentalParser;
import dan2097.org.bitbucket.reactionextraction.ExperimentalSectionParser;
import dan2097.org.bitbucket.reactionextraction.FunctionalGroupDefinitions;
import dan2097.org.bitbucket.reactionextraction.Reaction;
import dan2097.org.bitbucket.reactionextraction.ReactionDepicter;

public class Utils {
	
	private static Builder xomBuilder;
	
	static {
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
		POSContainer posContainer = OscarReliantFunctionality.getInstance().getPosTagger().runTaggers(text);
		return posContainer.getTokenTagTupleAsString();
	}
	
	/**
	 * Given a tagged string returns the sentence as a hierarchy grouped by identified phrases of chemical significance
	 * @param tagged
	 * @return
	 */
	public static Document runChemicalSentenceParsingOnTaggedString(String tagged) {
		ChemistrySentenceParser chemistrySentenceParser = new ChemistrySentenceParser(tagged);
		chemistrySentenceParser.parseTags();
		return chemistrySentenceParser.makeXMLDocument();
	}

	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to SMILES
	 * @param name
	 * @return
	 */
	public static String resolveNameToSmiles(String name) {
		return OscarReliantFunctionality.getInstance().getChemNameDictRegistry().getShortestSmiles(name);
	}
	
	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to InChI
	 * @param name
	 * @return
	 */
	public static String resolveNameToInchi(String name) {
		Set<String> inchis = OscarReliantFunctionality.getInstance().getChemNameDictRegistry().getInchis(name);
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
		Nodes headings = doc.getRootElement().query("//heading|//p[starts-with(@id, 'h-')]");
		for (int i = 0; i < headings.size(); i++) {
			Element heading = (Element) headings.get(i);
	    	Element headingElementToProcess = new Element(XMLTags.HEADING);
	    	String headingText = heading.getValue();
	    	if (heading.getLocalName().equals(XMLTags.P) && headingText.contains("\n")){
	    		continue;
	    	}
	    	headingElementToProcess.addAttribute(new Attribute(XMLAtrs.TITLE, headingText));
	    	List<Element> paragraphs = getAdjacentSiblingsParagraphs(heading);
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
	 * Returns an arrayList containing sibling elements of type "p" which are not believed to be subheadings
	 * @param currentElem: the element to look for following siblings of
	 * @return
	 */
	private static List<Element> getAdjacentSiblingsParagraphs(Element currentElem) {
		List<Element> siblingElementsOfType= new ArrayList<Element>();
		Element parent =(Element) currentElem.getParent();
		if (parent==null){
			return siblingElementsOfType;
		}
		Node nextSibling = XOMTools.getNextSibling(currentElem);
		while (nextSibling !=null){
			if (nextSibling instanceof Element){
				if (((Element)nextSibling).getLocalName().equals(XMLTags.P)){
					String id = ((Element)nextSibling).getAttributeValue(XMLAtrs.ID);
					if (id !=null && id.startsWith("h-")){
						break;//break on subheadings
					}
					else{
						siblingElementsOfType.add(((Element)nextSibling));
					}
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
	
	public static Chemical extractResolvableChemicalFromHeading(String title) {
		if (title==null){
			throw new IllegalArgumentException("Input title text was null");
		}
		List<String> names = getSystematicChemicalNamesFromText(title);
		if (names.size()==1){
			String name = names.get(0);
			NameToStructure n2s;
			try {
				n2s = NameToStructure.getInstance();
			} catch (NameToStructureException e) {
				throw new RuntimeException("OPSIN failed to initialise", e);
			}
			OpsinResult result = n2s.parseChemicalName(name);
			if (result.getStatus() != OPSIN_RESULT_STATUS.FAILURE){
				Chemical chem = new Chemical(name);
				chem.setSmiles(result.getSmiles());
				String rawInchi = NameToInchi.convertResultToInChI(result);
				if (rawInchi!=null){
					chem.setInchi(InchiNormaliser.normaliseInChI(rawInchi));
				}
				chem.setSmarts(FunctionalGroupDefinitions.getSmartsFromChemicalName(name));
				return chem;
			}
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
		Chemical titleCompound = extractResolvableChemicalFromHeading(title);
		List<Element> paragraphEls = new ArrayList<Element>();
		Element paragraph = new Element(XMLTags.P);
		paragraph.appendChild(content);
		paragraphEls.add(paragraph);
		return new ExperimentalSectionParser(titleCompound, paragraphEls, new HashMap<String, Chemical>());
	}
	
	/**
	 * Converts a reaction object into an indigo reaction object
	 * @param reaction
	 * @return
	 */
	public static IndigoObject createIndigoReaction(Reaction reaction) {
		Indigo indigo = IndigoHolder.getInstance();
		IndigoObject rxn = indigo.createReaction();
		for (Chemical product: reaction.getProducts()) {
			if (product.getSmiles()!=null){
				IndigoObject mol = indigo.loadMolecule(product.getSmiles());
				mol.foldHydrogens();
				rxn.addProduct(mol);
			}
		}
		for (Chemical reactant: reaction.getReactants()) {
			if (reactant.getSmiles()!=null){
				IndigoObject mol = indigo.loadMolecule(reactant.getSmiles());
				mol.foldHydrogens();
				rxn.addReactant(mol);
			}
		}
		for (Chemical spectator: reaction.getSpectators()) {
			if (spectator.getSmiles()!=null){
				IndigoObject mol = indigo.loadMolecule(spectator.getSmiles());
				mol.foldHydrogens();
				rxn.addCatalyst(mol);
			}
		}
		return rxn;
	}
	
	public static void serializeReactions(File directory, List<Reaction> reactions) throws IOException {
		if (!directory.exists()){
			FileUtils.forceMkdir(directory);
		}
		if (!directory.isDirectory()){
			throw new IllegalArgumentException("A directory was expected");
		}
		for (int i = 0; i < reactions.size(); i++) {
			Reaction reaction = reactions.get(i);
			if (reaction.getProducts().size()>0 || reaction.getReactants().size()>0){
				try {
					File f = new File(directory, "reaction" + i + ".png");
					ReactionDepicter.depictReaction(Utils.createIndigoReaction(reaction), f);
						FileOutputStream in = new FileOutputStream(new File(directory, "reaction" + i + "src.xml"));
					    Serializer serializer = new Serializer(in);
						serializer.setIndent(2);
						serializer.write(reaction.getInput().getTaggedSentencesDocument());
						IOUtils.closeQuietly(in);
						
					FileOutputStream out = new FileOutputStream(new File(directory, "reaction" + i + ".cml"));
				    serializer = new Serializer(out);
					serializer.setIndent(2);
					serializer.write(new Document(reaction.toCML()));
					IOUtils.closeQuietly(out);	
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

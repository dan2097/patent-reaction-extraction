package dan2097.org.bitbucket.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistrySentenceParser;
import uk.ac.cam.ch.wwmm.chemicaltagger.POSContainer;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;

import dan2097.org.bitbucket.inchiTools.InchiMerger;
import dan2097.org.bitbucket.inchiTools.InchiNormaliser;
import dan2097.org.bitbucket.reactionextraction.Chemical;
import dan2097.org.bitbucket.reactionextraction.ChemicalIdentifierPair;
import dan2097.org.bitbucket.reactionextraction.ExperimentalSectionParser;
import dan2097.org.bitbucket.reactionextraction.ExperimentalSectionsCreator;
import dan2097.org.bitbucket.reactionextraction.PreviousReactionData;
import dan2097.org.bitbucket.reactionextraction.Reaction;
import dan2097.org.bitbucket.reactionextraction.ReactionDepicter;

public class Utils {
	
	private static final Logger LOG = Logger.getLogger(Utils.class);
	private static final Builder xomBuilder;
	private static final Pattern matchTab = Pattern.compile("\\t");
	private static final Pattern matchWhiteSpace = Pattern.compile("\\s+");
	private static final Pattern matchDot = Pattern.compile("\\.");
	private static final Pattern matchForwardSlash = Pattern.compile("/");
	private static final Pattern matchMiddleDot = Pattern.compile("\u00B7");
	
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
	 * Tags a string with parts of speech using ChemicalTagger. Where known the annotations will be more specific than those used for the Brown corpus
	 * @param text
	 * @return
	 */
	public static String tagString(String text) {
		POSContainer posContainer = OscarReliantFunctionality.getInstance().getPosTagger().runTaggers(text);
		return posContainer.getTokenTagTupleAsString();
	}
	
	/**
	 * Convenience method to tag and parse a string of text using ChemicalTagger
	 * @param text
	 * @return
	 */
	public static Document runChemicalTagger(String text) {
		try{
			String taggedText = tagString(text);
			ChemistrySentenceParser chemistrySentenceParser = new ChemistrySentenceParser(taggedText);
			chemistrySentenceParser.parseTags();
			Document doc = chemistrySentenceParser.makeXMLDocument();
			if (doc == null){
				LOG.warn("Chemical tagger failed to tag a text string indicating a bug in ChemicalTagger");
				Element root = new Element("Document");
				return new Document(root);
			}
			return doc;
		}
		catch (StackOverflowError e){
			LOG.error("A StackOverflowError was encountered while running with ChemicalTagger. Possibly a section of text with A LOT of brackets was encountered");
			Element root = new Element("Document");
			return new Document(root);
		}
	}

	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to SMILES
	 * @param name
	 * @return
	 */
	public static String resolveNameToSmiles(String name) {
		return OscarReliantFunctionality.getInstance().resolveNameToSmiles(name);
	}
	
	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to SMILES
	 * The space concatenated form of the nameComponents will first be tried
	 * followed by attempting to resolve all the individual components
	 * @param nameComponents
	 * @return
	 */
	public static String resolveNameToSmiles(List<String> nameComponents) {
		String completeSmiles = resolveNameToSmiles(StringUtils.stringListToString(nameComponents, " "));
		if (completeSmiles == null){
			if (nameComponents.size() > 1){
				StringBuilder smilesSB = new StringBuilder();
				for (String nameComponent : nameComponents) {
					String partialSmiles = resolveNameToSmiles(nameComponent);
					if (partialSmiles == null){
						return null;
					}
					if (smilesSB.length() > 0){
						smilesSB.append('.');
					}
					smilesSB.append(partialSmiles);
				}
				return smilesSB.toString();
			}
			else{
				nameComponents = splitNameIntoComponents(nameComponents.get(0));
				if (nameComponents.size() > 1){
					return resolveNameToSmiles(nameComponents);
				}
			}
		}
		return completeSmiles;
	}
	
	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to a normalised InChI
	 * @param name
	 * @return
	 */
	public static String resolveNameToInchi(String name) {
		String inchi = OscarReliantFunctionality.getInstance().resolveNameToStdInchi(name);
		if (inchi != null) {
			return InchiNormaliser.normaliseInChI(inchi);
		}
		return null;
	}
	
	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to a normalised InChI
	 * The space concatenated form of the nameComponents will first be tried
	 * followed by attempting to resolve all the individual components
	 * @param nameComponents
	 * @return
	 */
	public static String resolveNameToInchi(List<String> nameComponents) {
		String completeInChI = resolveNameToInchi(StringUtils.stringListToString(nameComponents, " "));
		if (completeInChI == null){
			if (nameComponents.size() > 1){
				List<String> partialInchis = new ArrayList<String>();
				for (String nameComponent : nameComponents) {
					String partialInChI = resolveNameToInchi(nameComponent);
					if (partialInChI == null){
						return null;
					}
					partialInchis.add(partialInChI);
				}
				InchiMerger merger = new InchiMerger(partialInchis);
				return merger.generateMergedNormalisedInchi();
			}
			else{
				nameComponents = splitNameIntoComponents(nameComponents.get(0));
				if (nameComponents.size() > 1){
					return resolveNameToInchi(nameComponents);
				}
			}
		}
		return completeInChI;
	}
	
	/**
	 * Attempts to split the given name into components using suitable delimiters
	 * e.g. slashes, dots, whitespace etc.
	 * @param nameComponent
	 * @return
	 */
	private static List<String> splitNameIntoComponents(String nameComponent) {
		String[] middleDotSeperatedStrs = matchMiddleDot.split(nameComponent);
		if (middleDotSeperatedStrs.length > 1){
			return StringUtils.arrayToList(middleDotSeperatedStrs);
		}
		String[] slashSeperatedStrs = matchForwardSlash.split(nameComponent);
		if (slashSeperatedStrs.length > 1){
			return StringUtils.arrayToList(slashSeperatedStrs);
		}
		String[] dotSeperatedStrs = matchDot.split(nameComponent);
		if (dotSeperatedStrs.length > 1){
			return StringUtils.arrayToList(dotSeperatedStrs);
		}
		String[] whiteSpaceSeperatedStrs = matchWhiteSpace.split(nameComponent);
		if (whiteSpaceSeperatedStrs.length == 2){
			return StringUtils.arrayToList(whiteSpaceSeperatedStrs);
		}
		List<String> nameComponents = new ArrayList<String>();
		nameComponents.add(nameComponent);
		return nameComponents;
	}

	public static List<IdentifiedChemicalName> getSystematicChemicalNamesFromText(String text) {
		try{
			return new DocumentToStructures(text).extractNames();
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
	
	/**
	 * Builds an XML document from text
	 * @param xmlAsText
	 * @return
	 * @throws ValidityException
	 * @throws ParsingException
	 * @throws IOException
	 */
	public static Document buildXmlFromString(String xmlAsText) throws ValidityException, ParsingException, IOException {
		return xomBuilder.build(xmlAsText, null);
	}
	
	/**
	 * Returns the text contained within a paragraph.
	 * Tables and lists are detached, white space is normalised
	 * @param paragraphEl
	 * @return
	 */
	public static String detachIrrelevantElementsAndGetParagraphText(Element paragraphEl) {
		if (!paragraphEl.getLocalName().equals(XMLTags.P)){
			throw new IllegalArgumentException("A paragraph element was expected!");
		}
		List<Element> elsToDetach = XomUtils.getDescendantElementsWithTagNames(paragraphEl, new String[]{XMLTags.TABLE_EXTERNAL_DOC, XMLTags.TABLES, XMLTags.DL, XMLTags.OL, XMLTags.UL});
		for (Element elToDetach : elsToDetach) {
			elToDetach.detach();
		}
		return getElementText(paragraphEl);
	}

	/**
	 * Returns the space normalised, trimmed string contents of the given element
	 * @param element
	 * @return
	 */
	public static String getElementText(Element element) {
		String text = element.getValue();
		text = matchWhiteSpace.matcher(text).replaceAll(" ");
		return text.trim();
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
		if (index + 1 >= parent.getChildCount()){
			return getNextElement((Element) parent);//reached end of element
		}
		Node nextNode = parent.getChild(index + 1);
		if (!(nextNode instanceof Element)){
			return null;
		}
		Element next = (Element) nextNode;
		Elements children = next.getChildElements();
		while (children.size() != 0){
			next = children.get(0);
			children = next.getChildElements();
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
		Node previousNode = parent.getChild(index - 1);
		if (!(previousNode instanceof Element)){
			return null;
		}
		Element previous =(Element) previousNode;
		Elements children = previous.getChildElements();
		while (children.size() != 0){
			previous = children.get(children.size() - 1);
			children = previous.getChildElements();
		}
		return previous;
	}
	
	/**
	 * Creates a chemical from a given name with if possible resolved smiles and InChI
	 * @param name
	 * @return
	 */
	public static Chemical createChemicalFromName(String name) {
		if (name == null){
			throw new IllegalArgumentException("Input name was null");
		}
		Chemical chem = new Chemical(name);
		chem.setChemicalIdentifierPair(new ChemicalIdentifierPair(resolveNameToSmiles(name), resolveNameToInchi(name)));
		return chem;
	}
	
	/**
	 * Creates a chemical from a list of name components with if possible resolved smiles and InChI
	 * Each component would be expected to be a standalone chemical entity
	 * @param nameComponents
	 * @return
	 */
	public static Chemical createChemicalFromName(List<String> nameComponents) {
		if (nameComponents == null){
			throw new IllegalArgumentException("Input nameComponents was null");
		}
		Chemical chem = new Chemical(StringUtils.stringListToString(nameComponents, " "));
		chem.setChemicalIdentifierPair(new ChemicalIdentifierPair(resolveNameToSmiles(nameComponents), resolveNameToInchi(nameComponents)));
		return chem;
	}

	/**
	 * Convenience method for creating an experimental section parser
	 * @param title
	 * @param content
	 * @return
	 */
	public static ExperimentalSectionParser createExperimentalSectionParser(String title, String content){
		List<Element> orderedHeadingsAndParagraphs = new ArrayList<Element>();
		Element heading = new Element(XMLTags.HEADING);
		heading.appendChild(title);
		orderedHeadingsAndParagraphs.add(heading);
		Element paragraph = new Element(XMLTags.P);
		paragraph.appendChild(content);
		orderedHeadingsAndParagraphs.add(paragraph);
		ExperimentalSectionsCreator sectionsCreator = new ExperimentalSectionsCreator(orderedHeadingsAndParagraphs);
		return new ExperimentalSectionParser(sectionsCreator.createSections().get(0), new PreviousReactionData());
	}

	/**
	 * Converts a reaction into an indigo reaction.
	 * InChIs are used to collapse compounds with identical structures
	 * @param reaction
	 * @return
	 */
	public static IndigoObject convertToIndigoReaction(Reaction reaction) {
		List<String> products = getSmilesForUniqueStructuresUsingInChIs(reaction.getProducts());
		List<String> reactants = getSmilesForUniqueStructuresUsingInChIs(reaction.getReactants());
		List<String> spectators = getSmilesForUniqueStructuresUsingInChIs(reaction.getSpectators());
		return createIndigoReaction(products, reactants, spectators);
	}

	public static List<String> getSmilesForUniqueStructuresUsingInChIs(List<Chemical> chemicals) {
		List<String> uniqueStructureSmiles = new ArrayList<String>();
		Set<String> seenInChIs = new HashSet<String>();
		for (Chemical chemical : chemicals) {
			String smiles = chemical.getSmiles();
			if (smiles != null){
				String inchi = chemical.getInchi();
				if (inchi != null){
					if (!seenInChIs.contains(inchi)){
						uniqueStructureSmiles.add(smiles);
						seenInChIs.add(inchi);
					}
				}
				else{
					LOG.trace(chemical.getName() +" has no InChI");
				}
			}
		}
		return uniqueStructureSmiles;
	}
	
	/**
	 * Generates an indigo reaction from product/reactant/spectator SMILES lists
	 * @param products
	 * @param reactants
	 * @param spectators
	 * @return
	 */
	public static IndigoObject createIndigoReaction(List<String> products, List<String> reactants, List<String> spectators) {
		Indigo indigo = IndigoHolder.getInstance();
		IndigoObject rxn = indigo.createReaction();
		for (String productSmiles : products) {
			IndigoObject mol = indigo.loadMolecule(productSmiles);
			mol.foldHydrogens();
			mol.aromatize();
			rxn.addProduct(mol);
		}
		
		int i = 0;
		for (IndigoObject product : rxn.iterateProducts()) {
			product.addDataSGroup(new int[0], new int[0], "smiles", products.get(i));
			i++;
		}
		
		for (String reactantSmiles : reactants) {
			IndigoObject mol = indigo.loadMolecule(reactantSmiles);
			mol.foldHydrogens();
			mol.aromatize();
			rxn.addReactant(mol);
		}

		i = 0;
		for (IndigoObject reactant : rxn.iterateReactants()) {
			reactant.addDataSGroup(new int[0], new int[0], "smiles", reactants.get(i));
			i++;
		}
		
		for (String spectatorSmiles : spectators) {
			IndigoObject mol = indigo.loadMolecule(spectatorSmiles);
			mol.foldHydrogens();
			mol.aromatize();
			rxn.addCatalyst(mol);
		}
		
		i = 0;
		for (IndigoObject catalyst : rxn.iterateCatalysts()) {
			catalyst.addDataSGroup(new int[0], new int[0], "smiles", spectators.get(i));
			i++;
		}
		
		return rxn;
	}
	
	public static void serializeReactions(File directory, Map<Reaction, IndigoObject> reactionMap) throws IOException {
		if (!directory.exists()){
			FileUtils.forceMkdir(directory);
		}
		if (!directory.isDirectory()){
			throw new IllegalArgumentException("A directory was expected");
		}
		Map<String, Integer> identifierToCount = new HashMap<String, Integer>();//all files names must be unique with a patent!
		Set<Entry<Reaction, IndigoObject>> entries = reactionMap.entrySet();
		for (Entry<Reaction, IndigoObject> entry : entries) {
			Reaction reaction = entry.getKey();
			IndigoObject indigoReaction = entry.getValue();
			String identifier = reaction.getInput().getIdentifier();//may be null for non USPTO documents
			if (identifierToCount.get(identifier) == null){
				identifierToCount.put(identifier, 1);
			}
			String paraIdent = identifier != null ? identifier : "";
			Integer subParaIdent = identifierToCount.get(identifier);
			identifierToCount.put(identifier, subParaIdent + 1);
			try {
				File f = new File(directory, "reaction" + paraIdent +"_" + subParaIdent + ".png");
				ReactionDepicter.depictReaction(indigoReaction, f);
					FileOutputStream in = new FileOutputStream(new File(directory, "reaction" + paraIdent +"_" + subParaIdent + "src.xml"));
				    Serializer serializer = new Serializer(in);
					serializer.setIndent(2);
					serializer.write(reaction.getInput().getTaggedSentencesDocument());
					IOUtils.closeQuietly(in);
					
				FileOutputStream out = new FileOutputStream(new File(directory, "reaction" + paraIdent +"_" + subParaIdent + ".cml"));
			    serializer = new Serializer(out);
				serializer.setIndent(2);
				serializer.write(new Document(reaction.toCML()));
				IOUtils.closeQuietly(out);	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Reads a file. For every line that is neither empty or starts with a tab
	 * the line is split on tab and the first part added to the set of strings to be returned
	 * @param fileLocation
	 * @return 
	 */
	public static Set<String> fileToStringSet(String fileLocation) {
		Set<String> strings = new HashSet<String>();
		InputStream is = Utils.class.getResourceAsStream(fileLocation);
		if (is ==null){
			throw new RuntimeException("Failed to read " + fileLocation);
		}
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#") || line.equals("")){
					continue;
				}
				strings.add(matchTab.split(line)[0]);
			}
		}
		catch (IOException e ) {
			throw new RuntimeException("Failed to read " + fileLocation, e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
		return strings;
	}
}

package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ggasoftware.indigo.IndigoObject;

import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLTags;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import nu.xom.Document;
import nu.xom.Element;

public class ReactionExtractor {

	private final List<Element> headingsAndParagraphs;
	private final Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();
	private final Map<Reaction, IndigoObject> documentReactions = new LinkedHashMap<Reaction, IndigoObject>();
	private final Map<Reaction, IndigoObject> completeReactions = new LinkedHashMap<Reaction, IndigoObject>();

	/**
	 * Convenience constructor for extracting reactions from a USPTO patent as a XOM document
	 * @param usptoPatentDoc
	 */
	public ReactionExtractor(Document usptoPatentDoc) {
		this(getHeadingsAndParagraphsFromUSPTOPatent(usptoPatentDoc));
	}
	
	/**
	 * A list of headings and paragraphs from which reactions should be extracted should be provided
	 * These should be ordered in the order they would be in the source document
	 * @param orderedHeadingsAndParagraphs
	 */
	public ReactionExtractor(List<Element> orderedHeadingsAndParagraphs) {
		List<Element> headingsAndParagraphsCopy = new ArrayList<Element>();//defensively copy so as to allow modification and rearrangement of the XML
		for (Element element : orderedHeadingsAndParagraphs) {
			headingsAndParagraphsCopy.add(new Element(element));
		}
		headingsAndParagraphs = headingsAndParagraphsCopy;
	}
	
	/**
	 * Gets all reactions that looks to be chemically reasonable e.g. all product atoms have a corresponding reactant atom
	 * @return
	 */
	public Map<Reaction, IndigoObject> getAllCompleteReactions() {
		return completeReactions;
	}
	
	/**
	 * Gets every reaction found by the extractor. Many will be malformed, useful for debugging
	 * @return
	 */
	public Map<Reaction, IndigoObject> getAllFoundReactions() {
		return documentReactions;
	}
	
	/**
	 * Performs reaction extraction
	 * The getter methods of this class will return null prior to this being run
	 */
	public void extractReactions(){
		ExperimentalSectionsCreator sectionsCreator = new ExperimentalSectionsCreator(headingsAndParagraphs);
		List<ExperimentalSection> experimentalSections = sectionsCreator.createSections();
		for (ExperimentalSection experimentalSection : experimentalSections) {
			ExperimentalSectionParser sectionParser = new ExperimentalSectionParser(experimentalSection, aliasToChemicalMap);
			sectionParser.parseForReactions();
			List<Reaction> reactions  = sectionParser.getSectionReactions();
			for (Reaction reaction : reactions) {
				IndigoObject indigoReaction = Utils.createIndigoReaction(reaction);
				documentReactions.put(reaction, indigoReaction);
				if (reactionAppearsFeasible(reaction, indigoReaction)){//TODO extract functionality
					completeReactions.put(reaction, indigoReaction);
				}
			}
		}
	}
	
	/**
	 * Performs a few sanity checks e.g. at least 2 reactants and 1 product and that the product isn't a reactant
	 * Then performs atom by atom mapping to check that all atoms in the product are accounted for
	 * @param reaction
	 * @param indigoReaction
	 * @return
	 */
	private boolean reactionAppearsFeasible(Reaction reaction,IndigoObject indigoReaction) {
		if (reactantsContainsProduct(reaction)){
			return false;
		}
		if (indigoReaction.countReactants() < 2 || indigoReaction.countProducts() < 1 ){
			return false;
		}
		ReactionMapper mapper = new ReactionMapper(indigoReaction);
		if (!mapper.mapReaction()){
			return false;
		}
		return mapper.allProductAtomsAreMapped();
	}

	/**
	 * Uses InChIs to check whether any of the products are also reactants 
	 * @param reaction
	 * @return
	 */
	private boolean reactantsContainsProduct(Reaction reaction) {
		List<Chemical> products =reaction.getProducts();
		List<String> productInChIs = new ArrayList<String>();
		for (Chemical product : products) {
			if (product.getInchi() != null){
				productInChIs.add(product.getInchi());
			}
		}
		for (Chemical reactant : reaction.getReactants()) {
			if (productInChIs.contains(reactant.getInchi())){
				return true;
			}
		}
		return false;
	}

	private static List<Element> getHeadingsAndParagraphsFromUSPTOPatent(Document usptoPatentDoc) {
		Element description = usptoPatentDoc.getRootElement().getFirstChildElement(XMLTags.DESCRIPTION);
		if (description ==null){
			throw new RuntimeException("Malformed USPTO patent, no \"description\" element found");
		}
		return XOMTools.getChildElementsWithTagNames(description, new String[]{XMLTags.HEADING, XMLTags.P});
	}
}

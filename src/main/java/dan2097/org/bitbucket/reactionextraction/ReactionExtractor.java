package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import nu.xom.Document;
import nu.xom.Element;
import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import com.ggasoftware.indigo.IndigoException;
import com.ggasoftware.indigo.IndigoObject;

import dan2097.org.bitbucket.utility.IndigoHolder;
import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLTags;

public class ReactionExtractor {

	private final List<Element> headingsAndParagraphs;
	private final PreviousReactionData previousReactionData = new PreviousReactionData();
	private final Map<Reaction, IndigoObject> documentReactions = new LinkedHashMap<Reaction, IndigoObject>();
	private final Map<Reaction, IndigoObject> completeReactions = new LinkedHashMap<Reaction, IndigoObject>();
	private static Logger LOG = Logger.getLogger(ReactionExtractor.class);

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
		IndigoHolder.getInstance().setOption("aam-timeout", 15000);
	}
	
	/**
	 * Allows the timeout on reaction mapping to be set. 60,000 milliseconds by default
	 * On most systems can be reduced to 10,000 with minimal impact on results
	 * @param milliseconds
	 */
	public void setIndigoAtomMappingTimeout(int milliseconds){
		IndigoHolder.getInstance().setOption("aam-timeout", milliseconds);
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
			ExperimentalSectionParser sectionParser = new ExperimentalSectionParser(experimentalSection, previousReactionData);
			List<Reaction> reactions  = sectionParser.parseForReactions();
			for (Reaction reaction : reactions) {
				try{
					IndigoObject indigoReaction = Utils.convertToIndigoReaction(reaction);
					if (reactionIsSane(reaction, indigoReaction)){
						if (reactionIsMappable(indigoReaction)){
							new ReactionStoichiometryDeterminer(reaction, indigoReaction).processReactionStoichiometry();
							completeReactions.put(reaction, indigoReaction);
						}
						else {
							IndigoObject modifiedReaction = attemptToProduceMappableReactionByRoleReclassification(reaction);
							if (modifiedReaction!=null){
								indigoReaction = modifiedReaction;
								new ReactionStoichiometryDeterminer(reaction, indigoReaction).processReactionStoichiometry();
								completeReactions.put(reaction, indigoReaction);
							}
						}
					}
					documentReactions.put(reaction, indigoReaction);
					reaction.setReactionSmiles(indigoReaction.smiles());
				}
				catch (IndigoException e) {
					LOG.warn("Indigo threw an exception whilst handling an extracted reaction! The reaction has been ignored", e);
				}
			}
		}
	}

	/**
	 * Attempts to reclassify a solvent as a reactant to fully map the reaction
	 * The output is an indigo reaction of the modified reaction
	 * NOTE if this function is successful the input Reaction will have been modified
	 * @param reaction
	 * @return
	 */
	private IndigoObject attemptToProduceMappableReactionByRoleReclassification(Reaction reaction) {
		Set<String> seenInChIs = new HashSet<String>();
		List<Chemical> spectators = reaction.getSpectators();
		for (int i = 0; i < spectators.size(); i++) {
			Chemical spectator = spectators.get(i);
			if (spectator.hasInchi() && spectator.getRole().equals(ChemicalRole.solvent) &&
					!seenInChIs.contains(spectator.getInchi())){
				String inchi = spectator.getInchi();
				seenInChIs.add(inchi);
				List<Chemical> solventsToRecategorise = new ArrayList<Chemical>();
				for (Chemical solventToRecategorise : spectators) {
					if (inchi.equals(solventToRecategorise.getInchi())){
						solventsToRecategorise.add(solventToRecategorise);
					}
				}

				Reaction newReaction = new Reaction();
				for (Chemical product : reaction.getProducts()) {
					newReaction.addProduct(product);
				}
				for (Chemical spec : reaction.getSpectators()) {
					if (!solventsToRecategorise.contains(spec)){
						newReaction.addSpectator(spec);
					}
				}
				for (Chemical reactant : reaction.getReactants()) {
					newReaction.addReactant(reactant);
				}
				for (Chemical solventToRecategorise : solventsToRecategorise) {
					newReaction.addReactant(solventToRecategorise);
				}
				IndigoObject indigoReaction = Utils.convertToIndigoReaction(newReaction);
				if (reactionIsMappable(indigoReaction)){
					for (Chemical solventToRecategorise : solventsToRecategorise) {
						solventToRecategorise.setRole(ChemicalRole.reactant);
						reaction.removeSpectator(solventToRecategorise);
						reaction.addReactant(solventToRecategorise);
					}
					return indigoReaction;
				}
			}
		}
		return null;
	}

	private boolean reactionIsMappable(IndigoObject indigoReaction) {
		ReactionMapper mapper = new ReactionMapper(indigoReaction);
		if (!mapper.mapReaction()){
			return false;
		}
		return mapper.allProductAtomsAreMapped();
	}

	/**
	 * Performs a few sanity checks:
	 * A least 1 product
	 * There are at least two reagents
	 * The product/s are not all reactants
	 * @param reaction
	 * @param indigoReaction
	 * @return
	 */
	private boolean reactionIsSane(Reaction reaction, IndigoObject indigoReaction) {
		if (indigoReaction.countProducts() ==0){
			return false;
		}
		if (indigoReaction.countReactants() + indigoReaction.countCatalysts() < 2){
			return false;
		}
		if (reagentsContainProducts(reaction)){
			return false;
		}
		return true;
	}

	/**
	 * Uses InChIs to check whether all of the products are also reagents 
	 * @param reaction
	 * @return
	 */
	private boolean reagentsContainProducts(Reaction reaction) {
		List<Chemical> products =reaction.getProducts();
		Set<String> productInChIs = new HashSet<String>();
		for (Chemical product : products) {
			if (product.getInchi() != null){
				productInChIs.add(product.getInchi());
			}
		}
		if (productInChIs.isEmpty()){
			return false;
		}
		for (Chemical reactant : reaction.getReactants()) {
			productInChIs.remove(reactant.getInchi());
		}
		for (Chemical spectator : reaction.getSpectators()) {
			productInChIs.remove(spectator.getInchi());
		}
		return productInChIs.isEmpty();
	}

	private static List<Element> getHeadingsAndParagraphsFromUSPTOPatent(Document usptoPatentDoc) {
		Element description = usptoPatentDoc.getRootElement().getFirstChildElement(XMLTags.DESCRIPTION);
		if (description ==null){
			throw new RuntimeException("Malformed USPTO patent, no \"description\" element found");
		}
		return XOMTools.getChildElementsWithTagNames(description, new String[]{XMLTags.HEADING, XMLTags.P});
	}
}

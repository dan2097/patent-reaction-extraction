package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ggasoftware.indigo.IndigoObject;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLAtrs;
import dan2097.org.bitbucket.utility.XMLTags;

public class ExperimentalParser {
	private final Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();
	private final  Map<Reaction, IndigoObject> documentReactions = new LinkedHashMap<Reaction, IndigoObject>();
	private final Map<Reaction, IndigoObject> completeReactions = new LinkedHashMap<Reaction, IndigoObject>();

	public Map<Reaction, IndigoObject> getAllFoundReactions() {
		return documentReactions;
	}
	
	public Map<Reaction, IndigoObject> getAllCompleteReactions() {
		return completeReactions;
	}

	public void parseExperimentalSection(Element headingElementToProcess) {
		String title = headingElementToProcess.getAttributeValue(XMLAtrs.TITLE);
		Chemical titleCompound = Utils.extractResolvableChemicalFromHeading(title);
		if (titleCompound==null){
			return;
		}
		String alias = TitleTextAliasExtractor.findAlias(title);
		if (alias !=null){
			aliasToChemicalMap.put(alias, titleCompound);
		}
		ExperimentalSectionParser sectionparser = new ExperimentalSectionParser(titleCompound, XOMTools.getChildElementsWithTagName(headingElementToProcess, XMLTags.P), aliasToChemicalMap);
		sectionparser.parseForReactions();
		List<Reaction> reactions = sectionparser.getReactions();
		new ReactionStoichiometryDeterminer(reactions).processReactionStoichiometry();
		for (Reaction reaction : reactions) {
			IndigoObject indigoReaction = Utils.createIndigoReaction(reaction);
			documentReactions.put(reaction, indigoReaction);
			if (reactionAppearsFeasible(reaction, indigoReaction)){
				completeReactions.put(reaction, indigoReaction);
			}
		}
	}

	/**
	 * Performs a few santity checks e.g. at least 2 reactants and 1 product and that the product isn't a reactant
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
}

package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
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
	private final List<Reaction> documentReactions = new ArrayList<Reaction>();
	private final List<Reaction> completeReactions = new ArrayList<Reaction>();

	public List<Reaction> getAllFoundReactions() {
		return documentReactions;
	}
	
	public List<Reaction> getAllCompleteReactions() {
		throw new RuntimeException("Disabled whilst bugs in indigo are fixed");
		//return completeReactions;
	}

	public void parseExperimentalSection(Element headingElementToProcess) {
		String title = headingElementToProcess.getAttributeValue(XMLAtrs.TITLE);
		Chemical titleCompound = Utils.extractChemicalFromHeading(title);
		String alias = TitleTextAliasExtractor.findAlias(title);
		if (alias !=null){
			aliasToChemicalMap.put(alias, titleCompound);
		}
		ExperimentalSectionParser sectionparser = new ExperimentalSectionParser(titleCompound, XOMTools.getChildElementsWithTagName(headingElementToProcess, XMLTags.P), aliasToChemicalMap);
		sectionparser.parseForReactions();
		List<Reaction> reactions = sectionparser.getReactions();
		new ReactionStoichiometryDeterminer(reactions).processReactionStoichiometry();
		documentReactions.addAll(reactions);
		//completeReactions.addAll(determineCompleteReactions(reactions));
	}

	private List<Reaction> determineCompleteReactions(List<Reaction> reactions) {
		List<Reaction> validReactions = new ArrayList<Reaction>();
		for (Reaction reaction : reactions) {
			if (reactantsContainsProduct(reaction)){
				continue;
			}
			IndigoObject indigoReaction = Utils.createIndigoReaction(reaction);
			if (indigoReaction.countReactants() < 2 || indigoReaction.countProducts() < 1 ){
				continue;
			}
			ReactionMapper mapper = new ReactionMapper(indigoReaction);
			if (!mapper.mapReaction()){
				continue;
			}
			if (mapper.allProductAtomsAreMapped()){
				validReactions.add(reaction);
			}
		}
		return validReactions;
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

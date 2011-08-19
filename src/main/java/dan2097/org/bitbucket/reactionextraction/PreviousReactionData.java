package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds a mapping between procedures and extracted reactions
 * A document specific alias to chemical map is also kept
 * @author dl387
 *
 */
public class PreviousReactionData {
	
	private static class StepReactionHolder {
		private final String identifier;
		private final List<Reaction> reactions;

		/**
		 * Describes the reactions for a certain step. Identifer can be null if this step doesn't have a label
		 * @param reactions
		 * @param identifier
		 */
		public StepReactionHolder(List<Reaction> reactions, String identifier) {
			this.reactions = reactions;
			this.identifier = identifier;
		}
		
		/**
		 * Gets the identifier for this step, could be null if step is unnamed or there only was one step
		 * @return
		 */
		String getIdentifier() {
			return identifier;
		}

		/**
		 * Gets the reactions found for this step
		 * @return
		 */
		List<Reaction> getReactions() {
			return reactions;
		}
	} 

	private final Map<String, List<StepReactionHolder>> sectionIdentifierToSectionReactions = new HashMap<String, List<StepReactionHolder>>();
	private final Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();

	Map<String, Chemical> getAliasToChemicalMap() {
		return aliasToChemicalMap;
	}
	/**
	 * Records the reactions for a given section and step.
	 * The step may be null allowing for cases with cases with anonymous steps or only one step
	 * @param reactions
	 * @param section
	 * @param step
	 */
	void addReactions(List<Reaction> reactions, String section, String step){
		if (reactions ==null){
			throw new IllegalArgumentException("Secti");
		}
		if (section ==null){
			throw new IllegalArgumentException("Null input paramaet");
		}
		List<StepReactionHolder> stepHolders;
		if (sectionIdentifierToSectionReactions.get(section)==null){
			stepHolders = new ArrayList<StepReactionHolder>();
		}
		else{
			stepHolders = sectionIdentifierToSectionReactions.get(section);
		}
		stepHolders.add(new StepReactionHolder(reactions, step));
		sectionIdentifierToSectionReactions.put(section, stepHolders);
	}
	
	/**
	 * Returns reactions associated with the given section.
	 * 
	 * Null if this section identifier has not been encountered
	 * @param sectionIdentifier
	 * @return
	 */
	List<Reaction> getReactions(String sectionIdentifier){
		List<Reaction> reactions = new ArrayList<Reaction>();
		List<StepReactionHolder> stepReactionHolders = sectionIdentifierToSectionReactions.get(sectionIdentifier);
		if (stepReactionHolders ==null){
			return null;
		}
		for (StepReactionHolder stepReactionHolder : stepReactionHolders) {
			reactions.addAll(stepReactionHolder.getReactions());
		}
		return reactions;
	}
	
	/**
	 * Null if the given section identifier or stepIdentifier has not been encountered
	 * @param sectionIdentifier
	 * @param stepIdentifier
	 * @return
	 */
	List<Reaction> getReactions(String sectionIdentifier, String stepIdentifier){
		List<StepReactionHolder> stepReactionHolders = sectionIdentifierToSectionReactions.get(sectionIdentifier);
		if (stepReactionHolders ==null){
			return null;
		}
		if (stepIdentifier ==null && stepReactionHolders.size()>0){
			return stepReactionHolders.get(stepReactionHolders.size()-1).getReactions();
		}
		for (StepReactionHolder stepReactionHolder : stepReactionHolders) {
			if (stepIdentifier.equals(stepReactionHolder.getIdentifier())){
				return stepReactionHolder.getReactions();
			}
		}
		return null;
	}
	
	/**
	 * Attempts to get the product of a given section.
	 * Returns null if the section doesn't exist or has multiple products
	 * @param sectionIdentifier
	 * @return
	 */
	Chemical getProductOfReaction(String sectionIdentifier) {
		List<Reaction> reactions = getReactions(sectionIdentifier);
		if (reactions == null || reactions.isEmpty()){
			return null;
		}
		List<Chemical> products = reactions.get(reactions.size()-1).getProducts();
		if (products.size()==1){
			return products.get(0);
		}
		return null;
	}
	
	/**
	 * Attempts to get the product of a given section.
	 * Returns null if the section or step doesn't exist or has multiple products
	 * @param sectionIdentifier
	 * @param stepIdentifier
	 * @return
	 */
	Chemical getProductOfReaction(String sectionIdentifier, String stepIdentifier) {
		List<Reaction> reactions = getReactions(sectionIdentifier, stepIdentifier);
		if (reactions == null || reactions.isEmpty()){
			return null;
		}
		List<Chemical> products = reactions.get(reactions.size()-1).getProducts();
		if (products.size()==1){
			return products.get(0);
		}
		return null;
		
	}
}

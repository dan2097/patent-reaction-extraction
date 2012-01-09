package dan2097.org.bitbucket.reactionextraction;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ggasoftware.indigo.IndigoObject;



public class ReactionStoichiometryDeterminer {
	private final Reaction reaction;
	private final IndigoObject indigoReaction;

	ReactionStoichiometryDeterminer(Reaction reaction, IndigoObject indigoReaction) {
		this.reaction = reaction;
		this.indigoReaction = indigoReaction;
	}
	
	
	/**
	 * Uses the atom mapping to determine the stoichiometry of reactants used.
	 * The property "smiles" in the Indigo reactions is used to link the CML representation with the Indigo representation
	 */
	void processReactionStoichiometry() {
		Map<Integer, IndigoObject> mappingNumberToReactant = new HashMap<Integer, IndigoObject>();
		for (IndigoObject reactant : indigoReaction.iterateReactants()) {
			for (IndigoObject atom : reactant.iterateAtoms()) {
				mappingNumberToReactant.put(indigoReaction.atomMappingNumber(atom), reactant);
			}
		}
		Map<Integer, Integer> mappingNumberToTimesUsed= new HashMap<Integer, Integer>();
		for (IndigoObject product : indigoReaction.iterateProducts()) {
			for (IndigoObject atom : product.iterateAtoms()) {
				int number = indigoReaction.atomMappingNumber(atom);
				if (number != 0){
					if (mappingNumberToTimesUsed.get(number)==null){
						mappingNumberToTimesUsed.put(number, 1);
					}
					else{
						mappingNumberToTimesUsed.put(number, mappingNumberToTimesUsed.get(number) +1);
					}
				}
			}
		}
		Map<IndigoObject, Integer> timesReactantUsed = new HashMap<IndigoObject, Integer>();
		for (Entry<Integer, Integer> mappingNumberToTimesUsedEntry : mappingNumberToTimesUsed.entrySet()) {
			IndigoObject reactant = mappingNumberToReactant.get(mappingNumberToTimesUsedEntry.getKey());
			if (timesReactantUsed.get(reactant)==null){
				timesReactantUsed.put(reactant, mappingNumberToTimesUsedEntry.getValue());
			}
			else{
				int timesUsed = timesReactantUsed.get(reactant);
				if (mappingNumberToTimesUsedEntry.getValue() > timesUsed){
					timesReactantUsed.put(reactant, mappingNumberToTimesUsedEntry.getValue());
				}
			}
		}
		for (Entry<IndigoObject, Integer> timesReactantUsedEntry : timesReactantUsed.entrySet()) {
			String smiles =null;
			for (IndigoObject data : timesReactantUsedEntry.getKey().iterateDataSGroups()) {
				if (data.description().equals("smiles")){
					smiles = data.rawData();
					break;
				}
			}
			if (smiles ==null){
				throw new RuntimeException("Unable to find SMILES in S-group for indigio reactant!");
			}
			boolean setStoichiometry = false;
			for (Chemical reactant : reaction.getReactants()) {
				if (smiles.equals(reactant.getSmiles())){
					reactant.setStoichiometry(timesReactantUsedEntry.getValue());
					setStoichiometry = true;
					break;
				}
			}
			if (!setStoichiometry){
				throw new RuntimeException("Unable to find molecule in CML with same SMILES used to create indigo reaction!");
			}
		}
	}

}

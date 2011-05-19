package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ggasoftware.indigo.IndigoObject;

import static dan2097.org.bitbucket.utility.Utils.*;

public class ChemicalSenseApplication {

	private final Reaction reaction;
	private List<IndigoObject> products = new ArrayList<IndigoObject>();
	
	ChemicalSenseApplication(Reaction reaction) {
		this.reaction = reaction;
		for (Chemical product : reaction.getProducts()) {
			if (product.getSmiles()!=null){
				products.add(indigo.loadMolecule(product.getSmiles()));
			}
		}
	}

	void reassignMisCategorisedReagents() {
		correctReactantsThatAreCatalysts();
		correctReactantsThatAreSolvents();
	}

	void correctReactantsThatAreCatalysts() {
		List<Chemical> reactantsToReclassify = new ArrayList<Chemical>();
		for (Chemical reactant : reaction.getReactants()) {
			if (reactant.getSmiles()!=null){
				IndigoObject reactantMol = indigo.loadMolecule(reactant.getSmiles());
				List<Integer> transitionMetalInChemical = new ArrayList<Integer>();
				for (Iterator<IndigoObject> iterator = reactantMol.iterateAtoms(); iterator.hasNext();) {
					IndigoObject atom = iterator.next();
					if (!atom.isRSite() && isTransitionMetal(atom.atomicNumber())){
						transitionMetalInChemical.add(atom.atomicNumber());
					}
				}
				
				for (int inorganicAtomNum : transitionMetalInChemical) {
					boolean foundAtom =false;
					productLoop: for (IndigoObject product : products) {
						for (Iterator<IndigoObject> iterator = product.iterateAtoms(); iterator.hasNext();) {
							IndigoObject atom = iterator.next();
							if (!atom.isRSite() && atom.atomicNumber() == inorganicAtomNum){
								foundAtom =true;
								break productLoop;
							}
						}
					}
					if (!foundAtom){
						reactantsToReclassify.add(reactant);
						break;
					}
				}
			}
		}
		for (Chemical catalyst : reactantsToReclassify) {
			catalyst.setRole(ChemicalRole.catalyst);
			reaction.removeReactant(catalyst);
			reaction.addSpectator(catalyst);
		}
	}

	/**
	 * Is the atom a transition metal
	 * @param atomicNumber
	 * @return
	 */
	private boolean isTransitionMetal(int atomicNumber) {
		return (atomicNumber >=21 && atomicNumber <=30) ||
			(atomicNumber >=39 && atomicNumber <=48) ||
			(atomicNumber >=72 && atomicNumber <=80);
	}

	/**
	 * Applies the heuristic that the same chemical cannot be both a reactant and solvent
	 * to reclassify such "reactants" as solvents
	 */
	void correctReactantsThatAreSolvents() {
		List<String> solventInChIs = new ArrayList<String>();
		for (Chemical spectator : reaction.getSpectators()) {
			if (ChemicalRole.solvent.equals(spectator.getRole()) && spectator.getInchi() != null){
				solventInChIs.add(spectator.getInchi());
			}
		}
		List<Chemical> reactants = reaction.getReactants();
		for (int i = reactants.size()-1; i >=0; i--) {
			Chemical reactant = reactants.get(i);
			if (solventInChIs.contains(reactant.getInchi())){
				reactant.setRole(ChemicalRole.solvent);
				reaction.removeReactant(reactant);
				reaction.addSpectator(reactant);
			}
		}
	}
}

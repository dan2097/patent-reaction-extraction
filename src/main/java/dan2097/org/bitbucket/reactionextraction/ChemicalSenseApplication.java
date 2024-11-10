package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoObject;

import dan2097.org.bitbucket.utility.IndigoHolder;
import dan2097.org.bitbucket.utility.Utils;

public class ChemicalSenseApplication {
	private static final Indigo indigo = IndigoHolder.getInstance();
	private static final Logger LOG = Logger.getLogger(ChemicalSenseApplication.class);

	private final Reaction reaction;
	
	ChemicalSenseApplication(Reaction reaction) {
		this.reaction = reaction;
	}

	/**
	 * Keeps the last product with a particular InChI, unless a previous one has quantities and the last doesn't
	 * then the latest one with quantities is retained
	 */
	void mergeProductsByInChI() {
		List<Chemical> products = reaction.getProducts();
		Map<String, Chemical> inchiToProduct = new HashMap<String, Chemical>();
		for (int i = products.size() -1; i >=0; i--) {
			Chemical product = products.get(i);
			String inchi = product.getInchi();
			if (inchiToProduct.containsKey(inchi)){
				if (!inchiToProduct.get(inchi).hasAQuantity() && product.hasAQuantity()){
					reaction.removeProduct(inchiToProduct.get(inchi));
					inchiToProduct.put(inchi, product);
				}
				else{
					reaction.removeProduct(product);
				}
			}
			else{
				inchiToProduct.put(inchi, product);
			}
		}
	}

	void reassignMisCategorisedReagents() {
		correctReactantsThatAreCatalysts();
		correctReactantsThatAreSolvents();
	}

	void correctReactantsThatAreCatalysts() {
		try{
			List<IndigoObject> products = new ArrayList<IndigoObject>();
			for (Chemical product : reaction.getProducts()) {
				if (product.getSmiles() != null){
					products.add(indigo.loadMolecule(product.getSmiles()));
				}
			}
			List<Chemical> reactantsToReclassify = new ArrayList<Chemical>();
			for (Chemical reactant : reaction.getReactants()) {
				if (reactant.getSmiles() != null){
					IndigoObject reactantMol = indigo.loadMolecule(reactant.getSmiles());
					List<Integer> transitionMetalInChemical = new ArrayList<Integer>();
					for (Iterator<IndigoObject> iterator = reactantMol.iterateAtoms(); iterator.hasNext();) {
						IndigoObject atom = iterator.next();
						if (!atom.isRSite() && isTransitionMetal(atom.atomicNumber()) && isAllowedTransitionMetal(atom)){
							transitionMetalInChemical.add(atom.atomicNumber());
						}
					}
					
					for (int inorganicAtomNum : transitionMetalInChemical) {
						boolean foundAtom = false;
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
		catch (IndigoException e) {
			LOG.warn("Indigo threw an exception whilst loading the constituents of a reaction fromm SMILES", e);
		}
	}

	/**
	 * A few exceptions e.g. Cr(VI) for dichromate
	 * @param atom 
	 * @return
	 */
	private boolean isAllowedTransitionMetal(IndigoObject atom) {
		int atomicNumber = atom.atomicNumber();
		if(atomicNumber == 24 && atom.valence() == 6){
			return false;
		}
		if(atomicNumber == 25 && atom.valence() >= 6){
			return false;
		}
		if((atomicNumber == 29 || atomicNumber == 30 || atomicNumber == 80) && hasBondToCarbon(atom)){//organocopper/zinc/mercury compounds are often reactants
			return false;
		}
		return true;
	}

	private boolean hasBondToCarbon(IndigoObject atom) {
		for (IndigoObject neighbour : atom.iterateNeighbors()) {
			if (neighbour.atomicNumber() == 6){
				return true;
			}
		}
		return false;
	}

	/**
	 * Is the atom a transition metal
	 * @param atomicNumber
	 * @return
	 */
	private boolean isTransitionMetal(int atomicNumber) {
		return (atomicNumber >=21 && atomicNumber <= 30) ||
			(atomicNumber >=39 && atomicNumber <= 48) ||
			(atomicNumber >=72 && atomicNumber <= 80);
	}


	void correctReactantsThatAreSolvents() {
		Set<String> solventInChIs = getSolventInChIs();
		classifyReactantsThatAreAlsoSolventsAsSolvent(solventInChIs);
		classifyReactantAsSolventsUsingAprioriKnowledge();
	}



	private Set<String> getSolventInChIs() {
		Set<String> solventInChIs = new HashSet<String>();
		for (Chemical spectator : reaction.getSpectators()) {
			if (ChemicalRole.solvent.equals(spectator.getRole()) && spectator.getInchi() != null){
				solventInChIs.add(spectator.getInchi());
			}
		}
		return solventInChIs;
	}

	/**
	 * Applies the heuristic that the same chemical cannot be both a reactant and solvent
	 * to reclassify such "reactants" as solvents
	 * @param solventInChIs 
	 */
	private void classifyReactantsThatAreAlsoSolventsAsSolvent(Set<String> solventInChIs) {
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

	private void classifyReactantAsSolventsUsingAprioriKnowledge() {
		boolean hasSolvent = getSolventInChIs().size() > 0;
		List<Chemical> reactants = reaction.getReactants();
		Set<String> newSolventInChIs = new HashSet<String>();
		for (int i = reactants.size()-1; i >=0; i--) {
			Chemical reactant = reactants.get(i);
			if ((!hasSolvent || moreThan4UniqueReactantStructures()) && ReactionExtractionMethods.isKnownSolvent(reactant) && !reactant.hasAmountOrEquivalentsOrYield()){
				reactant.setRole(ChemicalRole.solvent);
				reaction.removeReactant(reactant);
				reaction.addSpectator(reactant);
				newSolventInChIs.add(reactant.getInchi());
				hasSolvent = true;
			}
		}
		if (!hasSolvent){
			for (int i = reactants.size()-1; i >=0; i--) {
				Chemical reactant = reactants.get(i);
				if (!hasSolvent && reactant.getVolumeValue() != null && !reactant.hasAmountOrEquivalentsOrYield() && reactant.hasImpreciseVolume()){
					//solvents will be liquids but typically with imprecise volume and no amount given
					reactant.setRole(ChemicalRole.solvent);
					reaction.removeReactant(reactant);
					reaction.addSpectator(reactant);
					String inchi = reactant.getInchi();
					if (inchi != null){
						newSolventInChIs.add(inchi);
					}
					hasSolvent = true;
				}
			}
		}
		if (!newSolventInChIs.isEmpty()){
			classifyReactantsThatAreAlsoSolventsAsSolvent(newSolventInChIs);
		}
	}

	private boolean moreThan4UniqueReactantStructures() {
		List<Chemical> reactants = reaction.getReactants();
		if (reactants.size() > 4){
			return Utils.getSmilesForUniqueStructuresUsingInChIs(reactants).size() > 4;
		}
		return false;
	}
}

package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;

import dan2097.org.bitbucket.utility.IndigoHolder;

public class ChemicalSenseApplication {

	private final Reaction reaction;
	private Indigo indigo = IndigoHolder.getInstance();
	private static AprioriKnowledge chemicalKnowledge = AprioriKnowledge.getInstance();
	
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
		List<IndigoObject> products = new ArrayList<IndigoObject>();
		for (Chemical product : reaction.getProducts()) {
			if (product.getSmiles()!=null){
				products.add(indigo.loadMolecule(product.getSmiles()));
			}
		}
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


	void correctReactantsThatAreSolvents() {
		Set<String> solventInChIs = getSolventInChIs();
		classifyReactantsThatAreAlsoSolventsAsSolvent(solventInChIs);
		classifyReactantAsSolventsUsingAprioriKnowledge();
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

	private Set<String> getSolventInChIs() {
		Set<String> solventInChIs = new HashSet<String>();
		for (Chemical spectator : reaction.getSpectators()) {
			if (ChemicalRole.solvent.equals(spectator.getRole()) && spectator.getInchi() != null){
				solventInChIs.add(spectator.getInchi());
			}
		}
		return solventInChIs;
	}

	private void classifyReactantAsSolventsUsingAprioriKnowledge() {
		boolean hasSolvent = getSolventInChIs().size()>0;
		List<Chemical> reactants = reaction.getReactants();
		Set<String> newSolventInChIs = new HashSet<String>();
		for (int i = reactants.size()-1; i >=0; i--) {
			Chemical reactant = reactants.get(i);
			String inchi = reactant.getInchi();
			if (!hasSolvent && chemicalKnowledge.isKnownSolventInChI(inchi) && reactant.getAmountValue()==null && reactant.getEquivalents()==null){
				reactant.setRole(ChemicalRole.solvent);
				reaction.removeReactant(reactant);
				reaction.addSpectator(reactant);
				newSolventInChIs.add(inchi);
				hasSolvent = true;
			}
		}
		if (!hasSolvent){
			for (int i = reactants.size()-1; i >=0; i--) {
				Chemical reactant = reactants.get(i);
				String inchi = reactant.getInchi();
				if (!hasSolvent && reactant.getVolumeValue()!=null && reactant.getAmountValue()==null && reactant.getEquivalents()==null && reactant.hasImpreciseVolume()){
					//solvents will be liquids but typically with imprecise volume and no amount given
					reactant.setRole(ChemicalRole.solvent);
					reaction.removeReactant(reactant);
					reaction.addSpectator(reactant);
					newSolventInChIs.add(inchi);
					hasSolvent = true;
				}
			}
		}
		if (!newSolventInChIs.isEmpty()){
			classifyReactantsThatAreAlsoSolventsAsSolvent(newSolventInChIs);
		}
	}
}

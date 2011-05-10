package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class Reaction {

	private List<Chemical> reactants =new ArrayList<Chemical>();
	private List<Chemical> products =new ArrayList<Chemical>();
	/** Typically solvents or catalysts*/
	private List<Chemical> spectators =new ArrayList<Chemical>();
	List<Paragraph> input;

	public void setInput(List<Paragraph> taggedSentencesDocument) {
		input =taggedSentencesDocument;	
	}
	public List<Paragraph> getInput() {
		return input;
	}
	
	List<Chemical> getReactants() {
		return reactants;
	}
	void addReactant(Chemical reactant) {
		reactants.add(reactant);
	}
	
	boolean removeReactant(Chemical reactant) {
		return reactants.remove(reactant);
	}
	
	List<Chemical> getProducts() {
		return products;
	}
	void addProduct(Chemical product) {
		products.add(product);
	}
	
	boolean removeProduct(Chemical product) {
		return products.remove(product);
	}
	
	List<Chemical> getSpectators() {
		return spectators;
	}
	void addSpectator(Chemical spectator) {
		spectators.add(spectator);
	}
	
	boolean removeSpectator(Chemical spectator) {
		return spectators.remove(spectator);
	}
	
	void importReaction(Reaction reactionToImport) {
		reactants.addAll(reactionToImport.getReactants());
		products.addAll(reactionToImport.getProducts());
		spectators.addAll(reactionToImport.getSpectators());
	}

	public Element toCML() {
		Element reaction = new Element("reaction");
		Element productList = new Element("productList");
		reaction.appendChild(productList);
		int i=0;
		for (Chemical product : products) {
			Element productCml =product.toCML("m" +i++);
			productCml.setLocalName("product");
			productList.appendChild(productCml);
		}

		Element reactantlist = new Element("reactantlist");
		reaction.appendChild(reactantlist);
		for (Chemical reactant : reactants) {
			reactantlist.appendChild(reactant.toCML("m" +i++));
		}
		
		Element spectatorList = new Element("spectatorList");
		reaction.appendChild(spectatorList);
		for (Chemical spectator : spectators) {
			Element solventCml =spectator.toCML("m" +i++);
			solventCml.setLocalName("spectator");
			spectatorList.appendChild(solventCml);
		}
		return reaction;
	}
}

package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class Reaction {

	List<Chemical> reactants =new ArrayList<Chemical>();
	List<Chemical> products =new ArrayList<Chemical>();
	List<Chemical> solvents =new ArrayList<Chemical>();
	Chemical primaryProduct;

	List<Chemical> getReactants() {
		return reactants;
	}
	void addReactant(Chemical reactant) {
		reactants.add(reactant);
	}
	List<Chemical> getProducts() {
		return products;
	}
	void addProduct(Chemical product) {
		products.add(product);
	}
	List<Chemical> getSolvents() {
		return solvents;
	}
	void addSpectator(Chemical solvent) {
		solvents.add(solvent);
	}
	Chemical getPrimaryProduct() {
		return primaryProduct;
	}
	void setPrimaryProduct(Chemical primaryProduct) {
		this.primaryProduct = primaryProduct;
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
		for (Chemical solvent : solvents) {
			Element solventCml =solvent.toCML("m" +i++);
			solventCml.setLocalName("spectator");
			spectatorList.appendChild(solventCml);
		}
		return reaction;
	}
}

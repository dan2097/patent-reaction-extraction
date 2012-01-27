package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class Reaction {
	private static String CML_NAMESPACE = "http://www.xml-cml.org/schema";
	private static String DL_NAMESPACE = "http://bitbucket.org/dan2097";

	private List<Chemical> reactants =new ArrayList<Chemical>();
	private List<Chemical> products =new ArrayList<Chemical>();
	/** Typically solvents or catalysts*/
	private List<Chemical> spectators =new ArrayList<Chemical>();
	private Paragraph inputPara = null;
	private String reactionSmiles = null;

	public void setInput(Paragraph inputPara) {
		this.inputPara =inputPara;	
	}
	public Paragraph getInput() {
		return inputPara;
	}
	
	public String getReactionSmiles() {
		return reactionSmiles;
	}

	public void setReactionSmiles(String reactionSmiles) {
		this.reactionSmiles = reactionSmiles;
	}
	
	public List<Chemical> getReactants() {
		return reactants;
	}
	void addReactant(Chemical reactant) {
		reactants.add(reactant);
	}
	
	boolean removeReactant(Chemical reactant) {
		return reactants.remove(reactant);
	}
	
	public List<Chemical> getProducts() {
		return products;
	}
	void addProduct(Chemical product) {
		products.add(product);
	}
	
	boolean removeProduct(Chemical product) {
		return products.remove(product);
	}
	
	public List<Chemical> getSpectators() {
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
		Element reaction = new Element("reaction", CML_NAMESPACE);
		reaction.addNamespaceDeclaration("cmlDict", "http://www.xml-cml.org/dictionary/cml/");
		reaction.addNamespaceDeclaration("nameDict", "http://www.xml-cml.org/dictionary/cml/name/");
		reaction.addNamespaceDeclaration("unit", "http://www.xml-cml.org/unit/");
		reaction.addNamespaceDeclaration("cml", "http://www.xml-cml.org/schema");
		reaction.addNamespaceDeclaration("dl", DL_NAMESPACE);
		if (reactionSmiles != null){
			Element reactionSmilesEl = new Element("reactionSmiles", DL_NAMESPACE);
			reactionSmilesEl.setNamespacePrefix("dl");
			reactionSmilesEl.appendChild(reactionSmiles);
			reaction.appendChild(reactionSmilesEl);
		}
		Element productList = new Element("productList", CML_NAMESPACE);
		reaction.appendChild(productList);
		int i=0;
		for (Chemical product : products) {
			Element productCml =product.toCML("m" +i++);
			productCml.setLocalName("product");
			productList.appendChild(productCml);
		}

		Element reactantList = new Element("reactantList", CML_NAMESPACE);
		reaction.appendChild(reactantList);
		for (Chemical reactant : reactants) {
			reactantList.appendChild(reactant.toCML("m" +i++));
		}
		
		Element spectatorList = new Element("spectatorList", CML_NAMESPACE);
		reaction.appendChild(spectatorList);
		for (Chemical spectator : spectators) {
			Element solventCml =spectator.toCML("m" +i++);
			solventCml.setLocalName("spectator");
			spectatorList.appendChild(solventCml);
		}
		return reaction;
	}
}

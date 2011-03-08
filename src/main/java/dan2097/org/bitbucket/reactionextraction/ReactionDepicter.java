package dan2097.org.bitbucket.reactionextraction;

import java.io.File;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;
import com.ggasoftware.indigo.IndigoRenderer;

public class ReactionDepicter {
	
	final static Indigo indigo;
	final static IndigoRenderer renderer;

	static{
		indigo = new Indigo();
		renderer = new IndigoRenderer(indigo);
	}
	
	public static void depictReaction(Reaction reaction){
		IndigoObject rxn = indigo.createReaction();
		System.out.println("#####################");
		for (Chemical product: reaction.getProducts()) {
			if (product.getSmiles()!=null){
				rxn.addProduct(indigo.loadMolecule(product.getSmiles()));
				System.out.println("prod " + product.getSmiles());
			}
		}
		for (Chemical reactant: reaction.getReactants()) {
			if (reactant.getSmiles()!=null){
				rxn.addReactant(indigo.loadMolecule(reactant.getSmiles()));
				System.out.println("react " + reactant.getSmiles());
			}
		}
		if (rxn.countProducts()==0 && rxn.countReactants()==0){
			return;
		}
		if (rxn.countProducts()>0 && rxn.countReactants()>0){
			try{
				rxn.automap("discard");
			}
			catch (Exception e) {
				return;
			}
		}
		
		indigo.setOption("render-output-format", "png");
		rxn.layout();
		try{
		File f = File.createTempFile("reaction", ".png", new File("C:/My Documents/workspace/PatentReactionExtractor/"));
		renderer.renderToFile(rxn, f.getCanonicalPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}

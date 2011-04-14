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
	
	public static void depictReaction(Reaction reaction, File depictionFile){
		IndigoObject rxn = indigo.createReaction();
		//System.out.println("#####################");
		for (Chemical product: reaction.getProducts()) {
			if (product.getSmiles()!=null){
				rxn.addProduct(indigo.loadMolecule(product.getSmiles()));
				//System.out.println("prod " + product.getSmiles());
			}
		}
		for (Chemical reactant: reaction.getReactants()) {
			if (reactant.getSmiles()!=null){
				rxn.addReactant(indigo.loadMolecule(reactant.getSmiles()));
				//System.out.println("react " + reactant.getSmiles());
			}
		}
		
		for (Chemical reactant: reaction.getSpectators()) {
			if (reactant.getSmiles()!=null){
				rxn.addCatalyst(indigo.loadMolecule(reactant.getSmiles()));
				//System.out.println("spectator " + reactant.getSmiles());
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
			renderer.renderToFile(rxn, depictionFile.getCanonicalPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}

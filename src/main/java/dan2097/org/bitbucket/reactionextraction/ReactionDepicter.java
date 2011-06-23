package dan2097.org.bitbucket.reactionextraction;

import java.io.File;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;
import com.ggasoftware.indigo.IndigoRenderer;

import dan2097.org.bitbucket.utility.Utils;

public class ReactionDepicter {

	final static IndigoRenderer renderer;

	static{
		Indigo indigo = Utils.indigo;
		renderer = new IndigoRenderer(indigo);
		indigo.setOption("render-output-format", "png");
	}
	
	public static void depictReaction(IndigoObject rxn, File depictionFile){
		rxn.layout();
		try{
			renderer.renderToFile(rxn, depictionFile.getCanonicalPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

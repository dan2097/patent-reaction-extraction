package dan2097.org.bitbucket.reactionextraction;

import java.io.File;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;
import com.ggasoftware.indigo.IndigoRenderer;

import dan2097.org.bitbucket.utility.IndigoHolder;

public class ReactionDepicter {

	final static IndigoRenderer renderer;

	static{
		Indigo indigo = IndigoHolder.getInstance();
		renderer = new IndigoRenderer(indigo);
		indigo.setOption("render-output-format", "png");
	}
	
	public static void depictReaction(IndigoObject rxn, File depictionFile){
		try{
			rxn.layout();
			renderer.renderToFile(rxn, depictionFile.getCanonicalPath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

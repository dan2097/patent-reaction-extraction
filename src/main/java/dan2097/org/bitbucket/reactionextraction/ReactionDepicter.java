package dan2097.org.bitbucket.reactionextraction;

import java.io.File;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;

import dan2097.org.bitbucket.utility.IndigoHolder;

public class ReactionDepicter {

	private static final IndigoRenderer renderer;

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

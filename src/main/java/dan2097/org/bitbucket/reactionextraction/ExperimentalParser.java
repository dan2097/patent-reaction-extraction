package dan2097.org.bitbucket.reactionextraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Element;

import org.apache.commons.io.FileUtils;

public class ExperimentalParser {
	private final Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();
	private final List<Reaction> documentReactions = new ArrayList<Reaction>();

	List<Reaction> getDocumentReactions() {
		return documentReactions;
	}

	public void parseExperimentalSection(Element headingElementToProcess) {
		ExperimentalSectionParser sectionparser = new ExperimentalSectionParser(headingElementToProcess, aliasToChemicalMap);
		documentReactions.addAll(sectionparser.getReactions());
	}

	public void serialize(File directory) throws IOException {
		if (!directory.exists()){
			FileUtils.forceMkdir(directory);
		}
		if (!directory.isDirectory()){
			throw new IllegalArgumentException("A directory was expected");
		}
		for (Reaction reaction : documentReactions) {
			if (reaction.getProducts().size()>0 || reaction.getReactants().size()>0){
				try {
					File f = File.createTempFile("reaction", ".png", directory);
					ReactionDepicter.depictReaction(reaction, f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}

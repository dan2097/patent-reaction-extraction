package dan2097.org.bitbucket.reactionextraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ExperimentalParser {
	private final Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();
	private final List<Reaction> documentReactions = new ArrayList<Reaction>();

	List<Reaction> getDocumentReactions() {
		return documentReactions;
	}

	public void parseExperimentalSection(Element headingElementToProcess) {
		ExperimentalSectionParser sectionparser = new ExperimentalSectionParser(headingElementToProcess, aliasToChemicalMap);
		sectionparser.parseForReactions();
		documentReactions.addAll(sectionparser.getReactions());
	}

	public void serialize(File directory) throws IOException {
		if (!directory.exists()){
			FileUtils.forceMkdir(directory);
		}
		if (!directory.isDirectory()){
			throw new IllegalArgumentException("A directory was expected");
		}
		for (int i = 0; i < documentReactions.size(); i++) {
			Reaction reaction = documentReactions.get(i);
			if (reaction.getProducts().size()>0 || reaction.getReactants().size()>0){
				try {
					File f = new File(directory, "reaction" + i + ".png");
					ReactionDepicter.depictReaction(reaction, f);
						FileOutputStream in = new FileOutputStream(new File(directory, "reactionsrc" + i + ".xml"));
					    Serializer serializer = new Serializer(in);
						serializer.setIndent(2);
						List<Paragraph> paras = reaction.getInput();
						for (Paragraph paragraph : paras) {
							serializer.write(paragraph.getTaggedSentencesDocument());
						}
						IOUtils.closeQuietly(in);
						
					FileOutputStream out = new FileOutputStream(new File(directory, "reaction" + i + ".cml"));
				    serializer = new Serializer(out);
					serializer.setIndent(2);
					serializer.write(new Document(reaction.toCML()));
					IOUtils.closeQuietly(out);	
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}

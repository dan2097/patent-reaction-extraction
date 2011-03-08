package dan2097.org.bitbucket.reactionextraction;

import java.util.HashMap;
import java.util.Map;

import nu.xom.Element;

public class ExperimentalParser {
	private final Map<String, Chemical> aliasToChemicalMap = new HashMap<String, Chemical>();

	public void parseExperimentalSection(Element headingElementToProcess) {
		new ExperimentalSectionParser(headingElementToProcess, aliasToChemicalMap);
		
	}

}

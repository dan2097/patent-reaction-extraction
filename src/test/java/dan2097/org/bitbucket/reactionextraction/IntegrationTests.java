package dan2097.org.bitbucket.reactionextraction;

import java.util.List;

import static junit.framework.Assert.*;

import nu.xom.Document;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import dan2097.org.bitbucket.utility.Utils;

public class IntegrationTests{
	
	@Test
	public void integrationTest1() throws Exception{
		Logger.getLogger("dan2097.org.bitbucket.reactionextraction").setLevel(Level.TRACE);
		Document doc = Utils.buildXmlFile(IntegrationTests.class.getResourceAsStream("patentText1.xml"));
		ExperimentalParser parser = Utils.extractReactions(doc);
		List<Reaction> reactions = parser.getDocumentReactions();
		assertEquals(1, reactions.size());
	}
	
	@Test
	public void integrationTest2() throws Exception{
		Logger.getLogger("dan2097.org.bitbucket.reactionextraction").setLevel(Level.TRACE);
		Document doc = Utils.buildXmlFile(IntegrationTests.class.getResourceAsStream("patentText2.xml"));
		ExperimentalParser parser = Utils.extractReactions(doc);
		List<Reaction> reactions = parser.getDocumentReactions();
		assertEquals(1, reactions.size());
	}
}

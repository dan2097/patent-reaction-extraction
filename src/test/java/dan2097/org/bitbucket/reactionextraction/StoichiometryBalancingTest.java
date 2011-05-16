package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import nu.xom.Element;

import org.junit.Test;

import static org.mockito.Mockito.mock;


public class StoichiometryBalancingTest {

	@Test
	public void stoichiometryUnavailableTest(){
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		reaction.addReactant(chem2);
		parser.getReactions().add(reaction);
		parser.processReactionStoichiometry();
		assertNull(chem1.getStoichiometry());
		assertNull(chem2.getStoichiometry());
	}
	
	@Test
	public void stoichiometry1to1Test(){
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("4");
		chem1.setAmountUnits("mols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("4");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		parser.getReactions().add(reaction);
		parser.processReactionStoichiometry();
		assertEquals(1, chem1.getStoichiometry(), 0.1d);
		assertEquals(1, chem2.getStoichiometry(), 0.1d);
	}
	
	@Test
	public void stoichiometry2to1Test(){
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("20");
		chem1.setAmountUnits("mols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("10");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		parser.getReactions().add(reaction);
		parser.processReactionStoichiometry();
		assertEquals(2, chem1.getStoichiometry(), 0.1d);
		assertEquals(1, chem2.getStoichiometry(), 0.1d);
	}
	
	@Test
	public void stoichiometry1to1point5Test(){
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("3");
		chem1.setAmountUnits("mols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("4.5");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		parser.getReactions().add(reaction);
		parser.processReactionStoichiometry();
		assertEquals(1, chem1.getStoichiometry(), 0.1d);
		assertEquals(1.5, chem2.getStoichiometry(), 0.1d);
	}
	
	@Test
	public void stoichiometry1to2DifferentUnitsTest(){
		ExperimentalSectionParser parser = new ExperimentalSectionParser(mock(Chemical.class), new ArrayList<Element>(), new HashMap<String, Chemical>());
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("500");
		chem1.setAmountUnits("mmols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("1");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		parser.getReactions().add(reaction);
		parser.processReactionStoichiometry();
		assertEquals(1, chem1.getStoichiometry(), 0.1d);
		assertEquals(2, chem2.getStoichiometry(), 0.1d);
	}
}

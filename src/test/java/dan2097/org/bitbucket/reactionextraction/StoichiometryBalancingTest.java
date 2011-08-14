package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;


public class StoichiometryBalancingTest {

	@Test
	public void stoichiometryUnavailableTest(){
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		reaction.addReactant(chem2);
		new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
		assertNull(chem1.getStoichiometry());
		assertNull(chem2.getStoichiometry());
	}
	
	@Test
	public void stoichiometry1to1Test(){
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("4");
		chem1.setAmountUnits("mols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("4");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
		assertEquals(1, chem1.getStoichiometry(), 0.1d);
		assertEquals(1, chem2.getStoichiometry(), 0.1d);
	}
	
	@Test
	public void stoichiometry2to1Test(){
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("20");
		chem1.setAmountUnits("mols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("10");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
		assertEquals(2, chem1.getStoichiometry(), 0.1d);
		assertEquals(1, chem2.getStoichiometry(), 0.1d);
	}
	
	@Test
	public void stoichiometry1to1point5Test(){
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("3");
		chem1.setAmountUnits("mols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("4.5");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
		assertEquals(1, chem1.getStoichiometry(), 0.1d);
		assertEquals(1.5, chem2.getStoichiometry(), 0.1d);
	}
	
	@Test
	public void stoichiometry1to2DifferentUnitsTest(){
		Reaction reaction = new Reaction();
		Chemical chem1 = new Chemical("foo");
		chem1.setAmountValue("500");
		chem1.setAmountUnits("mmols");
		reaction.addReactant(chem1);
		Chemical chem2= new Chemical("foo");
		chem2.setAmountValue("1");
		chem2.setAmountUnits("mols");
		reaction.addReactant(chem2);
		new ReactionStoichiometryDeterminer(reaction).processReactionStoichiometry();
		assertEquals(1, chem1.getStoichiometry(), 0.1d);
		assertEquals(2, chem2.getStoichiometry(), 0.1d);
	}
}

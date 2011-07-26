package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;

public class ChemicalSenseApplicationTest {

	@Test
	public void mergeProductsByInchiTest1(){
		Reaction reaction = new Reaction();
		Chemical product1 = new Chemical("foo");
		product1.setInchi("InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+");
		product1.setAmountValue("5");
		product1.setAmountUnits("mols");
		Chemical product2 = new Chemical("foo");
		product2.setInchi("InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+");
		product2.setAmountValue("5");
		product2.setAmountUnits("mols");
		reaction.addProduct(product1);
		reaction.addProduct(product2);
		new ChemicalSenseApplication(reaction).mergeProductsByInChI();
		assertEquals(1, reaction.getProducts().size());
		assertEquals(product2, reaction.getProducts().get(0));
	}

	@Test
	public void mergeProductsByInchiTest2(){
		//last product is preferred unless a previous one has quantities and it does not
		Reaction reaction = new Reaction();
		Chemical product1 = new Chemical("foo");
		product1.setInchi("InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+");
		product1.setAmountValue("5");
		product1.setAmountUnits("mols");
		Chemical product2 = new Chemical("foo");
		product2.setInchi("InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+");
		reaction.addProduct(product1);
		reaction.addProduct(product2);
		new ChemicalSenseApplication(reaction).mergeProductsByInChI();
		assertEquals(1, reaction.getProducts().size());
		assertEquals(product1, reaction.getProducts().get(0));
	}

	@Test
	public void transitionMetalCatalystTest() {
		Reaction reaction = new Reaction();
		Chemical catalyst = new Chemical("platinum");
		catalyst.setSmiles("[Pt]");
		reaction.addReactant(catalyst);
		catalyst.setRole(ChemicalRole.reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.catalyst, catalyst.getRole());
	}
	
	@Test
	public void inorganicCompoundTest() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("sodium hydroxide");
		reactant.setSmiles("[Na+].[OH-]");
		reaction.addReactant(reactant);
		reactant.setRole(ChemicalRole.reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void transitionMetalCompoundFormationTest() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("platinum");
		reactant.setSmiles("[Pt]");
		reactant.setRole(ChemicalRole.reactant);
		Chemical product = new Chemical("Cisplatin");
		product.setSmiles("[NH3][Pt](Cl)(Cl)[NH3]");
		product.setRole(ChemicalRole.product);
		reaction.addReactant(reactant);
		reaction.addProduct(product);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void reactantIsKnownToBeASolventInSameReactionTest() {
		Reaction reaction = new Reaction();
		Chemical misclassifiedReactant = new Chemical("THF");
		misclassifiedReactant.setInchi("InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2");
		misclassifiedReactant.setRole(ChemicalRole.reactant);
		Chemical solvent = new Chemical("THF");
		solvent.setInchi("InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2");
		solvent.setRole(ChemicalRole.solvent);
		reaction.addReactant(misclassifiedReactant);
		reaction.addSpectator(solvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, misclassifiedReactant.getRole());
	}
	
	@Test
	public void correctlyClassifiedTest() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("DMF");
		reactant.setInchi("InChI=1S/C6H8O4/c1-9-5(7)3-4-6(8)10-2/h3-4H,1-2H3/b4-3+");
		reactant.setRole(ChemicalRole.reactant);
		Chemical solvent = new Chemical("DMF");
		solvent.setInchi("InChI=1S/C3H7NO/c1-4(2)3-5/h3H,1-2H3");
		solvent.setRole(ChemicalRole.solvent);
		reaction.addReactant(reactant);
		reaction.addSpectator(solvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("THF");
		reactant.setInchi("InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge2() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("THF");
		reactant.setInchi("InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2");
		reactant.setAmountValue("0.5");
		reactant.setAmountUnits("mM");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge3() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("acetic acid");//can be a solvent
		reactant.setInchi("InChI=1/C2H4O2/c1-2(3)4/h1H3,(H,3,4)");
		reactant.setAmountValue("0.5");
		reactant.setAmountUnits("mM");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		Chemical misclassifiedSolvent = new Chemical("THF");
		misclassifiedSolvent.setInchi("InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2");
		misclassifiedSolvent.setRole(ChemicalRole.reactant);
		reaction.addReactant(misclassifiedSolvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
		assertEquals(ChemicalRole.solvent, misclassifiedSolvent.getRole());
	}

	@Test
	public void assignedAsSolventDueToInpreciseVolumeTest(){
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("foo");
		reactant.setVolumeValue("500");
		reactant.setVolumeUnits("ml");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge4() {
		Reaction reaction = new Reaction();
		Chemical misclassifiedSolvent1 = new Chemical("foo");//can be a solvent
		misclassifiedSolvent1.setVolumeValue("500");
		misclassifiedSolvent1.setVolumeUnits("ml");
		misclassifiedSolvent1.setRole(ChemicalRole.reactant);
		reaction.addReactant(misclassifiedSolvent1);
		Chemical misclassifiedSolvent2 = new Chemical("foo");
		misclassifiedSolvent2.setRole(ChemicalRole.reactant);
		reaction.addReactant(misclassifiedSolvent2);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, misclassifiedSolvent1.getRole());
		assertEquals(ChemicalRole.solvent, misclassifiedSolvent2.getRole());
	}

	@Test
	public void onlyEmployHeuristicWhenNoSolventPresentTest(){
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("foo");
		reactant.setVolumeValue("500");
		reactant.setVolumeUnits("ml");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		Chemical solvent = new Chemical("THF");
		solvent.setInchi("InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2");
		solvent.setRole(ChemicalRole.solvent);
		reaction.addReactant(solvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
}

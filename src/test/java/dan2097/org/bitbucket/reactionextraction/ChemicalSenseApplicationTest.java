package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;

public class ChemicalSenseApplicationTest {

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
	public void reactantMisclassifiedAsASolventTest() {
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
	public void correctlyClassifiedAlreadyTest() {
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
}

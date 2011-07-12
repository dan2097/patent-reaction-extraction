package dan2097.org.bitbucket.reactionextraction;

import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import static junit.framework.Assert.assertEquals;

import nu.xom.Attribute;
import nu.xom.Element;

import org.junit.Test;

import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;

public class ChemicalRoleAssignmentTest {

	@Test
	public void assignedAsSolventFromChemicalTaggerTest(){
		Element chemicalEl = new Element(MOLECULE_Container);
		chemicalEl.addAttribute(new Attribute(ChemicalTaggerAtrs.ROLE_ATR, ChemicalTaggerAtrs.SOLVENT_ROLE_VAL));
		Chemical chemical = new Chemical("foo");
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.solvent, chemical.getRole());
	}
	
	@Test
	public void assignedAsCatalystFromChemicalTaggerTest(){
		Element chemicalEl = new Element(MOLECULE_Container);
		chemicalEl.addAttribute(new Attribute(ChemicalTaggerAtrs.ROLE_ATR, ChemicalTaggerAtrs.CATALYST_ROLE_VAL));
		Chemical chemical = new Chemical("foo");
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.catalyst, chemical.getRole());
	}
	
	@Test
	public void insufficientInformationToClassAsSolventOrCatalystTest(){
		Element chemicalEl = new Element(MOLECULE_Container);
		Chemical chemical = new Chemical("foo");
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.reactant, chemical.getRole());
	}
}

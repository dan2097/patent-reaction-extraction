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
	public void assignedAsCatalystFromName1(){
		Element chemicalEl = new Element(MOLECULE_Container);
		Chemical chemical = new Chemical("Crabtree catalyst");//uses the word catalyst
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.catalyst, chemical.getRole());
	}
	
	@Test
	public void assignedAsCatalystFromName2(){
		Element chemicalEl = new Element(MOLECULE_Container);
		Chemical chemical = new Chemical("Raney Nickel");//lookup
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.catalyst, chemical.getRole());
	}
	
	@Test
	public void assignedAsCatalystFromName3(){
		Element chemicalEl = new Element(MOLECULE_Container);
		Chemical chemical = new Chemical("Pd / C");//lookup
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.catalyst, chemical.getRole());
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
	
	@Test
	public void assignedAsSolventInSolventTest(){
		Element parent = new Element(PREPPHRASE_Container);
		Element in = new Element(IN_IN);
		parent.appendChild(in);
		Element chemicalEl = new Element(MOLECULE_Container);
		parent.appendChild(chemicalEl);
		Chemical chemical = new Chemical("foo");
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.solvent, chemical.getRole());
	}
	
	@Test
	public void assignedAsSolventInChemAndSolventTest(){
		Element sentence = new Element(SENTENCE_Container);
		
		Element prepphrase = new Element(PREPPHRASE_Container);
		Element in = new Element(IN_IN);
		prepphrase.appendChild(in);
		Element chemicalEl1 = new Element(MOLECULE_Container);
		Element oscarCM = new Element(OSCARCM_Container);
		Element oscarDashCM = new Element(OSCAR_CM);
		oscarDashCM.appendChild("foo");
		oscarCM.appendChild(oscarDashCM);
		chemicalEl1.appendChild(oscarCM);
		prepphrase.appendChild(chemicalEl1);
		
		Element and = new Element(CC);
		and.appendChild("and");
		
		Element nounphrase = new Element(NOUN_PHRASE_Container);
		Element chemicalEl2 = new Element(MOLECULE_Container);
		nounphrase.appendChild(chemicalEl2);
		Chemical chemical2 = new Chemical("bar");
		
		sentence.appendChild(prepphrase);
		sentence.appendChild(and);
		sentence.appendChild(nounphrase);
		sentence.appendChild(new Element(STOP));
		
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl2, chemical2);
		assertEquals(ChemicalRole.solvent, chemical2.getRole());
	}
	
	@Test
	public void assignedAsSovlentFromName(){
		Element chemicalEl = new Element(MOLECULE_Container);
		Chemical chemical = new Chemical("brine");
		ChemicalRoleAssigner.assignRoleToChemical(chemicalEl, chemical);
		assertEquals(ChemicalRole.solvent, chemical.getRole());
	}
}

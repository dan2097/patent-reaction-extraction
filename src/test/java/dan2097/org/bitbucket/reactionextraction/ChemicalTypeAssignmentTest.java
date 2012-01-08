package dan2097.org.bitbucket.reactionextraction;

import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import static junit.framework.Assert.assertEquals;
import nu.xom.Element;

import org.junit.Test;

public class ChemicalTypeAssignmentTest {

	@Test
	public void typeDetectionTestFP1(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(UNNAMEDMOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element alphaNum = new Element(CD_ALPHANUM);
		alphaNum.appendChild("4H");
		moleculeEl.appendChild(alphaNum);
		Chemical chem = new Chemical("4H");
		assertEquals(ChemicalEntityType.falsePositive, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestFP2(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("benzene-d6");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("benzene-d6");
		chem.setChemicalIdentifierPair(new ChemicalIdentifierPair("[2H]C1=C([2H])C([2H])=C([2H])C([2H])=C1[2H]", "InChI=1/C6H6/c1-2-4-6-5-3-1/h1-6H/i1D,2D,3D,4D,5D,6D"));
		assertEquals(ChemicalEntityType.falsePositive, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestFP3(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("tms");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("tms");
		assertEquals(ChemicalEntityType.falsePositive, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestFP4(){//an ice-H2O bath
		Element sentence = new Element(SENTENCE_Container);
		Element apparatusContainer = new Element(APPARATUS_Container);
		sentence.appendChild(apparatusContainer);
		Element moleculeEl = new Element(MOLECULE_Container);
		apparatusContainer.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("ice-H2O");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("ice-H2O");
		assertEquals(ChemicalEntityType.falsePositive, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestSpecific(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("2,3-dimethylbutane");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("2,3-dimethylbutane");
		assertEquals(ChemicalEntityType.exact, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestADeterminer(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		Element dt = new Element(DT);
		dt.appendChild("a");
		sentence.appendChild(dt);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("pyridine");
		assertEquals(ChemicalEntityType.chemicalClass, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestADeterminerQualified(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		Element dt = new Element(DT);
		dt.appendChild("a");
		sentence.appendChild(dt);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Element referenceToCompound = new Element(REFERENCETOCOMPOUND_Container);
		referenceToCompound.appendChild(new Element(LRB));
		Element identifier = new Element(NN_IDENTIFIER);
		identifier.appendChild("IV");
		referenceToCompound.appendChild(identifier);
		referenceToCompound.appendChild(new Element(RRB));
		moleculeEl.appendChild(referenceToCompound);
		Chemical chem = new Chemical("pyridine");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestTheDeterminer(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(new Element(DT_THE));
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("pyridine");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestQualifiedCompound1(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Element qualifier = new Element(NN);
		qualifier.appendChild("compound");
		sentence.appendChild(qualifier);
		Chemical chem = new Chemical("pyridine");
		assertEquals(ChemicalEntityType.chemicalClass, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestQualifiedCompound2(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Element qualifier = new Element(NN);
		qualifier.appendChild("ring");
		sentence.appendChild(qualifier);
		Chemical chem = new Chemical("pyridine");
		assertEquals(ChemicalEntityType.fragment, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestPlural1(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("phenols");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("phenols");
		assertEquals(ChemicalEntityType.chemicalClass, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestPlural2(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("foobars");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("foobars");
		assertEquals(ChemicalEntityType.chemicalClass, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}

	@Test
	public void testFragment(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("ethyl");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("ethyl");
		assertEquals(ChemicalEntityType.fragment, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestSpecificPlural(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("phenols");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Element referenceToCompound = new Element(REFERENCETOCOMPOUND_Container);
		referenceToCompound.appendChild(new Element(LRB));
		Element identifier = new Element(CD);
		identifier.appendChild("1-3");
		referenceToCompound.appendChild(identifier);
		referenceToCompound.appendChild(new Element(RRB));
		moleculeEl.appendChild(referenceToCompound);
		Chemical chem = new Chemical("phenols");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestDeterminerWithinMolecule1(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		moleculeEl.appendChild(new Element(QUANTITY_Container));
		moleculeEl.appendChild(new Element(DT_THE));
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("furan");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("furan");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestDeterminerWithinMolecule2(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		moleculeEl.appendChild(new Element(QUANTITY_Container));
		Element dt = new Element(DT);
		dt.appendChild("a");
		moleculeEl.appendChild(dt);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("furan");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("furan");
		assertEquals(ChemicalEntityType.chemicalClass, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestDeterminerWithinMolecule3(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		moleculeEl.appendChild(new Element(QUANTITY_Container));
		moleculeEl.appendChild(new Element(DT_THE));
		Element cmCont = new Element(OSCARCM_Container);
		Element cm1 = new Element(OSCAR_CM);
		cm1.appendChild("sulfamic");
		cmCont.appendChild(cm1);
		Element cm2 = new Element(OSCAR_CM);
		cm2.appendChild("acid");
		cmCont.appendChild(cm2);
		moleculeEl.appendChild(cmCont);
		Chemical chem = new Chemical("sulfamic acid");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestDeterminerWithinMolecule4(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		moleculeEl.appendChild(new Element(QUANTITY_Container));
		Element dt = new Element(DT);
		dt.appendChild("a");
		moleculeEl.appendChild(dt);
		Element cmCont = new Element(OSCARCM_Container);
		Element cm1 = new Element(OSCAR_CM);
		cm1.appendChild("sulfamic");
		cmCont.appendChild(cm1);
		Element cm2 = new Element(OSCAR_CM);
		cm2.appendChild("acid");
		cmCont.appendChild(cm2);
		moleculeEl.appendChild(cmCont);
		Chemical chem = new Chemical("sulfamic acid");
		assertEquals(ChemicalEntityType.chemicalClass, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void typeDetectionTestSMARTSClassifyAsClass(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("Sulfonic acid");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("Sulfonic acid");
		assertEquals(ChemicalEntityType.chemicalClass, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void exactCompoundActuallyReferenceTest(){//e.g. indole 3
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("indole");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		moleculeEl.appendChild(new Element(REFERENCETOCOMPOUND_Container));
		Chemical chem = new Chemical("indole");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void chemicalClassActuallyReferenceTest(){//e.g. sulfone from step 5
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("sulfone");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		moleculeEl.appendChild(new Element(IN_FROM));
		moleculeEl.appendChild(new Element(PROCEDURE_Container));
		Chemical chem = new Chemical("sulfone");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
	
	@Test
	public void chemicalClassActuallyReferenceTest2(){//e.g. the amide
		Element sentence = new Element(SENTENCE_Container);
		sentence.appendChild(new Element(DT_THE));
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("amide");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("amide");
		assertEquals(ChemicalEntityType.definiteReference, ChemicalTypeAssigner.determineEntityTypeOfChemical(moleculeEl, chem));
	}
}

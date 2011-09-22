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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.falsePositive, chem.getType());
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
		chem.setSmiles("[2H]C1=C([2H])C([2H])=C([2H])C([2H])=C1[2H]");
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.falsePositive, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.falsePositive, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.falsePositive, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.exact, chem.getType());
	}
	
	@Test
	public void typeDetectionTestADeterminer(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(new Element(DT));
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("pyridine");
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
	}
	
	@Test
	public void typeDetectionTestADeterminerQualified(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(new Element(DT));
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.definiteReference, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.definiteReference, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.fragment, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.fragment, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.definiteReference, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.definiteReference, chem.getType());
	}
	
	@Test
	public void typeDetectionTestDeterminerWithinMolecule2(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		moleculeEl.appendChild(new Element(QUANTITY_Container));
		moleculeEl.appendChild(new Element(DT));
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("furan");
		Element cmContainer = new Element(OSCARCM_Container);
		cmContainer.appendChild(cm);
		moleculeEl.appendChild(cmContainer);
		Chemical chem = new Chemical("furan");
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.definiteReference, chem.getType());
	}
	
	@Test
	public void typeDetectionTestDeterminerWithinMolecule4(){
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		moleculeEl.appendChild(new Element(QUANTITY_Container));
		moleculeEl.appendChild(new Element(DT));
		Element cmCont = new Element(OSCARCM_Container);
		Element cm1 = new Element(OSCAR_CM);
		cm1.appendChild("sulfamic");
		cmCont.appendChild(cm1);
		Element cm2 = new Element(OSCAR_CM);
		cm2.appendChild("acid");
		cmCont.appendChild(cm2);
		moleculeEl.appendChild(cmCont);
		Chemical chem = new Chemical("sulfamic acid");
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.definiteReference, chem.getType());
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
		ChemicalTypeAssigner.assignTypeToChemical(moleculeEl, chem);
		assertEquals(ChemicalType.definiteReference, chem.getType());
	}
}

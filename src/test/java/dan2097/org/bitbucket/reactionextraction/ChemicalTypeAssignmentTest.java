package dan2097.org.bitbucket.reactionextraction;

import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;
import static junit.framework.Assert.assertEquals;
import nu.xom.Element;

import org.junit.Test;

public class ChemicalTypeAssignmentTest {

	@Test
	public void typeDetectionTestFP(){
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
	public void typeDetectionTestDeterminerCompound1(){
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
	public void typeDetectionTestDeterminerCompound2(){
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
		assertEquals(ChemicalType.exactReference, chem.getType());
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
	public void typeDetectionTestPlural(){
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
		assertEquals(ChemicalType.exactReference, chem.getType());
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
		assertEquals(ChemicalType.exactReference, chem.getType());
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
}

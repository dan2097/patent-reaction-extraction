package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;


import nu.xom.Element;

import org.junit.Test;

public class LocalSemanticInformationExtractionTest {

	@Test
	public void typeDetectionTestFP(){
		Map<Element, Chemical> map =new HashMap<Element, Chemical>();
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(UNNAMEDMOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element alphaNum = new Element(CD_ALPHANUM);
		alphaNum.appendChild("4H");
		moleculeEl.appendChild(alphaNum);
		Chemical chem = new Chemical("4H");
		map.put(moleculeEl, chem);
		ChemicalTypeAssigner.performPreliminaryTypeDetection(map);
		assertEquals(ChemicalType.falsePositive, chem.getType());
	}
	
	@Test
	public void typeDetectionTestSpecific(){
		Map<Element, Chemical> map =new HashMap<Element, Chemical>();
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("2,3-dimethylbutane");
		moleculeEl.appendChild(cm);
		Chemical chem = new Chemical("2,3-dimethylbutane");
		map.put(moleculeEl, chem);
		ChemicalTypeAssigner.performPreliminaryTypeDetection(map);
		assertEquals(ChemicalType.exact, chem.getType());
	}
	
	@Test
	public void typeDetectionTestDeterminerCompound1(){
		Map<Element, Chemical> map =new HashMap<Element, Chemical>();
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(new Element(DT));
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		moleculeEl.appendChild(cm);
		Chemical chem = new Chemical("pyridine");
		map.put(moleculeEl, chem);
		ChemicalTypeAssigner.performPreliminaryTypeDetection(map);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
	}
	
	@Test
	public void typeDetectionTestDeterminerCompound2(){
		Map<Element, Chemical> map =new HashMap<Element, Chemical>();
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(new Element(DT_THE));
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		moleculeEl.appendChild(cm);
		Chemical chem = new Chemical("pyridine");
		map.put(moleculeEl, chem);
		ChemicalTypeAssigner.performPreliminaryTypeDetection(map);
		assertEquals(ChemicalType.exactReference, chem.getType());
	}
	
	@Test
	public void typeDetectionTestQualifiedCompound1(){
		Map<Element, Chemical> map =new HashMap<Element, Chemical>();
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		moleculeEl.appendChild(cm);
		Element qualifier = new Element(NN);
		qualifier.appendChild("compound");
		sentence.appendChild(qualifier);
		Chemical chem = new Chemical("pyridine");
		map.put(moleculeEl, chem);
		ChemicalTypeAssigner.performPreliminaryTypeDetection(map);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
	}
	
	@Test
	public void typeDetectionTestQualifiedCompound2(){
		Map<Element, Chemical> map =new HashMap<Element, Chemical>();
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("pyridine");
		moleculeEl.appendChild(cm);
		Element qualifier = new Element(NN);
		qualifier.appendChild("ring");
		sentence.appendChild(qualifier);
		Chemical chem = new Chemical("pyridine");
		map.put(moleculeEl, chem);
		ChemicalTypeAssigner.performPreliminaryTypeDetection(map);
		assertEquals(ChemicalType.fragment, chem.getType());
	}
	
	@Test
	public void typeDetectionTestPlural(){
		Map<Element, Chemical> map =new HashMap<Element, Chemical>();
		Element sentence = new Element(SENTENCE_Container);
		Element moleculeEl = new Element(MOLECULE_Container);
		sentence.appendChild(moleculeEl);
		Element cm = new Element(OSCAR_CM);
		cm.appendChild("phenols");
		moleculeEl.appendChild(cm);
		Chemical chem = new Chemical("phenols");
		map.put(moleculeEl, chem);
		ChemicalTypeAssigner.performPreliminaryTypeDetection(map);
		assertEquals(ChemicalType.chemicalClass, chem.getType());
	}
}

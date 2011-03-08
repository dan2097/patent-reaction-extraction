package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLAtrs;
import dan2097.org.bitbucket.utility.XMLTags;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

public class ExperimentalParser {

	private final Map<Element, Chemical> moleculeToChemicalMap = new HashMap<Element, Chemical>();
	private final Chemical titleCompound;
	
	public ExperimentalParser(Element headingEl) {
		titleCompound = extractChemicalFromHeading(headingEl.getAttributeValue(XMLAtrs.TITLE));
		if (titleCompound!=null){
			List<Paragraph> paragraphs = new ArrayList<Paragraph>();
			for (Element p :  XOMTools.getChildElementsWithTagName(headingEl, XMLTags.P)) {
				Paragraph para = new Paragraph(p);
				if (!para.getTaggedString().equals("")){
					paragraphs.add(para);
					generateMoleculeToChemicalMap(para);
				}
			}
			List<Reaction> reactions = determineReactions(paragraphs);
			for (Reaction reaction : reactions) {
				//System.out.println(reaction.toCML().toXML());
			}
		}
	}

	private void generateMoleculeToChemicalMap(Paragraph para) {
		List<Element> moleculeEls = findAllMolecules(para);
		for (Element moleculeEl : moleculeEls) {
			Chemical chem = generateChemicalsFromMoleculeElsAndLocalInformation(moleculeEl);
			moleculeToChemicalMap.put(moleculeEl, chem);
		}
	}

	private Chemical generateChemicalsFromMoleculeElsAndLocalInformation(Element moleculeEl) {
		Chemical chem = new Chemical(findMoleculeName(moleculeEl));
		ChemicalPropertyDetermination.determineProperties(chem, moleculeEl);
		return chem;
	}
	
	/**
	 * Finds the chemical name of the chemical described by a chemical tagger MOLECULE tag
	 * @param molecule
	 * @return
	 */
	private String findMoleculeName(Element molecule) {
		if (!molecule.getLocalName().equals(ChemicalTaggerTags.MOLECULE_Container)) {
			throw new IllegalArgumentException("argument was not a molecule");
		}
		
		Element singleWordName = molecule.getFirstChildElement(ChemicalTaggerTags.OSCAR_CM);
		if (singleWordName != null) {
			return singleWordName.getValue();
		}
		
		Element multiWordName = molecule.getFirstChildElement(ChemicalTaggerTags.OSCARCM_Container);
		if (multiWordName != null) {
			Elements multiWordNameWords = multiWordName.getChildElements(ChemicalTaggerTags.OSCAR_CM);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < multiWordNameWords.size(); i++) {
				builder.append(multiWordNameWords.get(i).getValue());
				if (i < multiWordNameWords.size()-1) {
					builder.append(" ");
				}
			}
			return builder.toString();	
		}
		throw new RuntimeException("malformed molecule");
	}


	/**
	 * Creates a list of all MOLECULE elements
	 * @return
	 */
	private List<Element> findAllMolecules(Paragraph paragraph) {
		List <Element> mols = new ArrayList<Element>();
		Document chemicalTaggerResult =paragraph.getTaggedSentencesDocument();
		Nodes molecules = chemicalTaggerResult.query("//" + ChemicalTaggerTags.MOLECULE_Container);
		for (int i = 0; i < molecules.size(); i++) {
			Element molecule = (Element) molecules.get(i);
			mols.add(molecule);
		}
		return mols;
	}


	private Chemical extractChemicalFromHeading(String title) {
		if (title==null){
			throw new IllegalArgumentException("Input title text was null");
		}
		List<String> name = Utils.getSystematicChemicalNamesFromText(title);
		if (name.size()==1){
			return new Chemical(name.get(0));
		}
		return null;
	}


	private List<Reaction> determineReactions(List<Paragraph> paragraphs) {
		List<Reaction> reactions = new ArrayList<Reaction>();
		String titleCompoundInChI =titleCompound.getInchi();
		for (Paragraph paragraph : paragraphs) {
//			System.out.println(paragraph.getUnTaggedString());
//			System.out.println(paragraph.getTaggedSentencesDocument().toXML());
			Element taggedDocRoot = paragraph.getTaggedSentencesDocument().getRootElement();
//			System.out.println(paragraph.getUnTaggedString());
//			System.out.println(taggedDocRoot.toXML());
			List<Element> products = new ArrayList<Element>();
			for (String xpath : Xpaths.yieldXPaths) {
				Nodes synthesizedMolecules = taggedDocRoot.query(xpath);
				for (int i = 0; i < synthesizedMolecules.size(); i++) {
					products.add((Element) synthesizedMolecules.get(i));
//					System.out.println(synthesizedMolecules.get(i).toXML());
//					Chemical cm = moleculeToChemicalMap.get(synthesizedMolecules.get(i));
//					if (cm != null && cm.getInchi()!=null && titleCompoundInChI.equals(cm.getInchi())){
//						System.out.println("TITLE COMPOUND");
//					}
				}
			}
			List<Element> sentences = XOMTools.getChildElementsWithTagName(taggedDocRoot, ChemicalTaggerTags.SENTENCE_Container);
			for (Element sentence : sentences) {
				List<Element> molecules = XOMTools.getDescendantElementsWithTagName(sentence, ChemicalTaggerTags.MOLECULE_Container);
				List<Element> stepProducts = new ArrayList<Element>();
				for (Element product : products) {
					if (molecules.contains(product)){
						molecules.remove(product);
						stepProducts.add(product);
					}
				}
				if (molecules.size()>0){
					Reaction reaction = new Reaction();
					if (stepProducts.size()>0){
						Element realMolecule = null;
						for (Element product : stepProducts) {
							if (product.getLocalName().equals(ChemicalTaggerTags.MOLECULE_Container)){
								realMolecule =product;
							}
						}
						if (realMolecule!=null){
							reaction.addProduct(moleculeToChemicalMap.get(realMolecule));
						}
					}
					for (Element molecule : molecules) {
						if (!ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(molecule.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
							reaction.addReactant(moleculeToChemicalMap.get(molecule));
						}
						else{
							reaction.addSpectator(moleculeToChemicalMap.get(molecule));
						}
					}
					ReactionDepicter.depictReaction(reaction);
					reactions.add(reaction);
				}
			}
		}
		return reactions;
	}
}

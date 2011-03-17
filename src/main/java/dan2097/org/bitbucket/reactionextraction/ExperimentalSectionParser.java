package dan2097.org.bitbucket.reactionextraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

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

public class ExperimentalSectionParser {

	private final Map<Element, Chemical> moleculeToChemicalMap = new HashMap<Element, Chemical>();
	private final Map<String, Chemical> aliasToChemicalMap;
	private final static Pattern matchIdentifier = Pattern.compile("((\\d+[a-z]?)|[\\(\\{\\[](\\d+[a-z]?|.*\\d+)[\\)\\}\\]])\\s*$");
	private final Chemical titleCompound;
	static int i=0;
	
	public ExperimentalSectionParser(Element headingEl, Map<String, Chemical> aliasToChemicalMap) {
		String headingText = headingEl.getAttributeValue(XMLAtrs.TITLE);
		titleCompound = extractChemicalFromHeading(headingText);
		this.aliasToChemicalMap = aliasToChemicalMap;
		if (titleCompound!=null){
			attemptToExtractAliasFromTitleText(headingText);
			List<Paragraph> paragraphs = new ArrayList<Paragraph>();
			for (Element p :  XOMTools.getChildElementsWithTagName(headingEl, XMLTags.P)) {
				Paragraph para = new Paragraph(p);
				if (!para.getTaggedString().equals("")){
					paragraphs.add(para);
					generateMoleculeToChemicalMap(para);
				}
			}
			determineReactions(paragraphs);
		}
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

	private void attemptToExtractAliasFromTitleText(String headingText) {
		Matcher m = matchIdentifier.matcher(headingText);
		if (m.find()){
			if (m.group(1)!=null){
				aliasToChemicalMap.put(m.group(1), titleCompound);
			}
			else if (m.group(2)!=null){
				aliasToChemicalMap.put(m.group(2), titleCompound);
			}
			else{
				throw new RuntimeException("identifier regex is malformed");
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


	private List<Reaction> determineReactions(List<Paragraph> paragraphs) {
		List<Reaction> reactions = new ArrayList<Reaction>();
		String titleCompoundInChI =titleCompound.getInchi();
		for (Paragraph paragraph : paragraphs) {
			Element taggedDocRoot = paragraph.getTaggedSentencesDocument().getRootElement();
			List<Element> sentences = XOMTools.getChildElementsWithTagName(taggedDocRoot, ChemicalTaggerTags.SENTENCE_Container);
			for (Element sentence : sentences) {
				Set<Element> products = new HashSet<Element>();
				for (String xpath : Xpaths.yieldXPaths) {
					Nodes synthesizedMolecules = sentence.query(xpath);
					for (int i = 0; i < synthesizedMolecules.size(); i++) {
						products.add((Element) synthesizedMolecules.get(i));
					}
				}
				if (products.size() >0){
					Set<Element> reactants = new HashSet<Element>();
					for (Element product : products) {
						for (String xpath : Xpaths.reactantXpaths) {
							Nodes reactantMolecules = product.query(xpath);
							for (int i = 0; i < reactantMolecules.size(); i++) {
								reactants.add((Element) reactantMolecules.get(i));
							}
						}
					}
			
					Reaction reaction = new Reaction();
					for (Element product : products) {
						Chemical chem = moleculeToChemicalMap.get(product);
						if (chem!=null){
							reaction.addProduct(chem);
						}
						else{
							//TODO resolve symbolic chemical names (could however be an unresolvable chemical name)
						}
					}
					for (Element reactant : reactants) {
						Chemical chem = moleculeToChemicalMap.get(reactant);
						if (chem!=null){
							if (!ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(reactant.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
								reaction.addReactant(chem);
							}
							else{
								reaction.addSpectator(chem);
							}
						}
						else{
							//TODO resolve symbolic chemical names (could however be an unresolvable chemical name)
						}
					}
					List<Element> mols = XOMTools.getDescendantElementsWithTagNames(sentence, new String[]{ChemicalTaggerTags.MOLECULE_Container, ChemicalTaggerTags.UNNAMEDMOLECULE_Container});
					mols.removeAll(reactants);
					mols.removeAll(products);
					for (int i = mols.size()-1; i >=0; i--) {
						Element mol = mols.get(i);
						for (String xpath : Xpaths.moleculesToIgnoreXpaths) {
							if (mol.query(xpath).size()>0){
								mols.remove(i);
								break;
							}
						}
					}
					for (String xpath : Xpaths.referencesToPreviousReactions) {
						Nodes moleculesToRemove = sentence.query(xpath);
						for (int i = 0; i < moleculesToRemove.size(); i++) {
							mols.remove(moleculesToRemove.get(i));
						}
					}

					if (!mols.isEmpty()){
						System.out.println("##############");
						System.out.println(sentence.toXML());
						for (Element mol : mols) {
						System.out.println(i++);
							System.out.println(mol.toXML());
						}
					}
					
					if (reaction.getProducts().size()>0 || reaction.getReactants().size()>0){
						try {
							File f = File.createTempFile("reaction", ".png", new File("C:/My Documents/workspace/PatentReactionExtractor/temp"));
							ReactionDepicter.depictReaction(reaction, f);
							File xml = new File("temp/"+f.getName().substring(0, f.getName().length()-4) +".xml");
							FileOutputStream out = new FileOutputStream(xml);
							IOUtils.write(sentence.toXML(), out);
							IOUtils.closeQuietly(out);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					reactions.add(reaction);
				}
			}
		}
		return reactions;
	}
}

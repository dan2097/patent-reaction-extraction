package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import dan2097.org.bitbucket.utility.ChemicalTaggerAtrs;
import dan2097.org.bitbucket.utility.ChemicalTaggerTags;
import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLAtrs;
import dan2097.org.bitbucket.utility.XMLTags;

public class ExperimentalSectionParser {

	private static Logger LOG = Logger.getLogger(ExperimentalSectionParser.class);
	private final BiMap<Element, Chemical>  moleculeToChemicalMap = HashBiMap.create();
	private final Map<String, Chemical> aliasToChemicalMap;
	private final static Pattern matchIdentifier = Pattern.compile("((\\d+[a-z]?)|[\\(\\{\\[](\\d+[a-z]?|.*\\d+)[\\)\\}\\]])\\s*$");
	private final Chemical titleCompound;
	private final List<Reaction> reactions = new ArrayList<Reaction>();

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
					ChemicalTypeAssigner.performPreliminaryTypeDetection(moleculeToChemicalMap);
				}
			}
			reactions.addAll(determineReactions(paragraphs));
		}
	}

	/**
	 * Retrieves the reactions found by this experimental section parser
	 * @return
	 */
	List<Reaction> getReactions() {
		return reactions;
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
	 * Finds the chemical name of the chemical described by a chemical tagger MOLECULE_Container/UNNAMEDMOLECULE_Container
	 * @param molecule
	 * @return
	 */
	private String findMoleculeName(Element molecule) {
		String elName= molecule.getLocalName();
		if (elName.equals(ChemicalTaggerTags.MOLECULE_Container)) {
			return findMoleculeNameFromMoleculeEl(molecule);
		}
		else if (elName.equals(ChemicalTaggerTags.UNNAMEDMOLECULE_Container)) {
			return findMoleculeNameFromEl(molecule);
		}
		throw new IllegalArgumentException("Unexpected tag type:" + elName +" The following are allowed" +
				ChemicalTaggerTags.MOLECULE_Container + ", "+ ChemicalTaggerTags.UNNAMEDMOLECULE_Container);
	}
	
	
	/**
	 * Finds the chemical name of the chemical described by a chemical tagger MOLECULE tag
	 * @param molecule
	 * @return
	 */
	private String findMoleculeNameFromMoleculeEl(Element molecule) {
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
	 * Returns the space concatenated value of the tags except those tags that are quantity tags of children thereof
	 * @param molecule
	 * @return
	 */
	private String findMoleculeNameFromEl(Element molecule) {
		StringBuilder builder = new StringBuilder();
		LinkedList<Element> stack = new LinkedList<Element>();
		stack.add(molecule);
		while (!stack.isEmpty()) {
			Element currentEl =stack.removeFirst();
			Elements els = currentEl.getChildElements();
			if (els.size()>0){
				for (int i = 0; i < els.size(); i++) {
					Element elToConsider =els.get(i);
					if (!elToConsider.getLocalName().equals(ChemicalTaggerTags.QUANTITY_Container)){
						stack.add(elToConsider);
					}
				}
			}
			else{
				builder.append(currentEl.getValue());
				builder.append(' ');
			}
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}


	/**
	 * Creates a list of all MOLECULE or UNNAMEDMOLECULE elements
	 * @return
	 */
	private List<Element> findAllMolecules(Paragraph paragraph) {
		List <Element> mols = new ArrayList<Element>();
		Document chemicalTaggerResult =paragraph.getTaggedSentencesDocument();
		Nodes molecules = chemicalTaggerResult.query("//*[self::MOLECULE or self::UNNAMEDMOLECULE]");
		for (int i = 0; i < molecules.size(); i++) {
			Element molecule = (Element) molecules.get(i);
			mols.add(molecule);
		}
		return mols;
	}


	private List<Reaction> determineReactions(List<Paragraph> paragraphs) {
		List<Reaction> reactions = new ArrayList<Reaction>();
		for (Paragraph paragraph : paragraphs) {
			Reaction currentReaction = new Reaction();
			Element taggedDocRoot = paragraph.getTaggedSentencesDocument().getRootElement();
			List<Element> sentences = XOMTools.getChildElementsWithTagName(taggedDocRoot, ChemicalTaggerTags.SENTENCE_Container);
			for (Element sentence : sentences) {
				int indexToReattachAt = taggedDocRoot.indexOf(sentence);
				sentence.detach();
				Set<Element> products = identifyProducts(sentence);
				identifyReactants(sentence, products);
				identifySpectators(sentence);
		
				List<Element> mols = XOMTools.getDescendantElementsWithTagNames(sentence, new String[]{ChemicalTaggerTags.MOLECULE_Container, ChemicalTaggerTags.UNNAMEDMOLECULE_Container});
				List<Chemical> chemicals = new ArrayList<Chemical>();
				for (Element mol : mols) {
					Chemical cm = moleculeToChemicalMap.get(mol);
					if (ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(mol.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
						cm.setRole(ChemicalRole.spectator);
					}
					if (cm.getType()!= ChemicalType.falsePositive){
						chemicals.add(cm);
					}
					else{
						LOG.trace(cm.getName() +" is believed to be a false positive and has been ignored");
					}
				}
				Reaction tempReaction = new Reaction();
				List<Chemical> unassignedChemicals = new ArrayList<Chemical>();
				for (Chemical chemical : chemicals) {
					if (chemical.getType() ==ChemicalType.exact){
						if (chemical.getRole() == ChemicalRole.product){
							tempReaction.addProduct(chemical);
						}
						else if (chemical.getRole() == ChemicalRole.reactant){
							tempReaction.addReactant(chemical);
						}
						else if (chemical.getRole() == ChemicalRole.spectator){
							tempReaction.addSpectator(chemical);
						}
						else{
							unassignedChemicals.add(chemical);
						}
					}
					else if (chemical.getType() ==ChemicalType.exactReference){
						if (attemptToResolveBackReference(chemical)){
							if (chemical.getRole() == ChemicalRole.product){
								tempReaction.addProduct(chemical);
							}
							else if (chemical.getRole() == ChemicalRole.reactant){
								tempReaction.addReactant(chemical);
							}
							else if (chemical.getRole() == ChemicalRole.spectator){
								tempReaction.addSpectator(chemical);
							}
							else{
								unassignedChemicals.add(chemical);
							}
						}
						else {
							LOG.debug(chemical.getName() +" is believed to be a back reference but could not be resolved");
						}
					}
					else{
						unassignedChemicals.add(chemical);
					}
				}
				unassignedChemicals =removeChemicalsKnownToBeIrrelevant(unassignedChemicals, sentence);
				if (!tempReaction.getProducts().isEmpty() && !currentReaction.getProducts().isEmpty()){
					reactions.add(currentReaction);
					currentReaction= tempReaction;
				}
				else{
					currentReaction.importReaction(tempReaction);
				}
				taggedDocRoot.insertChild(sentence, indexToReattachAt);
			}
			if (currentReaction.getProducts().size()>0 || currentReaction.getReactants().size()>0){
				reactions.add(currentReaction);
			}
		}
		
		return reactions;
	}

	private boolean attemptToResolveBackReference(Chemical chemical) {
		if (chemical.getName().equalsIgnoreCase("title compound")){
			chemical.setSmiles(titleCompound.getSmiles());
			chemical.setInchi(titleCompound.getInchi());
			chemical.setRole(ChemicalRole.product);
			return true;
		}
		return false;
	}

	/**
	 * Given a list of unassignable chemicals checks whether these chemicals appear in places known to not fit into the rigid roles this program attempts to assign e.g. work-up
	 * @param unassignedChemicals
	 * @param sentence
	 * @return
	 */
	private List<Chemical> removeChemicalsKnownToBeIrrelevant(List<Chemical> unassignedChemicals, Element sentence) {
		for (int i = unassignedChemicals.size()-1; i >=0; i--) {
			Element mol = moleculeToChemicalMap.inverse().get(unassignedChemicals.get(i));
			for (String xpath : Xpaths.moleculesToIgnoreXpaths) {
				if (mol.query(xpath).size()>0){
					unassignedChemicals.remove(i);
					break;
				}
			}
		}
		for (String xpath : Xpaths.referencesToPreviousReactions) {
			Nodes moleculesToRemove = sentence.query(xpath);
			for (int i = 0; i < moleculesToRemove.size(); i++) {
				unassignedChemicals.remove(moleculeToChemicalMap.get(moleculesToRemove.get(i)));
			}
		}
		return unassignedChemicals;
	}

	/**
	 * Identifies products using yieldXpaths
	 * @param sentence
	 * @return 
	 */
	private Set<Element> identifyProducts(Element sentence) {
		Set<Element> products = new HashSet<Element>();
		for (String xpath : Xpaths.yieldXPaths) {
			Nodes synthesizedMolecules = sentence.query(xpath);
			for (int i = 0; i < synthesizedMolecules.size(); i++) {
				Element synthesizedMolecule= (Element) synthesizedMolecules.get(i);
				products.add(synthesizedMolecule);
				Chemical chem = moleculeToChemicalMap.get(synthesizedMolecule);
				if (chem.getRole()==null){
					chem.setXpathUsedToIdentify(xpath);
					chem.setRole(ChemicalRole.product);
				}
			}
		}
		return products;
	}
	
	/**
	 * Identifiers reactants using reactantXpathsRel and reactantXpathsAbs
	 * @param sentence
	 * @param products
	 */
	private void identifyReactants(Element sentence, Set<Element> products) {
		for (Element product : products) {
			for (String xpath : Xpaths.reactantXpathsRel) {
				Nodes reactantMolecules = product.query(xpath);
				for (int i = 0; i < reactantMolecules.size(); i++) {
					Element reactantMol= (Element) reactantMolecules.get(i);
					Chemical chem = moleculeToChemicalMap.get(reactantMol);
					if (chem.getRole()==null){
						chem.setXpathUsedToIdentify(xpath);
						chem.setRole(ChemicalRole.reactant);
					}
				}
			}
		}
		for (String xpath : Xpaths.reactantXpathsAbs) {
			Nodes reactantMolecules = sentence.query(xpath);
			for (int i = 0; i < reactantMolecules.size(); i++) {
				Element reactantMol= (Element) reactantMolecules.get(i);
				Chemical chem = moleculeToChemicalMap.get(reactantMol);
				if (chem.getRole()==null){
					chem.setXpathUsedToIdentify(xpath);
					chem.setRole(ChemicalRole.reactant);
				}
			}
		}
	}
	
	/**
	 * Identifies spectator molecules using spectatorXpathsAbs
	 * @param sentence
	 */
	private void identifySpectators(Element sentence) {
		for (String xpath : Xpaths.spectatorXpathsAbs) {
			Nodes spectatorMolecules = sentence.query(xpath);
			for (int i = 0; i < spectatorMolecules.size(); i++) {
				Element spectatorMol= (Element) spectatorMolecules.get(i);
				Chemical chem = moleculeToChemicalMap.get(spectatorMol);
				if (chem.getRole()==null){
					chem.setXpathUsedToIdentify(xpath);
					chem.setRole(ChemicalRole.spectator);
				}
			}
		}
	}
}

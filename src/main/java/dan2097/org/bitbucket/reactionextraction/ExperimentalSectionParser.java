package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ggasoftware.indigo.IndigoObject;
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

import static dan2097.org.bitbucket.utility.Utils.*;

public class ExperimentalSectionParser {
	private static Logger LOG = Logger.getLogger(ExperimentalSectionParser.class);
	private final BiMap<Element, Chemical>  moleculeToChemicalMap = HashBiMap.create();
	private final Map<String, Chemical> aliasToChemicalMap;
	private final Chemical titleCompound;
	private final List<Element> paragraphEls;
	private final List<Reaction> reactions = new ArrayList<Reaction>();
	private final Pattern matchAmount = Pattern.compile("([mn\u00b5]|pico|nano|micro|milli)?mol[e]?[s]?");

	public ExperimentalSectionParser(Chemical titleCompound, List<Element> paragraphEls, Map<String, Chemical> aliasToChemicalMap) {
		this.titleCompound = titleCompound;
		this.paragraphEls = paragraphEls;
		this.aliasToChemicalMap = aliasToChemicalMap;
	}

	public void parseForReactions(){
		if (titleCompound!=null){
			List<Paragraph> paragraphs = new ArrayList<Paragraph>();
			for (Element p : paragraphEls) {
				Paragraph para = new Paragraph(p);
				if (!para.getTaggedString().equals("")){
					paragraphs.add(para);
					generateMoleculeToChemicalMap(para);
					ChemicalTypeAssigner.performPreliminaryTypeDetection(moleculeToChemicalMap);
				}
			}
			reactions.addAll(determineReactions(paragraphs));
			processReactionStoichiometry();
		}
	}

	/**
	 * Retrieves the reactions found by this experimental section parser
	 * @return
	 */
	public List<Reaction> getReactions() {
		return reactions;
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


	/**
	 * Master method for extracting reactions from a list of paragraphs
	 * @param paragraphs
	 * @return
	 */
	private List<Reaction> determineReactions(List<Paragraph> paragraphs) {
		List<Reaction> reactions = new ArrayList<Reaction>();
		for (Paragraph paragraph : paragraphs) {
			Reaction currentReaction = new Reaction();
			paragraph.segmentIntoSections(moleculeToChemicalMap);
			Map<Element, PhraseType> phraseMap = paragraph.getPhraseMap();
			for (Element phrase : phraseMap.keySet()) {
				Reaction tempReaction = new Reaction();
				Set<Element> reagents;
				if (phraseMap.get(phrase).equals(PhraseType.synthesis)){
					reagents = findAllReagents(phrase);
				}
				else{
					reagents = Collections.emptySet();
				}
				Set<Element> products = identifyProducts(phrase);
				reagents.removeAll(products);
				Set<Element> chemicals = new LinkedHashSet<Element>(products);
				chemicals.addAll(reagents);
				resolveBackReferencesAndChangeRoleIfNecessary(chemicals, reactions);
				preliminaryClassifyReagentsAsReactantsAndSolvents(reagents);
				for (Element chemical : chemicals) {
					Chemical chemChem = moleculeToChemicalMap.get(chemical);
					if (ChemicalRole.product.equals(chemChem.getRole())){
						tempReaction.addProduct(chemChem);
					}
					else if (ChemicalRole.reactant.equals(chemChem.getRole())){
						tempReaction.addReactant(chemChem);
					}
					else if (ChemicalRole.solvent.equals(chemChem.getRole())){
						tempReaction.addSpectator(chemChem);
					}
					else if (!ChemicalType.falsePositive.equals(chemChem.getType())){
						LOG.debug("Role not assigned to: " +chemChem.getName());
					}
				}
		
				currentReaction.importReaction(tempReaction);
				if (!currentReaction.getProducts().isEmpty()){
					currentReaction.setInput(paragraph);
					reactions.add(currentReaction);
					currentReaction = new Reaction();
				}
			}
			if (currentReaction.getProducts().size()>0 || currentReaction.getReactants().size()>0){
				currentReaction.setInput(paragraph);
				reactions.add(currentReaction);
			}
		}
		addImplicitTitleCompoundToFinalReactionIfRequired(reactions);
		for (Reaction reaction : reactions) {
			new ChemicalSenseApplication(reaction).reassignMisCategorisedReagents();
		}
		return reactions;
	}

	private void preliminaryClassifyReagentsAsReactantsAndSolvents(Set<Element> reagents) {
		for (Element reagent : reagents) {
			Chemical cm = moleculeToChemicalMap.get(reagent);
			if (cm.getType()== ChemicalType.falsePositive){
				LOG.trace(cm.getName() +" is believed to be a false positive and has been ignored");
			}
			else if (ChemicalTaggerAtrs.SOLVENT_ROLE_VAL.equals(reagent.getAttributeValue(ChemicalTaggerAtrs.ROLE_ATR))){
				cm.setRole(ChemicalRole.solvent);
			}
			else if (cm.getVolumeValue()!=null && !cm.getVolumeValue().contains(".") && cm.getAmountValue()==null){
				//solvents will be liquids but typically with imprecise volume and no amount given
				cm.setRole(ChemicalRole.solvent);
			}
			else {
				cm.setRole(ChemicalRole.reactant);
			}
		}
	}

	private Set<Element> findAllReagents(Element el) {
		List<Element> mols = XOMTools.getDescendantElementsWithTagNames(el, new String[]{ChemicalTaggerTags.MOLECULE_Container, ChemicalTaggerTags.UNNAMEDMOLECULE_Container});
		return new LinkedHashSet<Element>(mols);
	}

	/**
	 * Identifies products using yieldXpaths
	 * @param phrase
	 * @return 
	 */
	private Set<Element> identifyProducts(Element phrase) {
		Set<Element> products = new LinkedHashSet<Element>();
		for (String xpath : Xpaths.yieldXPaths) {
			Nodes synthesizedMolecules = phrase.query(xpath);
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

	private void resolveBackReferencesAndChangeRoleIfNecessary(Set<Element> chemicals, List<Reaction> reactions) {
		for (Element chemical : chemicals) {
			Chemical chemChem = moleculeToChemicalMap.get(chemical);
			if (chemChem.getType() ==ChemicalType.exactReference){
				attemptToResolveBackReference(chemChem, reactions);
			}
		}
		
	}

	boolean attemptToResolveBackReference(Chemical chemical, List<Reaction> reactionsToConsider) {
		if (chemical.getName().equalsIgnoreCase("title compound")){
			chemical.setSmiles(titleCompound.getSmiles());
			chemical.setInchi(titleCompound.getInchi());
			chemical.setRole(ChemicalRole.product);
			return true;
		}
		if (chemical.getSmiles()!=null){
			boolean success = attemptToResolveViaSubstructureMatch(chemical, reactionsToConsider);
			if (success){
				chemical.setRole(ChemicalRole.reactant);
				return true;
			}
			//The <name of compound> could also be specific
			Element molecule = moleculeToChemicalMap.inverse().get(chemical);
			Element previous = Utils.getPreviousElement(molecule);
			if (previous !=null && previous.getLocalName().equals(ChemicalTaggerTags.DT_THE)){
				chemical.setType(ChemicalType.exact);
				return true;
			}
		}
		return false;
	}

	private boolean attemptToResolveViaSubstructureMatch(Chemical backReference, List<Reaction> reactionsToConsider) {
		IndigoObject query = indigo.loadSmarts(backReference.getSmiles());
		List<Chemical> chemicalMatches = new ArrayList<Chemical>();
		for (Reaction reaction : reactionsToConsider) {
			List<Chemical> products = reaction.getProducts();
			for (Chemical chemical : products) {
				if (chemical.getSmiles()!=null){
					IndigoObject substructureMatcher = indigo.substructureMatcher(indigo.loadMolecule(chemical.getSmiles()));
					if (substructureMatcher.match(query) !=null){
						chemicalMatches.add(chemical);
					}
				}
			}
		}
		if (chemicalMatches.size()==1){
			Chemical anaphoraChem = chemicalMatches.get(0);
			backReference.setSmiles(anaphoraChem.getSmiles());
			backReference.setInchi(anaphoraChem.getInchi());
			return true;
		}
		return false;
	}

	private void addImplicitTitleCompoundToFinalReactionIfRequired(List<Reaction> reactions) {
		if (reactions.size()>0){
			Reaction lastReaction = reactions.get(reactions.size()-1);
			if (lastReaction.getProducts().size()==0){//product was probably implicitly the title compound
				boolean titleCompoundUsedAsProduct =false;
				if (titleCompound.getInchi()!=null){
					reactionLoop: for (int i = 0; i < reactions.size()-1; i++) {
						Reaction reaction =reactions.get(i);
						for (Chemical product : reaction.getProducts()) {
							if (titleCompound.getInchi().equals(product.getInchi())){
								titleCompoundUsedAsProduct =true;
								break reactionLoop;
							}
						}
					}
				}
				if (!titleCompoundUsedAsProduct){
					lastReaction.addProduct(titleCompound);
				}
			}
		}
	}
	
	/**
	 * Uses the amount(mols) of reactants to give the correct stoichiometry
	 */
	void processReactionStoichiometry() {
		for (Reaction reaction : reactions) {
			Map<Chemical, Double> reactantToAmount = new HashMap<Chemical, Double>();
			for (Chemical reactant : reaction.getReactants()) {
				if (reactant.getAmountValue()!=null){
					try{
						double amount = Double.parseDouble(reactant.getAmountValue());
						String units = reactant.getAmountUnits();
						Matcher m = matchAmount.matcher(units);
						if (m.matches()){
							String scale = m.group(1);
							if (scale != null){
								if (scale.equalsIgnoreCase("m") || scale.equalsIgnoreCase("milli")){
									amount = amount/1000;
								}
								if (scale.equalsIgnoreCase("\u00b5") || scale.equalsIgnoreCase("micro")){
									amount = amount/1000000;
								}
								if (scale.equalsIgnoreCase("n") || scale.equalsIgnoreCase("nano")){
									amount = amount/1000000000;
								}
								if (scale.equalsIgnoreCase("pico")){
									amount = amount/1000000000000d;	
								}
							}
							reactantToAmount.put(reactant, amount);
						}
						
					}
					catch (NumberFormatException e) {
						LOG.trace(reactant.getAmountValue() +" was not a valid double");
					}
				}
			}
			if (reactantToAmount.keySet().size()>1){
				double lowestAmount = Double.MAX_VALUE;
				for (double amountInMols: reactantToAmount.values()) {
					if (amountInMols < lowestAmount){
						lowestAmount = amountInMols;
					}
				}
				for (Entry<Chemical, Double> entries: reactantToAmount.entrySet()) {
					double stoichiometry = 1;
					if (entries.getValue() != lowestAmount){
						stoichiometry = entries.getValue()/lowestAmount;
					}
					entries.getKey().setStoichiometry(stoichiometry);
				}
			}
		}
		
	}

}

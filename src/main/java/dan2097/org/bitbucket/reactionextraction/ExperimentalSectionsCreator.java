package dan2097.org.bitbucket.reactionextraction;

import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.chemicaltagger.Formatter;
import uk.ac.cam.ch.wwmm.opsin.StringTools;
import uk.ac.cam.ch.wwmm.opsin.XOMTools;
import dan2097.org.bitbucket.paragraphclassification.ParagraphClassifier;
import dan2097.org.bitbucket.utility.ParagraphClassifierHolder;
import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLAtrs;
import dan2097.org.bitbucket.utility.XMLTags;

public class ExperimentalSectionsCreator {
	private static Logger LOG = Logger.getLogger(ExperimentalSectionsCreator.class);
	private static ParagraphClassifier paragraphClassifier = ParagraphClassifierHolder.getInstance();
	private static Pattern matchCompoundWith = Pattern.compile("(; )?(compd\\. with|compound with)", Pattern.CASE_INSENSITIVE);
	
	private final List<Element> orderedHeadingsAndParagraphs;
	private final List<ExperimentalSection> experimentalSections =new ArrayList<ExperimentalSection>();
	private ExperimentalSection currentSection = new ExperimentalSection();
	private int unnamedProcedureCounter =1;

	public ExperimentalSectionsCreator(List<Element> orderedHeadingsAndParagraphs) {
		this.orderedHeadingsAndParagraphs = orderedHeadingsAndParagraphs;
	}

	/**
	 * Attempts to return an experimental section for each example reaction.
	 * A multi step reaction should be contained within an experimental section
	 * @return
	 */
	public List<ExperimentalSection> createSections() {
		for (Element element : orderedHeadingsAndParagraphs) {
			if (isHeading(element)){
				handleHeading(element);
			}
			else{
				handleParagraph(element);
			}
		}
		addCurrentSectionIfNonEmptyAndReset();
		return experimentalSections;
	}

	boolean isHeading(Element headingOrParagraph) {
		String name = headingOrParagraph.getLocalName();
		if (name.equals(XMLTags.HEADING)){
			return true;
		}
		if (name.equals(XMLTags.P)){
			String id = headingOrParagraph.getAttributeValue(XMLAtrs.ID); 
			if (id !=null && id.startsWith("h-") && !headingOrParagraph.getValue().contains("\n")){
				String text = Utils.getElementText(headingOrParagraph);
				Document taggedDoc = Utils.runChemicalTagger(text);
				boolean isNonChemicalHeading = isAllCapitalLetters(text);
				List<Element> moleculesFound = isNonChemicalHeading ? new ArrayList<Element>() : extractNonFalsePositiveMoleculeEls(taggedDoc.getRootElement());
				List<Element> procedureNames = extractProcedureNames(taggedDoc.getRootElement());
				return moleculesFound.size() >0 || procedureNames.size() >0;
			}
			else{
				return false;
			}
		}
		else{
			throw new IllegalArgumentException("Unexpected element local name: " + name);
		}
	}

	/**
	 * Returns the molecules elements within the given element that do not match a false Positive Pattern
	 * @param taggedDocRoot
	 * @return
	 */
	List<Element> extractNonFalsePositiveMoleculeEls(Element taggedDocRoot) {
		List<Element> moleculesFound = XOMTools.getDescendantElementsWithTagName(taggedDocRoot, MOLECULE_Container);
		for (int i = moleculesFound.size() -1; i >= 0; i--) {
			List<String> nameComponents = ChemTaggerOutputNameExtraction.findMoleculeName(moleculesFound.get(i));
			String chemicalName = StringTools.stringListToString(nameComponents, " ");
			if (ChemicalTypeAssigner.isFalsePositive(chemicalName, moleculesFound.get(i))){
				moleculesFound.remove(i);
			}
		}
		return moleculesFound;
	}
	
	/**
	 * If there are two molecules with their OSCARCM_Containers separated by "compound with" removes them an creates a new molecule with the union of the two molecules
	 * @param moleculesFound
	 * @param taggedDocRoot
	 * @return
	 */
	 void correctCompoundWithSpecialCase(List<Element> moleculesFound, Element taggedDocRoot) {
		if (moleculesFound.size()==2){
			LinkedList<Element> stack = new LinkedList<Element>();
			stack.add(taggedDocRoot);
			List<Element> interveningElements = new ArrayList<Element>();
			boolean seenMolecule = false;
			while (stack.size()>0){
				Element currentElement =stack.removeLast();
				Elements children =currentElement.getChildElements();
				if (seenMolecule && children.size()==0){
					interveningElements.add(currentElement);
				}
				if (currentElement.getLocalName().equals(OSCARCM_Container)){
					if (seenMolecule){
						StringBuilder sb = new StringBuilder();
						for (Element el : interveningElements) {
							if (sb.length()!=0){
								sb.append(' ');
							}
							sb.append(el.getValue());
						}
						String beforeMoleculeString = sb.toString();
						if (matchCompoundWith.matcher(beforeMoleculeString).matches()){//assumption made that "compound with" cannot occur within the same molecule
							Element firstOscarCMContainer = moleculesFound.get(0).getFirstChildElement(OSCARCM_Container);
							Element secondOscarCMContainer = moleculesFound.get(1).getFirstChildElement(OSCARCM_Container);
							if (firstOscarCMContainer !=null && secondOscarCMContainer !=null){
								//create a new molecule from the union of the two molecules
								Element newMolecule = new Element(MOLECULE_Container);
								newMolecule.appendChild(new Element(firstOscarCMContainer));
								Element newOscarCMContainer = newMolecule.getFirstChildElement(OSCARCM_Container);
								for (int i = 0; i < interveningElements.size(); i++) {
									Element interveningElCopy = new Element(interveningElements.get(i));
									if (interveningElCopy.getLocalName().equals(STOP)){//semicolon can screw up name to structure
										continue;
									}
									interveningElCopy.setLocalName(OSCAR_CM);
									newOscarCMContainer.appendChild(interveningElCopy);
								}
								Elements oscarcms = secondOscarCMContainer.getChildElements(OSCAR_CM);
								for (int i = 0; i < oscarcms.size(); i++) {
									newOscarCMContainer.appendChild(new Element(oscarcms.get(i)));
								}
								moleculesFound.clear();
								moleculesFound.add(newMolecule);
							}
						}
						break;
					}
					seenMolecule =true;
				}
				else{
					for (int i = children.size()-1; i >=0; i--) {
						stack.add(children.get(i));
					}
				}
			}
		}
	}
	
	/**
	 * Extracts a procedures and/or molecule from the heading and adds it to the current section
	 * or step of the current section as a appropriate
	 * @param headingEl
	 */
	private void handleHeading(Element headingEl) {
		String text = Utils.getElementText(headingEl);
		if (text.length()>35000){
			//far too long to be an appropriate heading
			addCurrentSectionIfNonEmptyAndReset();
			return;
		}
		Document taggedDoc = Utils.runChemicalTagger(text);
		boolean isNonChemicalHeading = isAllCapitalLetters(text);
		List<Element> moleculesFound = isNonChemicalHeading ? new ArrayList<Element>() : extractNonFalsePositiveMoleculeEls(taggedDoc.getRootElement());
		correctCompoundWithSpecialCase(moleculesFound, taggedDoc.getRootElement());
		List<Element> procedureNames = extractProcedureNames(taggedDoc.getRootElement());
		if (moleculesFound.size()!=1 && procedureNames.size()!=1){
			//doesn't appear to be an appropriate heading
			addCurrentSectionIfNonEmptyAndReset();
			return;
		}
		boolean isSubHeading = isSubHeading(headingEl, taggedDoc.getRootElement());
		if (procedureNames.size()==1){
			addProcedure(procedureNames.get(0), isSubHeading);
		}
		if (moleculesFound.size()==1){
			String alias = TitleTextAliasExtractor.findAlias(text);
			ChemicalAliasPair nameAliasPair = new ChemicalAliasPair(createChemicalFromHeadingMoleculeEl(moleculesFound.get(0)), alias);
			addNameAliasPair(nameAliasPair);
		}
	}

	/**
	 * Creates a chemical from the name contained with the moleculeEl
	 * If the name appears to be a chemical class e.g. "amide", "a benzaldehyde" smiles/inchi are not set
	 * @param moleculeEl
	 * @return
	 */
	private Chemical createChemicalFromHeadingMoleculeEl(Element moleculeEl) {
		List<String> nameComponents = ChemTaggerOutputNameExtraction.findMoleculeName(moleculeEl);
		Chemical chem = Utils.createChemicalFromName(nameComponents);
		String name = chem.getName();
		String smarts = FunctionalGroupDefinitions.getSmartsFromChemicalName(name);
		chem.setSmarts(smarts);
		if ((smarts !=null && FunctionalGroupDefinitions.getFunctionalClassSmartsFromChemicalName(name)!=null) || ChemicalEntityType.chemicalClass.equals(ChemicalTypeAssigner.determineTypeFromSurroundingText(moleculeEl))){
			chem.setChemicalIdentifierPair(new ChemicalIdentifierPair(null, null));
		}
		return chem;
	}

	private boolean isAllCapitalLetters(String text) {
		for (char charac : text.toCharArray()) {
			if (!Character.isWhitespace(charac) && !Character.isUpperCase(charac)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds a procedure to an appropriate place in the current section or step
	 * @param procedure
	 * @param isSubHeading
	 */
	private void addProcedure(Element procedure, boolean isSubHeading) {
		if (isSubHeading){
			if (currentSection.getCurrentStepProcedureElement()!=null && !currentSection.currentStepHasParagraphs()){
				LOG.trace(currentSection.getCurrentStepProcedureElement().toXML() + " was discarded!");
			}
			currentSection.moveToNextStep();
			currentSection.setCurrentStepProcedure(procedure);
		}
		else {
			if (currentSection.getExperimentalSteps().size()==1 && !currentSection.currentStepHasParagraphs()){
				if (currentSection.getProcedureElement()!=null){
					LOG.trace(currentSection.getProcedureElement().toXML() + " was discarded!");
				}
				if (currentSection.getCurrentStepProcedureElement()!=null){
					LOG.trace(currentSection.getCurrentStepProcedureElement().toXML() + " was discarded!");
				}
				if (currentSection.getTargetChemicalNamePair()!=null){
					LOG.trace(currentSection.getTargetChemicalNamePair() + " was discarded!");
				}
				if (currentSection.getCurrentStepTargetChemicalNamePair()!=null){
					LOG.trace(currentSection.getCurrentStepTargetChemicalNamePair() + " was discarded!");
				}
			}
			addCurrentSectionIfNonEmptyAndReset();
			currentSection.setProcedureElement(procedure);
		}
	}

	/**
	 * Adds a ChemicalNameAliasPair to an appropriate place in the current section or step
	 * @param nameAliasPair
	 */
	private void addNameAliasPair(ChemicalAliasPair nameAliasPair) {
		if (currentSection.currentStepHasParagraphs()){
			addCurrentSectionIfNonEmptyAndReset();
		}
		if (currentSection.getCurrentStepProcedureElement()!=null || currentSection.getTargetChemicalNamePair()!=null){
			if (currentSection.getCurrentStepTargetChemicalNamePair()!=null){
				LOG.trace(currentSection.getCurrentStepTargetChemicalNamePair() + " was discarded!");
			}
			currentSection.setCurrentStepTargetChemicalNamePair(nameAliasPair);
		}
		else{
			currentSection.setTargetChemicalNamePair(nameAliasPair);
		}
	}

	private List<Element> extractProcedureNames(Element taggedDocRoot) {
		return XOMTools.getDescendantElementsWithTagName(taggedDocRoot, PROCEDURE_Container);
	}
	
	/**
	 * Is the given heading a sub heading
	 * @param heading
	 * @param taggedDocRoot 
	 * @return
	 */
	boolean isSubHeading(Element heading, Element taggedDocRoot) {
		if (heading.getLocalName().equals(XMLTags.P)){
			String id = heading.getAttributeValue(XMLAtrs.ID); 
			if (id !=null && id.startsWith("h-")){
				return true;
			}
		}
		List<Element> procedures = XOMTools.getDescendantElementsWithTagName(taggedDocRoot, PROCEDURE_Container);
		if (procedures.size()==1){
			List<Element> methodAndExampleEls =  XOMTools.getDescendantElementsWithTagNames(procedures.get(0), new String[]{NN_METHOD, NN_EXAMPLE});
			if (methodAndExampleEls.size()==0){
				return true;
			}
			for (Element method : methodAndExampleEls) {
				if (ReactionExtractionMethods.isSynonymnOfStep(method.getValue())){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Examines paragraph for a starting procedure/chemical name heading
	 * Then adds the paragraph to the current step
	 * @param paraEl
	 */
	private void handleParagraph(Element paraEl) {
		String text = Utils.detachIrrelevantElementsAndGetParagraphText(paraEl);
		if (text.equals("")){//blank paragraph
			return;
		}
		boolean isExperimentalParagraph = text.length() > 35000 ? false : paragraphClassifier.isExperimental(text);
		if (!isExperimentalParagraph){
			if (currentSection.getCurrentStepProcedureElement()!=null){
				currentSection.moveToNextStep();
			}
			else{
				addCurrentSectionIfNonEmptyAndReset();
			}
			return;
		}
		String identifier = paraEl.getAttributeValue(XMLAtrs.NUM);
		Paragraph para = new Paragraph(text, identifier);
		
		//Sometimes headings are present at the start of paragraphs...
		Element hiddenHeadingEl = findAndDetachHiddenHeadingContent(para.getTaggedSentencesDocument());
		if (hiddenHeadingEl !=null){
			processInlineHeading(hiddenHeadingEl, paraEl, text);
			if (para.getTaggedSentencesDocument().getValue().equals("")){//paragraph was just a heading
				return;
			}
		}

		if (currentSection.getProcedureElement()==null && currentSection.getCurrentStepProcedureElement()==null 
				&& currentSection.getTargetChemicalNamePair()==null && currentSection.getCurrentStepTargetChemicalNamePair()==null ){//typically experimental paragraphs are preceded by a suitable heading
			addCurrentSectionIfNonEmptyAndReset();
			if (isSelfStandingParagraph(para.getTaggedSentencesDocument())){
				currentSection.addParagraphToCurrentStep(para);
			}
			//non self standing paragraphs with no heading are discarded
		}
		else{
			currentSection.addParagraphToCurrentStep(para);
		}
	}

	/**
	 * Similar in function to processHeading except hiddenHeadingEl has already been tagged by chemical tagger
	 * There is a possibility of the heading being a false positive e.g. "LCMS:" so a new section is not started if
	 * this method fails to do anything
	 * @param hiddenHeadingEl
	 * @param text 
	 * @param paraEl 
	 */
	private void processInlineHeading(Element hiddenHeadingEl, Element paraEl, String text) {
		String headingText = findTextCorrespondingToChemicallyTaggedText(hiddenHeadingEl, text);
		List<Element> moleculesFound = extractNonFalsePositiveMoleculeEls(hiddenHeadingEl);
		correctCompoundWithSpecialCase(moleculesFound, hiddenHeadingEl);
		List<Element> procedureNames = extractProcedureNames(hiddenHeadingEl);
		boolean isSubHeading = isSubHeading(paraEl, hiddenHeadingEl);
		if (procedureNames.size()==1){
			addProcedure(procedureNames.get(0), isSubHeading);
		}
		if (moleculesFound.size()==1){
			String alias = TitleTextAliasExtractor.findAlias(headingText);
			ChemicalAliasPair nameAliasPair = new ChemicalAliasPair(createChemicalFromHeadingMoleculeEl(moleculesFound.get(0)), alias);
			addNameAliasPair(nameAliasPair);
		}
	}

	/**
	 * Find the text corresponding to chemical tagger's input.
	 * This is non trivial as chemical tagger does not employ a pure white space tokenizer
	 * @param hiddenHeadingEl
	 * @param text
	 * @return
	 */
	private String findTextCorrespondingToChemicallyTaggedText(Element taggedTextEl, String text) {
		String extractedText = Utils.getElementText(taggedTextEl);
		char[] extractedCharArray = extractedText.toCharArray();
		char[] inputCharArray = Formatter.normaliseText(text).toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0, j=0; i < inputCharArray.length && j < extractedCharArray.length; i++, j++) {
			if (inputCharArray[i] == ' '){
				j--;
			}
			sb.append(inputCharArray[i]);
		}
		return sb.toString().trim();
	}

	/**
	 * Looks for known patterns that are indicative of headings
	 * Detaches the heading element/s and returns them attached to a new heading element
	 * @param taggedDoc
	 * @return
	 */
	Element findAndDetachHiddenHeadingContent(Document taggedDoc) {
		Element firstSentence = taggedDoc.getRootElement().getFirstChildElement(SENTENCE_Container);
		Element heading = null;
		if (firstSentence != null){
			List<Element> elementsToConsider = expandActionPhrases(firstSentence.getChildElements());
			if (elementsToConsider.size() >=1){
				Element firstPhrase =elementsToConsider.get(0);
				if (firstPhrase.getLocalName().equals(NOUN_PHRASE_Container) && nounphraseContainsRecognisedHeadingForm(firstPhrase)){
					if (elementsToConsider.size()>=2){
						Element secondPhrase = elementsToConsider.get(1);
						if (isPeriodOrSemiColonOrColon(secondPhrase)){
							heading = new Element(XMLTags.HEADING);
							detachElementAndEmptySentenceAndActionPhraseParents(firstPhrase);
							detachElementAndEmptySentenceAndActionPhraseParents(secondPhrase);
							heading.appendChild(firstPhrase);
							heading.appendChild(secondPhrase);
							if(elementsToConsider.size() >=4){
								Element thirdPhrase =elementsToConsider.get(2);
								Element fourthPhrase =elementsToConsider.get(3);
								if (thirdPhrase.getLocalName().equals(NOUN_PHRASE_Container) 
										&& nounphraseContainsRecognisedHeadingForm(thirdPhrase)
										&& isPeriodOrSemiColonOrColon(fourthPhrase)){
									detachElementAndEmptySentenceAndActionPhraseParents(thirdPhrase);
									detachElementAndEmptySentenceAndActionPhraseParents(fourthPhrase);
									heading.appendChild(thirdPhrase);
									heading.appendChild(fourthPhrase);
								}
							}
						}
					}
					else{//a sentence which is just a heading e.g. "3)"
						heading = new Element(XMLTags.HEADING);
						detachElementAndEmptySentenceAndActionPhraseParents(firstPhrase);
						heading.appendChild(firstPhrase);
					}
				}
			}
		}
		return heading;
	}

	private void detachElementAndEmptySentenceAndActionPhraseParents(Element element) {
		Element parent = (Element) element.getParent();
		element.detach();
		while (parent.getLocalName().equals(ACTIONPHRASE_Container) && parent.getChildElements().size()==0){
			Node newParent = parent.getParent();
			parent.detach();
			if (!(newParent instanceof Element)){
				break;
			}
			parent = (Element) newParent;
		}
	}

	/**
	 * Returns the list of children with action phrases recursively replaced by their children
	 * @param children
	 * @return
	 */
	private List<Element> expandActionPhrases(Elements children) {
		List<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			if (child.getLocalName().equals(ACTIONPHRASE_Container)){
				elements.addAll(expandActionPhrases(child.getChildElements()));
			}
			else{
				elements.add(child);
			}
		}
		return elements;
	}

	/**
	 * Does the nounPhrase contain a recognised heading form
	 * @param nounPhrase
	 * @return
	 */
	private boolean nounphraseContainsRecognisedHeadingForm(Element nounPhrase) {
		Elements nounPhraseChildren = nounPhrase.getChildElements();
		if (nounPhraseChildren.size()==1){
			String child1Lc = nounPhraseChildren.get(0).getLocalName();
			if (child1Lc.equals(MOLECULE_Container) || child1Lc.equals(PROCEDURE_Container) || child1Lc.equals(UNNAMEDMOLECULE_Container)){
				return true;
			}
		}
		else if (nounPhraseChildren.size()==2){
			String child1Lc = nounPhraseChildren.get(0).getLocalName();
			String child2Lc = nounPhraseChildren.get(1).getLocalName();
			if ((child1Lc.equals(UNNAMEDMOLECULE_Container) || child1Lc.equals(PROCEDURE_Container) )
					&& child2Lc.equals(MOLECULE_Container)){
				return true;
			}
			if (child1Lc.equals(PROCEDURE_Container) && 
					(child2Lc.equals(RRB) || child2Lc.equals(STOP) || child2Lc.equals(COLON))){
				return true;
			}
			if (child1Lc.equals(NN_SYNTHESIZE) && child2Lc.equals(PREPPHRASE_Container) && isAnOfNounPhrasePrepPhrase(nounPhraseChildren.get(1))){
				return true;
			}
		}
		else if (nounPhraseChildren.size()==3){
			String child1Lc = nounPhraseChildren.get(0).getLocalName();
			String child2Lc = nounPhraseChildren.get(1).getLocalName();
			String child3Lc = nounPhraseChildren.get(2).getLocalName();
			if ((child1Lc.equals(UNNAMEDMOLECULE_Container) || child1Lc.equals(PROCEDURE_Container) )
					&& child2Lc.equals(NN_SYNTHESIZE) && child3Lc.equals(PREPPHRASE_Container) && isAnOfNounPhrasePrepPhrase(nounPhraseChildren.get(2))){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Does this prepphrase contain IN-OF NOUNPHRASE
	 * @param prepPhrase
	 * @return
	 */
	private boolean isAnOfNounPhrasePrepPhrase(Element prepPhrase) {
		Elements childrenOfPrepPhrase = prepPhrase.getChildElements();
		if (childrenOfPrepPhrase.size()==2){
			String child1Lc = childrenOfPrepPhrase.get(0).getLocalName();
			String child2Lc = childrenOfPrepPhrase.get(1).getLocalName();
			if (child1Lc.equals(IN_OF) && child2Lc.equals(NOUN_PHRASE_Container)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Is the value of this element a period/semicolon/colon
	 * @param secondPhrase
	 * @return
	 */
	private boolean isPeriodOrSemiColonOrColon(Element el) {
		String value = el.getValue();
		return (value.equals(".") || value.equals(";") || value.equals(":"));
	}

	/**
	 * Checks whether the document has a resolvable chemical that is indicated to have been yielded
	 * @param taggedDoc
	 * @return
	 */
	boolean isSelfStandingParagraph(Document taggedDoc) {
		Nodes yieldNodes = taggedDoc.query("//ActionPhrase[@type='Yield']//MOLECULE");
		for (int i = 0; i < yieldNodes.size(); i++) {
			Element molecule = (Element) yieldNodes.get(i);
			String smiles = Utils.resolveNameToSmiles(ChemTaggerOutputNameExtraction.findMoleculeName(molecule));
			if (smiles != null){
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the currentSection to experimentalSections if it contains experimental paragraphs
	 */
	private void addCurrentSectionIfNonEmptyAndReset() {
		addCurrentSectionIfNonEmpty();
		currentSection = new ExperimentalSection();
	}

	private void addCurrentSectionIfNonEmpty() {
		List<ExperimentalStep> steps = currentSection.getExperimentalSteps();
		if (steps.size()>0){
			ExperimentalStep lastStep = steps.get(steps.size()-1);
			if (lastStep.getParagraphs().size()==0){
				steps.remove(lastStep);
			}
			if (!steps.isEmpty()){
				if (currentSection.getProcedureElement()==null){
					addDummyProcedureElement();
				}
				experimentalSections.add(currentSection);
			}
		}
	}

	/**
	 * Adds a dummy identifier to simplify the implementation of looking up previous reaction data
	 */
	private void addDummyProcedureElement() {
		Element procedureElement = new Element(PROCEDURE_Container);
		Element identifier = new Element(NN_IDENTIFIER);
		identifier.appendChild("unnamedProcedure_" + unnamedProcedureCounter++);
		procedureElement.appendChild(identifier);
		currentSection.setProcedureElement(procedureElement);
	}

}

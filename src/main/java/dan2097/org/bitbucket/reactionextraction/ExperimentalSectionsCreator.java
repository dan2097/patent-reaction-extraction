package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import dan2097.org.bitbucket.paragraphclassification.ParagraphClassifier;
import dan2097.org.bitbucket.utility.ParagraphClassifierHolder;
import dan2097.org.bitbucket.utility.Utils;
import dan2097.org.bitbucket.utility.XMLAtrs;
import dan2097.org.bitbucket.utility.XMLTags;
import static dan2097.org.bitbucket.utility.ChemicalTaggerTags.*;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;

public class ExperimentalSectionsCreator {
	private static Logger LOG = Logger.getLogger(ExperimentalSectionsCreator.class);
	private static ParagraphClassifier paragraphClassifier = ParagraphClassifierHolder.getInstance();
	
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
			boolean isHeading = isHeading(element);
			if (isHeading){
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
				List<String> namesFoundByOpsin = Utils.getSystematicChemicalNamesFromText(text);
				List<Element> procedureNames = extractProcedureNames(taggedDoc.getRootElement());
				return namesFoundByOpsin.size() >0 || procedureNames.size() >0;
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
	 * Extracts a procedures and/or molecule from the heading and adds it to the current section
	 * or step of the current section as a appropriate
	 * @param headingEl
	 */
	private void handleHeading(Element headingEl) {
		String text = Utils.getElementText(headingEl);
		Document taggedDoc = Utils.runChemicalTagger(text);
		List<String> namesFoundByOpsin = Utils.getSystematicChemicalNamesFromText(text);
		List<Element> procedureNames = extractProcedureNames(taggedDoc.getRootElement());
		if (namesFoundByOpsin.size()!=1 && procedureNames.size()!=1){
			//doesn't appear to be an appropriate heading
			addCurrentSectionIfNonEmptyAndReset();
			return;
		}
		boolean isSubHeading = isSubHeading(headingEl, taggedDoc.getRootElement());
		if (procedureNames.size()==1){
			addProcedure(procedureNames.get(0), isSubHeading);
		}
		if (namesFoundByOpsin.size()==1){
			String alias = TitleTextAliasExtractor.findAlias(text);
			String name = namesFoundByOpsin.get(0);
			ChemicalAliasPair nameAliasPair = new ChemicalAliasPair(Utils.createChemicalFromName(name), alias);
			addNameAliasPair(nameAliasPair);
		}
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
			if (currentSection.getProcedureElement()!=null && currentSection.getExperimentalSteps().size()==1 && !currentSection.currentStepHasParagraphs() ){
				LOG.trace(currentSection.getProcedureElement().toXML() + " was discarded!");
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
				if (Utils.isSynonymnOfStep(method.getValue())){
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
		boolean isExperimentalParagraph = paragraphClassifier.isExperimental(text);
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
		List<String> namesFoundByOpsin = Utils.getSystematicChemicalNamesFromText(headingText);
		List<Element> procedureNames = extractProcedureNames(hiddenHeadingEl);
		boolean isSubHeading = isSubHeading(paraEl, hiddenHeadingEl);
		if (procedureNames.size()==1){
			addProcedure(procedureNames.get(0), isSubHeading);
		}
		if (namesFoundByOpsin.size()==1){
			String alias = TitleTextAliasExtractor.findAlias(headingText);
			String name = namesFoundByOpsin.get(0);
			ChemicalAliasPair nameAliasPair = new ChemicalAliasPair(Utils.createChemicalFromName(name), alias);
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
		char[] inputCharArray = text.replaceAll("sulph", "sulf").toCharArray();
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

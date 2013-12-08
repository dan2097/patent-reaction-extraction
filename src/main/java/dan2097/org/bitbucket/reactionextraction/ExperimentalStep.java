package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

class ExperimentalStep {

	private Element procedureEl; 
	private ChemicalAliasPair targetChemicalNamePair;
	private final List<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	Element getProcedureEl() {
		return procedureEl;
	}

	void setProcedureEl(Element procedureEl) {
		this.procedureEl = procedureEl;
	}

	void addParagraph(Paragraph para) {
		paragraphs.add(para);
	}
	
	List<Paragraph> getParagraphs(){
		return paragraphs;
	}

	ChemicalAliasPair getTargetChemicalNamePair() {
		return targetChemicalNamePair;
	}

	void setTargetChemicalNamePair(ChemicalAliasPair targetChemicalNamePair) {
		this.targetChemicalNamePair = targetChemicalNamePair;
	}
}

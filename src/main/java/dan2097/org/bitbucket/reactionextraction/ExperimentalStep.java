package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class ExperimentalStep {

	private Element procedureEl; 
	private ChemicalNameAliasPair targetChemicalNamePair;
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

	ChemicalNameAliasPair getTargetChemicalNamePair() {
		return targetChemicalNamePair;
	}

	void setTargetChemicalNamePair(ChemicalNameAliasPair targetChemicalNamePair) {
		this.targetChemicalNamePair = targetChemicalNamePair;
	}
}

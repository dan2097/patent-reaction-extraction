package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class ExperimentalStep {

	private Element procedureEl; 
	private String targetChemicalName;
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

	String getTargetChemicalName() {
		return targetChemicalName;
	}

	void setTargetChemicalName(String name) {
		targetChemicalName = name;
	}
}

package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class ExperimentalSection {
	private Element procedureElement;
	private ChemicalAliasPair targetChemicalNamePair;
	private final List<ExperimentalStep> experimentalSteps = new ArrayList<ExperimentalStep>();
	private ExperimentalStep currentStep = new ExperimentalStep();
	
	ExperimentalSection() {
		experimentalSteps.add(currentStep);
	}
	
	Element getProcedureElement() {
		return procedureElement;
	}

	void setProcedureElement(Element procedureElement) {
		this.procedureElement = procedureElement;
	}

	ChemicalAliasPair getTargetChemicalNamePair() {
		return targetChemicalNamePair;
	}

	void setTargetChemicalNamePair(ChemicalAliasPair targetChemicalNamePair) {
		this.targetChemicalNamePair = targetChemicalNamePair;
	}
	
	List<ExperimentalStep> getExperimentalSteps() {
		return experimentalSteps;
	}

	Element getCurrentStepProcedureElement(){
		return currentStep.getProcedureEl();
	}

	boolean currentStepHasParagraphs() {
		return currentStep.getParagraphs().size() > 0;
	}
	
	void setCurrentStepProcedure(Element procedureEl) {
		currentStep.setProcedureEl(procedureEl);
	}
	
	void addParagraphToCurrentStep(Paragraph para) {
		currentStep.addParagraph(para);
	}

	ChemicalAliasPair getCurrentStepTargetChemicalNamePair(){
		return currentStep.getTargetChemicalNamePair();
	}
	
	void setCurrentStepTargetChemicalNamePair(ChemicalAliasPair targetChemicalNamePair) {
		currentStep.setTargetChemicalNamePair(targetChemicalNamePair);
	}
	
	void moveToNextStep() {
		currentStep = new ExperimentalStep();
		experimentalSteps.add(currentStep);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("*********\n");
		if (targetChemicalNamePair !=null){
			sb.append("Target Chemical: " + targetChemicalNamePair.getChemical().getName() +"\talias: " + targetChemicalNamePair.getAlias()+"\n");
		}
		else{
			sb.append("Target Chemical: null\n");
		}
		sb.append("Procedure:" + (procedureElement ==null ? "null" :procedureElement.toXML()) +"\n");
		for (ExperimentalStep step : experimentalSteps) {
			if (step.getTargetChemicalNamePair() !=null){
				sb.append("Target Chemical: " +step.getTargetChemicalNamePair().getChemical().getName() +"\talias: " + step.getTargetChemicalNamePair().getAlias() +"\n");
			}
			else{
				sb.append("Target Chemical: null\n");
			}
			sb.append("Procedure:" + (step.getProcedureEl() == null ? "null" : step.getProcedureEl().toXML()) +"\n");
			for (Paragraph para : step.getParagraphs()) {
				sb.append("para number: " + para.getIdentifier() +"\n");
			}
		}
		sb.append("*********\n");
		return sb.toString();
	}
}

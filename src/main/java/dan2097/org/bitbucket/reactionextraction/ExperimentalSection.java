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
		return currentStep.getParagraphs().size()>0;
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

}

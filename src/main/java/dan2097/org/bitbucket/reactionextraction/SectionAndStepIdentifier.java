package dan2097.org.bitbucket.reactionextraction;

class SectionAndStepIdentifier {

	private final String sectionIdentifier;
	private final String stepIdentifier;
	
	SectionAndStepIdentifier(String sectionIdentifier, String stepIdentifier) {
		this.sectionIdentifier = sectionIdentifier;
		this.stepIdentifier = stepIdentifier;
	}
	
	String getSectionIdentifier() {
		return sectionIdentifier;
	}

	String getStepIdentifier() {
		return stepIdentifier;
	}
}

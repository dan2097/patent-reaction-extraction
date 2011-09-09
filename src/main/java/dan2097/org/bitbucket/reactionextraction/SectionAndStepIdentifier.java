package dan2097.org.bitbucket.reactionextraction;

public class SectionAndStepIdentifier {

	private final String sectionIdentifier;
	private final String stepIdentifier;
	
	public SectionAndStepIdentifier(String sectionIdentifier, String stepIdentifier) {
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

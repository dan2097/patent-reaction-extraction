package dan2097.org.bitbucket.utility;

import dan2097.org.bitbucket.paragraphclassification.ParagraphClassifier;

public class ParagraphClassifierHolder {
	private ParagraphClassifierHolder() {}
	 
	private static class SingletonHolder { 
		public static final ParagraphClassifier INSTANCE = new ParagraphClassifier();
	}
 
	public static ParagraphClassifier getInstance() {
		return SingletonHolder.INSTANCE;
	}
}

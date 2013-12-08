package dan2097.org.bitbucket.reactionextraction;

public enum PhraseType {
	synthesis,
	/**Reagents used in workup are currently ignored*/
	workup,
	/**Currently unused. Characterisation is identified indirectly by assigning chemicals within characterisation as false positives */
	characterisaton
}

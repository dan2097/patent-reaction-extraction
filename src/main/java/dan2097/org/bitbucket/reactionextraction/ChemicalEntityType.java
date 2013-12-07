package dan2097.org.bitbucket.reactionextraction;

public enum ChemicalEntityType {
	/** OSCAR EXACT A name that exactly describes the compound it represents*/
	exact,
	/** type of OSCAR CLASS but describes an exact compound e.g. added to pyridine 6*/
	definiteReference,//
	/** OSCAR CLASS e.g. pyridines*/
	chemicalClass,
	/** OSCAR PART e.g. pyridyl*/
	fragment,
	/** False Positive which should not to be included in a reaction<br>
	 * Also applies to unrelated chemicals e.g. NMR solvents*/
	falsePositive;
}

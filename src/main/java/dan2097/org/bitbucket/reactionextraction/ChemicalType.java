package dan2097.org.bitbucket.reactionextraction;

public enum ChemicalType {
	exact,//OSCAR EXACT
	exactReference,//formally OSCAR CLASS but describes an exact compound e.g. added to pyridine 6
	chemicalClass,//OSCAR CLASS
	fragment,//OSCAR PART
	falsePositive;
}

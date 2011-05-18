package dan2097.org.bitbucket.utility;

public class ChemicalTaggerTags {
	
	/** A number followed by a letter*/
	public static final String CD_ALPHANUM = "CD-ALPHANUM";
	
	/** OSCAR chemical adjective e.g. alcoholic*/
	public static final String OSCAR_CJ = "OSCAR-CJ";
	
	/** OSCAR reaction e.g. methylated*/
	public static final String OSCAR_RN = "OSCAR-RN";
	
	/** OSCAR enzyme*/
	public static final String OSCAR_ASE = "OSCAR-ASE";
	
	/** OSCAR ontology term e.g. anything present in ChEBI*/
	public static final String OSCAR_ONT = "OSCAR-ONT";

	/** Unicode trademark*/
	public static final String TM_UNICODE = "TM-UNICODE";
	
	/** A unicode cardinal number?*/
	public static final String CD_UNICODE = "CD-UNICODE";
	
	/** A chemical adjective*/
	public static final String JJ_CHEM = "JJ-CHEM";
	
	/** A chemical adjective that defines an unnamed molecule e.g. "title" */
	public static final String JJ_COMPOUND = "JJ-COMPOUND";

	
	// Prepositions
	public static final String IN_AS = "IN-AS";
	public static final String IN_BEFORE = "IN-BEFORE";
	public static final String IN_AFTER = "IN-AFTER";
	public static final String IN_IN = "IN-IN";
	public static final String IN_INTO = "IN-INTO";
	public static final String IN_WITH = "IN-WITH";
	public static final String IN_WITHOUT = "IN-WITHOUT";
	public static final String IN_BY = "IN-BY";
	public static final String IN_VIA = "IN-VIA";
	public static final String IN_OF = "IN-OF";
	public static final String IN_ON = "IN-ON";
	public static final String IN_FOR = "IN-FOR";
	public static final String IN_FROM = "IN-FROM";
	public static final String IN_UNDER = "IN-UNDER";
	public static final String IN_OVER = "IN-OVER";
	public static final String IN_OFF = "IN-OFF";

	//Modified Nouns
	public static final String NN_STATE = "NN-STATE";
	public static final String NN_TIME = "NN-TIME";
	
	/** contains the text typically describing the units used for mass */
	public static final String NN_MASS = "NN-MASS";
	
	/** contains the text typically describing the units used for amount */
	public static final String NN_AMOUNT = "NN-AMOUNT";

	public static final String NN_MOLAR = "NN-MOLAR";
	
	public static final String NN_ATMOSPHERE = "NN-ATMOSPHERE";
	public static final String NN_EQ = "NN-EQ";
	
	/** contains the text typically describing the units used for volume */
	public static final String NN_VOL = "NN-VOL";
	public static final String NN_CHEMENTITY = "NN-CHEMENTITY";
	public static final String NN_TEMP = "NN-TEMP";
	public static final String NN_FLASH = "NN-FLASH";
	public static final String NN_GENERAL = "NN-GENERAL";
	public static final String NN_METHOD = "NN-METHOD";
	public static final String NN_PRESSURE = "NN-PRESSURE";
	public static final String NN_COLUMN = "NN-COLUMN";
	public static final String NN_CHROMATOGRAPHY = "NN-CHROMATOGRAPHY";
	public static final String NN_VACUUM = "NN-VACUUM";
	public static final String NN_CYCLE = "NN-CYCLE";
	public static final String NN_TIMES = "NN-TIMES";
	public static final String NN_EXAMPLE = "NN-EXAMPLE";

	//Not really Oscar-cm.. but need to be fixed
	/** contains the text for one word of chemical name */
	public static final String OSCAR_CM = "OSCAR-CM";

	//Verbs
	public static final String VB_USE = "VB-USE";
	public static final String VB_CHANGE = "VB-CHANGE";
	public static final String VB_SUBMERGE = "VB-SUBMERGE";
	public static final String VB_SUBJECT = "VB-SUBJECT";

	//Add Tokens
	public static final String NN_ADD = "NN-ADD";
	public static final String NN_MIXTURE = "NN-MIXTURE";
	public static final String VB_DILUTE = "VB-DILUTE";
	public static final String VB_ADD = "VB-ADD";
	public static final String VB_CHARGE = "VB-CHARGE";
	public static final String VB_CONTAIN = "VB-CONTAIN";
	public static final String VB_DROP = "VB-DROP";
	public static final String VB_FILL = "VB-FILL";
	public static final String VB_SUSPEND = "VB-SUSPEND";
	public static final String VB_TREAT = "VB-TREAT";

	//Apparatus Tokens
	public static final String VB_APPARATUS = "VB-APPARATUS";
	public static final String NN_APPARATUS = "NN-APPARATUS";

	//Concentrate Tokens
	public static final String VB_CONCENTRATE = "VB-CONCENTRATE";
	public static final String NN_CONCENTRATE = "NN-CONCENTRATE";

	//Cool Tokens
	public static final String VB_COOL = "VB-COOL";

	//Degass Tokens
	public static final String VB_DEGASS = "VB-DEGASS";

	//Dissolve Tokens
	public static final String VB_DISSOLVE = "VB-DISSOLVE";

	//Dry Tokens
	public static final String VB_DRY = "VB-DRY";
	public static final String NN_DRY = "NN-DRY";

	//Extract Tokens
	public static final String VB_EXTRACT = "VB-EXTRACT";
	public static final String NN_EXTRACT = "NN-EXTRACT";

	//Filter Tokens
	public static final String VB_FILTER = "VB-FILTER";
	public static final String NN_FILTER = "NN-FILTER";

	//Heat Tokens
	public static final String VB_HEAT = "VB-HEAT";
	public static final String VB_INCREASE = "VB-INCREASE";

	//Immerse tokens
	public static final String VB_IMMERSE = "VB-IMMERSE";
	//Partition Tokens
	public static final String VB_PARTITION = "VB-PARTITION";

	//Precipitate Tokens
	public static final String VB_PRECIPITATE = "VB-PRECIPITATE";
	public static final String NN_PRECIPITATE = "NN-PRECIPITATE";

	//Purify Tokens
	public static final String VB_PURIFY = "VB-PURIFY";
	public static final String NN_PURIFY = "NN-PURIFY";

	//Quench Tokens
	public static final String VB_QUENCH = "VB-QUENCH";

	//Recover Tokens
	public static final String VB_RECOVER = "VB-RECOVER";

	//Remove Tokens
	public static final String VB_REMOVE = "VB-REMOVE";
	public static final String NN_REMOVE = "NN-REMOVE";

	//Stir Tokens
	public static final String VB_STIR = "VB-STIR";

	//Synthesize Tokens
	public static final String VB_SYNTHESIZE = "VB-SYNTHESIZE";
	public static final String NN_SYNTHESIZE = "NN-SYNTHESIZE";

	//Wait Tokens
	public static final String VB_WAIT = "VB-WAIT";

	//Wash Tokens
	public static final String VB_WASH = "VB-WASH";

	//Yield Tokens
	public static final String VB_YIELD = "VB-YIELD";

	//Yield Tokens
	public static final String NN_YIELD = "NN-YIELD";
	//Misc Tokens mainly to replace characters that are not markup friendly
	// Conjunctive Adverbs
	public static final String RB_CONJ = "RB-CONJ";
	public static final String COLON = "COLON";
	public static final String COMMA = "COMMA";
	public static final String APOST = "APOST";
	public static final String NEG = "NEG";
	public static final String DASH = "DASH";
	public static final String STOP = "STOP";
	public static final String NN_PERCENT = "NN-PERCENT";
	public static final String LSQB = "LSQB";
	public static final String RSQB = "RSQB";
	
	//The determiner 'the';
	public static final String DT_THE = "DT-THE";

	public static final String LRB = "-LRB-";
	public static final String RRB = "-RRB-";

	//Penn Treebank Tokens

	// Coordinating conjunction (and, or)
	public static final String CC = "CC";

	// Cardinal numeral (one, two, 2, etc.)
	public static final String CD = "CD";

	// Singular determiner/quantifier (this, that)
	public static final String DT = "DT";

	// Existential there
	public static final String EX = "EX";

	// Foreign word (hyphenated before regular tag)
	public static final String FW = "FW";

	// Preposition
	public static final String IN = "IN";

	// Adjective
	public static final String JJ = "JJ";

	// Comparative adjective
	public static final String JJR = "JJR";

	// Semantically superlative adjective (chief, top)
	public static final String JJS = "JJS";

	// List item marker 
	public static final String LS = "LS";

	// Modal auxiliary (can, should, will)
	public static final String MD = "MD";

	// Singular or mass noun
	public static final String NN = "NN";

	// Plural noun
	public static final String NNS = "NNS";

	// Proper noun or part of name phrase
	public static final String NNP = "NNP";

	// Proper noun, plural 
	public static final String NNPS = "NNPS";

	//Predeterminer
	public static final String PDT = "PDT";

	// Possessive ending 
	public static final String POS = "POS";

	//Personal pronoun
	public static final String PRP = "PRP";

	//Possessive pronoun
	public static final String PRP$ = "PRP$";

	// Adverb
	public static final String RB = "RB";

	// Comparative adverb
	public static final String RBR = "RBR";

	// Superlative adverb
	public static final String RBS = "RBS";

	// Adverb/particle (about, off, up)
	public static final String RP = "RP";

	// Symbol
	public static final String SYM = "SYM";

	// Infinitive marker to
	public static final String TO = "TO";

	// Interjection, exclamation
	public static final String UH = "UH";

	// Verb, base form
	public static final String VB = "VB";

	// Verb, past tense
	public static final String VBD = "VBD";

	// Verb, present participle/gerund
	public static final String VBG = "VBG";

	// Verb, past participle
	public static final String VBN = "VBN";

	// Verb, non-3rd person singular present
	public static final String VBP = "VBP";

	// Verb, 3rd. singular present
	public static final String VBZ = "VBZ";

	// Wh- determiner (which, that)
	public static final String WDT = "WDT";

	// wh- pronoun (what, who, whom)
	public static final String WP = "WP";

	// Possessive wh- pronoun (whose)
	public static final String WP$ = "WP$";

	// Wh- adverb (how, where, when)
	public static final String WRB = "WRB";

	//Container nodes
	/**Container for a parsed sentence*/
	public static final String SENTENCE_Container = "Sentence";
	
	/**Contains content that could not be assigned to a known phrasal structure*/
	public static final String UNMATCHED_Container = "Unmatched";

	public static final String NOUN_PHRASE_Container = "NounPhrase";
	
	public static final String MULTIPLEAPPARATUS_Container = "MultipleApparatus";
	
	public static final String DISSOLVEPHRASE_Container = "DissolvePhrase";
	
	public static final String VERBPHRASE_Container = "VerbPhrase";
	
	public static final String CYCLES_Container = "CYCLES";
	
	public static final String RATIO_Container = "RATIO";
	
	public static final String CITATION_Container = "CITATION";
	
	public static final String MIXTURE_Container = "MIXTURE";
	
	public static final String PREPPHRASE_Container = "PrepPhrase";
	
	public static final String TIMEPHRASE_Container = "TimePhrase";
	
	public static final String ROLEPREPPHRASE_Container = "RolePrepPhrase";
	
	public static final String ATMOSPHEREPHRASE_Container = "AtmospherePhrase";
	
	public static final String TEMPPHRASE_Container = "TempPhrase";
	
	/** contains one or more cardinal numbers and NN-AMOUNT */
	public static final String AMOUNT_Container = "AMOUNT";
	
	/** contains one or more cardinal numbers and NN-MASS */
	public static final String MASS_Container = "MASS";
	
	public static final String PERCENT_Container = "PERCENT";
	
	/** contains one or more cardinal numbers and NN-VOL */
	public static final String VOLUME_Container = "VOLUME";
	
	/** contains one or more cardinal numbers and NN-MOLAR */
	public static final String MOLAR_Container = "MOLAR";
	
	/**contains one or more cardinal numbers and NN-EQ  */
	public static final String EQUIVALENT_Container = "EQUIVALENT";
	
	/** contains a PERCENT_Container and NN-YIELD */
	public static final String YIELD_Container = "YIELD";
	
	public static final String APPARATUS_Container = "APPARATUS";
	
	/** Something to do with multiple measurement types*/
	public static final String MULTIPLE_Container = "MULTIPLE";
	
	/** contains OSCAR-CM */
	public static final String OSCARCM_Container = "OSCARCM";
	
	/**Contains a compound identified by OSCAR and measurements about it*/
	public static final String MOLECULE_Container = "MOLECULE";
	
	/**A molecule referred to only by an identifier such as a cardinal number*/
	public static final String UNNAMEDMOLECULE_Container = "UNNAMEDMOLECULE";
	
	/**Holds among other things amount nodes*/
	public static final String QUANTITY_Container = "QUANTITY";

	public static final String OSCARONT_Container = "OSCARONT";
	
	/**Contains a procedure e.g. "example 1"*/
	public static final String PROCEDURE_Container = "PROCEDURE";
	
	/**Holds phrases/punctuation that together form the action phrase*/
	public static final String ACTIONPHRASE_Container = "ActionPhrase";

}

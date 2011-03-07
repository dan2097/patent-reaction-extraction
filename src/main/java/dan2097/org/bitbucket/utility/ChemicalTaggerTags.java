package dan2097.org.bitbucket.utility;

public class ChemicalTaggerTags {
	
	/** OSCAR cardinal number e.g. compound 5*/
	public static final String OSCAR_CD = "OSCAR-CD";
	
	/** OSCAR chemical adjective e.g. alcoholic*/
	public static final String OSCAR_CJ = "OSCAR-CJ";
	
	/** OSCAR reaction e.g. methylated*/
	public static final String OSCAR_RN = "OSCAR-RN";

	/** OSCAR chemical prefix e.g. '1,3-'*/
	public static final String OSCAR_CPR = "OSCAR-CPR";
	
	/** OSCAR ontology term e.g. anything present in ChEBI*/
	public static final String OSCAR_ONT = "OSCAR-ONT";

	/** Unknown not currently used*/
	public static final String TM_UNICODE = "TM-UNICODE";
	
	/** A unicode cardinal number?*/
	public static final String CD_UNICODE = "CD-UNICODE";
	
	/** A chemical adjective*/
	public static final String JJ_CHEM = "JJ-CHEM";

	
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
	public static final String NN_EXAMPLE = "NN-EXAMPLE";
	public static final String NN_STATE = "NN-STATE";
	public static final String NN_TIME = "NN-TIME";

	/** contains the text typically describing the units used for mass */
	public static final String NN_MASS = "NN-MASS";
	public static final String NN_MOLAR = "NN-MOLAR";
	
	/** contains the text typically describing the units used for amount */
	public static final String NN_AMOUNT = "NN-AMOUNT";
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
	public static final String COLON = "COLON";
	public static final String COMMA = "COMMA";
	public static final String APOST = "APOST";
	public static final String NEG = "NEG";
	public static final String DASH = "DASH";
	public static final String STOP = "STOP";
	public static final String NN_PERCENT = "NN-PERCENT";
	public static final String LSQB = "LSQB";
	public static final String RSQB = "RSQB";

	public static final String LRB = "-LRB-";
	public static final String RRB = "-RRB-";

	//Brown Corpus Tokens

	// Pre-qualifier (quite, rather)
	public static final String ABL = "ABL";

	// Pre-quantifier (half, all)
	public static final String ABN = "ABN";

	// Pre-quantifier (both)
	public static final String ABX = "ABX";

	// Post-determiner (many, several, next)
	public static final String AP = "AP";

	// Article (a, the, no)
	public static final String AT = "AT";

	// Be
	public static final String BE = "BE";

	// Were
	public static final String BED = "BED";

	// Was
	public static final String BEDZ = "BEDZ";

	// Being
	public static final String BEG = "BEG";

	// Am
	public static final String BEM = "BEM";

	// Been
	public static final String BEN = "BEN";

	// Are, art
	public static final String BER = "BER";

	// Is
	public static final String BEZ = "BEZ";

	// Coordinating conjunction (and, or)
	public static final String CC = "CC";

	/** Cardinal numeral (one, two, 2, etc.) */
	public static final String CD = "CD";

	// Subordinating conjunction (if, although)
	public static final String CS = "CS";

	// Do
	public static final String DO = "DO";

	// Did
	public static final String DOD = "DOD";

	// Does
	public static final String DOZ = "DOZ";

	// Singular determiner/quantifier (this, that)
	public static final String DT = "DT";


	// Singular determiner/quantifier (this, that)
	public static final String DT_THE = "DT-THE";
	// Singular or plural determiner/quantifier (some, any)
	public static final String DTI = "DTI";

	// Plural determiner (these, those)
	public static final String DTS = "DTS";

	// Determiner/double conjunction (either)
	public static final String DTX = "DTX";

	// Existential there
	public static final String EX = "EX";

	// Foreign word (hyphenated before regular tag)
	public static final String FW = "FW";

	// Foreign word (hyphenated before regular tag)
	public static final String FW_IN = "FW-IN";

	// Have
	public static final String HV = "HV";

	// Had (past tense)
	public static final String HVD = "HVD";

	// Having
	public static final String HVG = "HVG";

	// Had (past participle)
	public static final String HVN = "HVN";

	// Preposition
	public static final String IN = "IN";

	// Adjective
	public static final String JJ = "JJ";

	// Comparative adjective
	public static final String JJR = "JJR";

	// Semantically superlative adjective (chief, top)
	public static final String JJS = "JJS";

	// Morphologically superlative adjective (biggest)
	public static final String JJT = "JJT";

	// Modal auxiliary (can, should, will)
	public static final String MD = "MD";

	// Cited word (hyphenated after regular tag)
	public static final String NC = "NC";

	// Singular or mass noun
	public static final String NN = "NN";

	// Possessive singular noun
	public static final String NN$ = "NN$";

	// Plural noun
	public static final String NNS = "NNS";

	// Possessive plural noun
	public static final String NNS$ = "NNS$";

	// Proper noun or part of name phrase
	public static final String NP = "NP";


	// Proper noun or part of name phrase
	public static final String NNP = "NNP";


	// Possessive proper noun
	public static final String NP$ = "NP$";

	// Plural proper noun
	public static final String NPS = "NPS";

	// Possessive plural proper noun
	public static final String NPS$ = "NPS$";

	// Adverbial noun (home, today, west)
	public static final String NR = "NR";

	// Ordinal numeral (first, 2nd)
	public static final String OD = "OD";

	// Nominal pronoun (everybody, nothing)
	public static final String PN = "PN";

	// Possessive nominal pronoun
	public static final String PN$ = "PN$";

	// Possessive personal pronoun (my, our)
	public static final String PP$ = "PP$";

	// Second (nominal) possessive pronoun (mine, ours)
	public static final String PP$$ = "PP$$";

	// Singular reflexive/intensive personal pronoun (myself)
	public static final String PPL = "PPL";

	// Plural reflexive/intensive personal pronoun (ourselves)
	public static final String PPLS = "PPLS";

	public static final String PRP = "PRP";

	// Objective personal pronoun (me, him, it, them)
	public static final String PPO = "PPO";

	// 3rd. singular nominative pronoun (he, she, it, one)
	public static final String PPS = "PPS";

	// Other nominative personal pronoun (i, we, they, you)
	public static final String PPSS = "PPSS";

	// Qualifier (very, fairly)
	public static final String QL = "QL";

	// Post-qualifier (enough, indeed)
	public static final String QLP = "QLP";

	// Adverb
	public static final String RB = "RB";

	// Comparative adverb
	public static final String RBR = "RBR";

	// Conjunctive Adverbs
	public static final String RB_CONJ = "RB-CONJ";

	// Superlative adverb
	public static final String RBT = "RBT";

	// Superlative adverb
	public static final String RBS = "RBS";


	// Nominal adverb (here, then, indoors)
	public static final String RN = "RN";

	// Adverb/particle (about, off, up)
	public static final String RP = "RP";


	public static final String SYM = "SYM";

	// Infinitive marker to
	public static final String TO = "TO";

	// Interjection, exclamation
	public static final String UH = "UH";

	// Verb, base form
	public static final String VB = "VB";

	public static final String VBP = "VBP";
		;

	// Verb, past tense
	public static final String VBD = "VBD";

	// Verb, present participle/gerund
	public static final String VBG = "VBG";

	// Verb, past participle
	public static final String VBN = "VBN";

	// Verb, 3rd. singular present
	public static final String VBZ = "VBZ";

	// Wh- determiner (what, which)
	public static final String WDT = "WDT";

	// Possessive wh- pronoun (whose)
	public static final String WP$ = "WP$";


	// Objective wh- pronoun (whom, which, that)
	public static final String WPO = "WPO";

	// Nominative wh- pronoun (who, which, that)
	public static final String WPS = "WPS";


	// Wh- qualifier (how)
	public static final String WQL = "WQL";

	// Wh- adverb (how, where, when)
	public static final String WRB = "WRB";

	public static final String PDT = "PDT";
	

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
	
	/** contains one or more cardinal numbers and NN-VOL */
	public static final String VOLUME_Container = "VOLUME";
	
	/** contains one or more cardinal numbers and NN-MOLAR */
	public static final String MOLAR_Container = "MOLAR";
	
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
	
	/**Holds phrases/punctuation that together form the action phrase*/
	public static final String ACTIONPHRASE_Container = "ActionPhrase";

}

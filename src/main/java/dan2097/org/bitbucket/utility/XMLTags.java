package dan2097.org.bitbucket.utility;

/**
 * A subset of the tags allowed in a USPTO patent
 * @author dl387
 *
 */
public class XMLTags {
	
	/**
	 * A line break element
	 */
	public static final String BR = "br";
	
	/**
	 * A chemistry element, expected to contain a chemical img child
	 */
	public static final String CHEMISTRY = "chemistry";
	
	
	/**
	 * A description element. Hold the description of the invention the patent is for.
	 * Typically this is the bulk of the patent
	 */
	public static final String DESCRIPTION = "description";
	
	/**
	 * A definition list
	 */
	public static final String DL = "dl";
	
	/**
	 * A heading element
	 */
	public static final String HEADING = "heading";
	
	/**
	 * An ordered list
	 */
	public static final String OL = "ol";
	
	/**
	 * A paragraph element
	 */
	public static final String P = "p";
	
	/**
	 * Unknown function
	 */
	public static final String TABLE_EXTERNAL_DOC = "table-external-doc";
	
	/**
	 * Holds tables
	 */
	public static final String TABLES = "tables";
	
	/**
	 * An unordered list
	 */
	public static final String UL = "ul";
}

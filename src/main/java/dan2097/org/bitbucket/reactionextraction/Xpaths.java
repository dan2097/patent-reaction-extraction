package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

public class Xpaths {

	public static List<String> yieldXPaths;
	public static List<String> reactantXpaths;
	public static List<String> moleculesToIgnoreXpaths;
	public static List<String> referencesToPreviousReactions;
	static{
		yieldXPaths = new ArrayList<String>();
		reactantXpaths = new ArrayList<String>();
		moleculesToIgnoreXpaths = new ArrayList<String>();
		referencesToPreviousReactions = new ArrayList<String>();
		/*A nounphrase containing the returned molecule at the start of a synthesize phrase followed by something thing like "is/was synthesised"*/
		yieldXPaths.add(".//ActionPhrase[@type='Synthesize']/NounPhrase[following-sibling::*[1][local-name()='VerbPhrase'][VBD|VBZ][VB-SYNTHESIZE]]/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
		
		/*A nounphrase containing the returned molecule in a yield phrase*/
		yieldXPaths.add(".//ActionPhrase[@type='Yield']/NounPhrase/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
		
		
		
		
		
		/*synthesized by/from chemical*/
		reactantXpaths.add("../following-sibling::*[1]/VB-SYNTHESIZE/following-sibling::PrepPhrase/*[self::IN-BY or self::IN-FROM]/following-sibling::*[1]/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
	
	
		/*In a purify or filtration action phrase*/
		moleculesToIgnoreXpaths.add("./ancestor::ActionPhrase[@type='Purify' or @type='Filter']");
		/*Nested with a molecule e.g. a numerical reference*/
		moleculesToIgnoreXpaths.add("./ancestor::MOLECULE");
		/*Ignore silica, silica gel and the like*/
		moleculesToIgnoreXpaths.add(".[OSCAR-CM/text()='silica']");
		
		/*following/using conditions */
		referencesToPreviousReactions.add(".//VerbPhrase[VBG[text()='following'] or VB-USE][following-sibling::*[1]/*/text()='conditions']/following-sibling::*[2]//*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
	}
}

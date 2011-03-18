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
		yieldXPaths.add(".//ActionPhrase[@type='Yield']/descendant::*[name() = 'PrepPhrase' or name() = 'NounPhrase']/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
		//TODO needs to be more specific e.g. foo was yielded from
		
		
		/*synthesized by/from chemical*/
		reactantXpaths.add("../following-sibling::*[1]/VB-SYNTHESIZE/following-sibling::PrepPhrase/*[self::IN-BY or self::IN-FROM]/following-sibling::*[1]/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
	
	
		/*In a purify or filtration action phrase*/
		moleculesToIgnoreXpaths.add("./ancestor::ActionPhrase[@type='Purify' or @type='Filter' or @type='Dry' or @type='Precipitate' or @type='Wash' or @type='Extract']");
		/*Nested with a molecule e.g. a numerical reference*/
		moleculesToIgnoreXpaths.add("./ancestor::MOLECULE");
		/*Ignore silica, silica gel and the like*/
		moleculesToIgnoreXpaths.add(".[//OSCAR-CM[starts-with(.,'silica')]]");
		//method/procedure numbers
		moleculesToIgnoreXpaths.add(".[preceding-sibling::*[1][local-name()='NN-METHOD']]");//note that we can't restrict to unnamed molecules as things like "1A" are OSCAR-CMs
		
		/*following/using conditions */
		referencesToPreviousReactions.add(".//VerbPhrase[VBG[text()='following'] or VB-USE][following-sibling::*[1]/*/text()='conditions']/following-sibling::ActionPhrase[@type='Synthesize']//*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
	}
}

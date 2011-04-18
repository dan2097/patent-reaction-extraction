package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

public class Xpaths {

	public static List<String> yieldXPaths;
	public static List<String> reactantXpathsRel;
	public static List<String> reactantXpathsAbs;
	public static List<String> spectatorXpathsAbs;
	public static List<String> moleculesToIgnoreXpaths;
	public static List<String> referencesToPreviousReactions;
	static{
		yieldXPaths = new ArrayList<String>();
		/*A nounphrase containing the returned molecule at the start of a synthesize phrase followed by something thing like "is/was synthesised"*/
		yieldXPaths.add(".//ActionPhrase[@type='Synthesize']/NounPhrase[following-sibling::*[1][local-name()='VerbPhrase'][VBD|VBP|VBZ][VB-SYNTHESIZE]]/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
		
		/*A nounphrase containing the returned molecule in a yield phrase*/
		yieldXPaths.add(".//ActionPhrase[@type='Yield']/descendant::*[name() = 'PrepPhrase' or name() = 'NounPhrase']/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
		//TODO needs to be more specific e.g. foo was yielded from
	}
	static{
		reactantXpathsRel = new ArrayList<String>();
		reactantXpathsAbs = new ArrayList<String>();
		/*synthesized by/from chemical*/
		reactantXpathsRel.add("../following-sibling::*[1]/VB-SYNTHESIZE/following-sibling::PrepPhrase/*[self::IN-BY or self::IN-FROM]/following-sibling::*[1]/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
		/*Added*/
		reactantXpathsAbs.add(".//ActionPhrase[@type='Add']//descendant::*[name() = 'PrepPhrase' or name() = 'NounPhrase']//*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
		/*Is/were/are added to reactant (match what the chemical is being added to). Must all be within same sentence as per other xqueries*/
		reactantXpathsAbs.add(".//ActionPhrase[@type='Add']//descendant::*[name() = 'VBD' or name() = 'VBP' or name() = 'VBZ']/following-sibling::*[1][local-name()='VB-ADD']/following::TO/following::*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE][1]");
	}
	
	static{
		spectatorXpathsAbs = new ArrayList<String>();
		spectatorXpathsAbs.add(".//ActionPhrase[@type='Dissolve']//*[self::MOLECULE or self::MIXTURE]");
	}
	
	static{
		moleculesToIgnoreXpaths = new ArrayList<String>();
		/*In a purify or filtration action phrase*/
		moleculesToIgnoreXpaths.add("./ancestor::ActionPhrase[@type='Purify' or @type='Filter' or @type='Dry' or @type='Precipitate' or @type='Wash' or @type='Extract']");
		/*Nested with a molecule e.g. a numerical reference*/
		moleculesToIgnoreXpaths.add("./ancestor::MOLECULE");
		/*Ignore silica, silica gel and the like*/
		moleculesToIgnoreXpaths.add(".[//OSCAR-CM[starts-with(.,'silica')]]");
		//method/procedure numbers
		moleculesToIgnoreXpaths.add(".[preceding-sibling::*[1][local-name()='NN-METHOD']]");//note that we can't restrict to unnamed molecules as things like "1A" are OSCAR-CMs
	}

	
	static{
		referencesToPreviousReactions = new ArrayList<String>();
		/*following/using conditions */
		referencesToPreviousReactions.add(".//VerbPhrase[VBG[text()='following'] or VB-USE][following-sibling::*[1]/*/text()='conditions']/following-sibling::ActionPhrase[@type='Synthesize']//*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE]");
	
	}
}

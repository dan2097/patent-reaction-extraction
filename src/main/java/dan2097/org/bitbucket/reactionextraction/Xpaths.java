package dan2097.org.bitbucket.reactionextraction;

import java.util.ArrayList;
import java.util.List;

public class Xpaths {

	public static List<String> yieldXPaths;
	public static List<String> reactantXpaths;
	static{
		yieldXPaths = new ArrayList<String>();
		reactantXpaths = new ArrayList<String>();
		/*A nounphrase containing the returrned molecule at the start of a synthesize phrase followed by something thing like "is/was synthesised"*/
		yieldXPaths.add(".//ActionPhrase[@type='Synthesize']/NounPhrase[fn:position() = 1][following-sibling::*[1][local-name()='VerbPhrase'][VBD|VBZ][VB-SYNTHESIZE]]/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE ]");
		
		/*A nounphrase containing the returned molecule in a yield phrase*/
		yieldXPaths.add(".//ActionPhrase[@type='Yield']/NounPhrase/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE ]");
		
		
		
		
		
		/*synthesized by/from chemical*/
		reactantXpaths.add("../following-sibling::*[1]/VB-SYNTHESIZE/following-sibling::PrepPhrase/*[self::IN-BY or self::IN-FROM]/following-sibling::*[1]/*[self::MOLECULE or self::UNNAMEDMOLECULE or self::MIXTURE ]");
	}
}

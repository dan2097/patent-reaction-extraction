package dan2097.org.bitbucket.reactionextraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleTextAliasExtractor {
	
	//private final static Pattern matchIdentifier = Pattern.compile("((\\d+[a-z]?)|[\\(\\{\\[](\\d+[a-z]?|.*\\d+)[\\)\\}\\]])\\s*$");
	private final static Pattern matchAlphaNumericIdentifier = Pattern.compile("\\d+[a-z]?['`\u2032]*", Pattern.CASE_INSENSITIVE);//TODO normalise all to apostrophe?
	private final static Pattern matchTerminalAlphaNumericIdentifier = Pattern.compile("\\d+[a-z]?['`\u2032]*$", Pattern.CASE_INSENSITIVE);//TODO normalise all to apostrophe?
	private final static Pattern matchStereochemicalQualification = Pattern.compile("(endo|exo|syn|anti|erythro|threo|ent|cis|trans|all-trans|\\([rsez+-](,[rsez+-])*\\)||[rsez+-](,[rsez+-])*)-?$", Pattern.CASE_INSENSITIVE);
	private final static Pattern matchStereochemicalQualificationToIgnore = Pattern.compile("(\u00B1|\\(\u00B1\\))-?");
	private final static Pattern matchPreIdentifierWord = Pattern.compile("formula|intermediate|example|preparation|synthesis", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Attempts to find an identifier in the given heading text
	 * Returns null if one cannot be found
	 * @param text
	 * @return
	 */
	public static String findAlias(String text) {
		text = text.trim();
		if (text.length()==0){
			return null;
		}
		char finalChar = text.charAt(text.length()-1);
		if (finalChar == ',' || finalChar == '.' || finalChar == ';' || finalChar == ':' || finalChar == '-'){
			text = text.substring(0, text.length()-1);
			finalChar = text.charAt(text.length()-1);
		}
		if (text.length()==0){
			return null;
		}
		String identifier = matchEndingIndentifier(text);
		if (identifier!=null){
			return identifier;
		}
		
		identifier = matchEndingBrackettedIdentifier(text, finalChar);
		if (identifier!=null){
			return identifier;
		}
		
		identifier = matchStartingIdentifier(text);
		if (identifier!=null){
			return identifier;
		}
		
		return null;
	}

	private static String matchEndingIndentifier(String text) {
		Matcher m = matchTerminalAlphaNumericIdentifier.matcher(text);
		if (m.find()){
			Matcher qualifier = matchStereochemicalQualification.matcher(text.substring(0, m.start()));
			if (qualifier.find()){
				return qualifier.group() + m.group();
			}
			else{
				return m.group();
			}
		}
		return null;
	}

	private static String matchEndingBrackettedIdentifier(String text, char finalChar) {
		Integer openingBracket = null;
		if (finalChar == ')'){
			openingBracket = findMatchingOpeningBracketIndice(text, '(', ')');
		}
		else if (finalChar == ']'){
			openingBracket = findMatchingOpeningBracketIndice(text, '[', ']');
		}
		else if (finalChar == '}'){
			openingBracket = findMatchingOpeningBracketIndice(text, '{', '}');
		}
		if (openingBracket !=null) {
			return extractBrackettedIdentifier(text.substring(openingBracket +1, text.length()-1));
		}
		return null;
	}

	private static Integer findMatchingOpeningBracketIndice(String text, char openingBracket, char closingBracket) {
		int bracketLevel =0;
		for (int i = text.length() -1 ; i >=0; i--) {
			if (text.charAt(i) == openingBracket){
				bracketLevel--;
			}
			else if (text.charAt(i) == closingBracket){
				bracketLevel++;
			}
			
			if (bracketLevel==0){
				return i;
			}
		}
		return null;
	}

	private static String extractBrackettedIdentifier(String brackettedContents) {
		Matcher m = matchStereochemicalQualificationToIgnore.matcher(brackettedContents);
		if (m.lookingAt()){
			brackettedContents = brackettedContents.substring(m.end());
		}
		return brackettedContents;
	}

	private static String matchStartingIdentifier(String text) {
		Integer closingBracket = null;
		char firstChar = text.charAt(0);
		if (firstChar == '('){
			closingBracket = findMatchingClosingBracketIndice(text, '(', ')');
		}
		else if (firstChar == '['){
			closingBracket = findMatchingClosingBracketIndice(text, '[', ']');
		}
		else if (firstChar == '{'){
			closingBracket = findMatchingClosingBracketIndice(text, '{', '}');
		}
		if (closingBracket !=null) {
			return extractBrackettedIdentifier(text.substring(1, closingBracket));
		}
		
		Matcher m = matchPreIdentifierWord.matcher(text);
		if (m.lookingAt()){
			Matcher matchIdentifier = matchAlphaNumericIdentifier.matcher(text.substring(m.end()).trim());
			if (matchIdentifier.lookingAt()){
				return m.group() +" "+ matchIdentifier.group();
			}
		}
		
		
		String identifier = "";
		m = matchStereochemicalQualification.matcher(text);
		if (m.lookingAt()){
			identifier = m.group();
			text =text.substring(m.end());
		}
		m = matchAlphaNumericIdentifier.matcher(text);
		if (m.lookingAt()){
			return identifier + m.group();
		}
		
		return null;
	}
	
	private static Integer findMatchingClosingBracketIndice(String text, char openingBracket, char closingBracket) {
		int bracketLevel =0;
		for (int i = 0 ; i <text.length(); i++) {
			if (text.charAt(i) == openingBracket){
				bracketLevel++;
			}
			else if (text.charAt(i) == closingBracket){
				bracketLevel--;
			}
			
			if (bracketLevel==0){
				return i;
			}
		}
		return null;
	}
}

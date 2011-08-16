package dan2097.org.bitbucket.inchiTools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.opsin.StringTools;

public class InchiNormaliser {

	private final static Pattern matchSlash = Pattern.compile("/");
	
	/**
	 * Return a modified version of the given InChI where the:
	 * fixed hydrogen and reconnected layers have been removed
	 * The S indicating standard InChI is also removed
	 * @param inchi
	 * @return
	 */
	public static String normaliseInChI(String inchi){
		if (inchi == null){
			throw new IllegalArgumentException("Input InChI was the null string");
		}
		String[] inchiLayers = matchSlash.split(inchi);
		if (inchiLayers.length < 2){
			throw new IllegalArgumentException("Invalid InChI");
		}
		List<String> retainedLayers = new ArrayList<String>();
		if (Character.isLetter(inchiLayers[0].charAt(inchiLayers[0].length() -1))){//remove the S indicating this to be a standard InChI
			inchiLayers[0]=inchiLayers[0].substring(0, inchiLayers[0].length() -1);
		}
		retainedLayers.add(inchiLayers[0]);//version identifier
		retainedLayers.add(inchiLayers[1]);//molecular formula

		for (int i = 2; i < inchiLayers.length; i++) {
			Character c = inchiLayers[i].charAt(0);
			if (c=='c' || c=='h' || c=='q' || c=='p' || c=='b' || c=='t' || c=='m' || c=='s' || c=='i'){
				retainedLayers.add(inchiLayers[i]);
			}
			else{
				break;
			}
		}
		return StringTools.stringListToString(retainedLayers, "/");
	}
}

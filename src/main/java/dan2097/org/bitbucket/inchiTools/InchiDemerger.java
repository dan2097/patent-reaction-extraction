package dan2097.org.bitbucket.inchiTools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.opsin.StringTools;

public class InchiDemerger {

	private static final Pattern matchSlash = Pattern.compile("/");
	private static final Pattern matchDot = Pattern.compile("[.]");
	private static final Pattern matchSemiColon = Pattern.compile("[;]");
	private static final Pattern matchMultiplierStar = Pattern.compile("([2-9][0-9]?)\\*");
	private static final Pattern matchMultiplier = Pattern.compile("([2-9][0-9]?)");
	private final String inchi;
	
	/**
	 * Input the inchi you would like to partition into inchis for each component
	 * e.g. InChI=1/C8H18O.H2O/c1-2-3-4-5-6-7-8-9;/h9H,2-8H2,1H3;1H2
	 * --> InChI=1/C8H18O/c1-2-3-4-5-6-7-8-9/h9H,2-8H2,1H3 and InChI=1/H2O/h1H2
	 * @param inchi
	 */
	public InchiDemerger(String inchi) {
		if (inchi==null){
			throw new IllegalArgumentException("Input inchi was null");
		}
		this.inchi = inchi;
	}
	
	/**
	 * Uses a subset of the  rules of the InChI technical manual Appendix 3 to split a standard InChI (or similar) into component InChIs
	 * @return
	 */
	public List<String> generateDemergedInchis(){
		List<List<String>> demergedInChIArrays = new ArrayList<List<String>>();
		String[] inchiLayers = matchSlash.split(inchi);
		if (inchiLayers.length < 2){
			throw new IllegalArgumentException("Invalid InChI");
		}
		for (int i = 1; i < inchiLayers.length; i++) {
			String layer = inchiLayers[i];
			String layerSansIdentifier = (i==1 ? layer : layer.substring(1));
			String layerPrefix = (i==1 ? "" :layer.substring(0, 1));
			String[] componentsOfLayer = (i==1 ? matchDot.split(layerSansIdentifier) : matchSemiColon.split(layerSansIdentifier));
			int componentCounter = 0;
			for (String componentOfLayer : componentsOfLayer) {
				Matcher m;
				if (i==1){
					m = matchMultiplier.matcher(componentOfLayer);
				}
				else{
					m = matchMultiplierStar.matcher(componentOfLayer);
				}
				if (demergedInChIArrays.size()==0){
					List<String> contents =new ArrayList<String>();
					contents.add(inchiLayers[0]);
					demergedInChIArrays.add(contents);
				}
				int multiplier = 1;
				String sectionOfLayerToMultiply;
				if (m.lookingAt()){
					sectionOfLayerToMultiply = componentOfLayer.substring(m.end(0));
					multiplier = Integer.parseInt(m.group(1));
				}
				else{
					sectionOfLayerToMultiply = componentOfLayer;
				}

				for (int j = 0; j < multiplier; j++) {
					if (componentCounter == demergedInChIArrays.size()){
						List<String> contents =new ArrayList<String>();
						contents.add(inchiLayers[0]);
						demergedInChIArrays.add(contents);
					}
					demergedInChIArrays.get(componentCounter++).add(layerPrefix + sectionOfLayerToMultiply);
				}
			}
		}
		List<String> demergedInChIs = new ArrayList<String>();
		for (List<String> demergedInChIArray : demergedInChIArrays) {
			demergedInChIs.add(StringTools.stringListToString(demergedInChIArray, "/"));
		}
		return demergedInChIs;
	}
}

package dan2097.org.bitbucket.utility;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bitbucket.dan2097.structureExtractor.DocumentToStructures;
import org.bitbucket.dan2097.structureExtractor.IdentifiedChemicalName;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistryPOSTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistrySentenceParser;
import uk.ac.cam.ch.wwmm.chemicaltagger.POSContainer;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.opsin.OpsinDictionary;

public class Utils {
	
	private static ChemNameDictRegistry chemNameRegistery;
	static{
		chemNameRegistery = ChemNameDictRegistry.getInstance();
		chemNameRegistery.register(new OpsinDictionary());
	}
	/**
	 * Tags a string with parts of speech using chemical tagger. Where known the annotations will be more specific than those used for the Brown corpus
	 * @param text
	 * @return
	 */
	public static String tagString(String text) {
		POSContainer posContainer = ChemistryPOSTagger.getInstance().runTaggers(text);
		return posContainer.getTokenTagTupleAsString();
	}
	
	/**
	 * Given a tagged string returns the sentence as a hierarchy grouped by identified phrases of chemical significance
	 * @param tagged
	 * @return
	 */
	public static Document runChemicalSentenceParsingOnTaggedString(String tagged) {
		InputStream in;
		try {
			in = new ByteArrayInputStream(tagged.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Java VM is broken, UTF-8 should be supported!",e);
		}
		ChemistrySentenceParser chemistrySentenceParser = new ChemistrySentenceParser(in);
		chemistrySentenceParser.parseTags();
		return chemistrySentenceParser.getDocument();
	}

	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to SMILES
	 * @param name
	 * @return
	 */
	public static String resolveNameToSmiles(String name) {
		return chemNameRegistery.getShortestSMILES(name);
	}
	
	/**
	 * Uses OSCAR4's dictionaries/OPSIN to convert a name to InChI
	 * @param name
	 * @return
	 */
	public static String resolveNameToInchi(String name) {
		Set<String> inchis = chemNameRegistery.getInChI(name);
		if (!inchis.isEmpty()){
			return inchis.iterator().next();
		}
		return null;
	}
	
	public static List<String> getSystematicChemicalNamesFromText(String text) {
		try{
			List<IdentifiedChemicalName> identifiedNames = DocumentToStructures.extractNames(text);
			List<String> names = new ArrayList<String>();
			for (IdentifiedChemicalName identifiedChemicalName : identifiedNames) {
				names.add(identifiedChemicalName.getValue());
			}
			return names;
		}
		catch (Exception e) {
			return Collections.emptyList();
		}
	}
}

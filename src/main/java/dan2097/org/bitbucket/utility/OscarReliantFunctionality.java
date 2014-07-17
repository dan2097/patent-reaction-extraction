package dan2097.org.bitbucket.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistryPOSTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.OpenNLPTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.OscarTokeniser;
import uk.ac.cam.ch.wwmm.chemicaltagger.RegexTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.Tagger;
import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ISMILESProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IStdInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.DefaultDictionary;
import uk.ac.cam.ch.wwmm.oscar.opsin.OpsinDictionary;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import dan2097.org.bitbucket.chemicaltagging.CustomisedOscarTagger;
import dan2097.org.bitbucket.chemicaltagging.OpsinTagger;
import dan2097.org.bitbucket.chemicaltagging.TrivialChemicalNameTagger;

public class OscarReliantFunctionality {
	private final List<IChemNameDict> dictionaries;
	private final ChemistryPOSTagger posTagger;
	
	private OscarReliantFunctionality() {
		Oscar oscar = new Oscar();
		dictionaries = new ArrayList<IChemNameDict>();
		dictionaries.add(new OpsinDictionary());
		dictionaries.add(new DefaultDictionary());
		dictionaries.add(ChEBIDictionary.getInstance());
		ChemNameDictRegistry chemNameRegistry = new ChemNameDictRegistry(Locale.ENGLISH);
		for (IChemNameDict dict : dictionaries) {
			chemNameRegistry.register(dict);
		}
		oscar.setDictionaryRegistry(chemNameRegistry);
		MEMMRecogniser recogniser = new MEMMRecogniser();
		recogniser.setDeprioritiseOnts(true);
		recogniser.setCprPseudoConfidence(0);
		recogniser.setOntPseudoConfidence(0);
		oscar.setRecogniser(recogniser);
		List<Tagger> taggersOrderedInDescendingPriority = new ArrayList<Tagger>();
		taggersOrderedInDescendingPriority.add(new TrivialChemicalNameTagger());
		taggersOrderedInDescendingPriority.add(new OpsinTagger());
		taggersOrderedInDescendingPriority.add(new RegexTagger());
		taggersOrderedInDescendingPriority.add(new CustomisedOscarTagger(oscar));
		taggersOrderedInDescendingPriority.add(OpenNLPTagger.getInstance());
		posTagger = new ChemistryPOSTagger(new OscarTokeniser(), taggersOrderedInDescendingPriority);
	}
	 
	private static class SingletonHolder {
		public static final OscarReliantFunctionality INSTANCE = new OscarReliantFunctionality();
	}
	 
	public static OscarReliantFunctionality getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public ChemistryPOSTagger getPosTagger() {
		return posTagger;
	}
	
	public String resolveNameToSmiles(String name) {
		for (IChemNameDict dict : dictionaries) {
			if (dict instanceof ISMILESProvider) {
				String smiles = (((ISMILESProvider)dict).getShortestSmiles(name));
				if (smiles != null){
					return smiles;
				}
			}
		}
		return null;
	}
	
	public String resolveNameToStdInchi(String name) {
		for (IChemNameDict dict : dictionaries) {
			if (dict instanceof IStdInChIProvider) {
				Set<String> inchis = (((IStdInChIProvider)dict).getStdInchis(name));
				if (!inchis.isEmpty()){
					String inchi = inchis.iterator().next();
					return inchi;
				}
			}
		}
		return null;
	}
}

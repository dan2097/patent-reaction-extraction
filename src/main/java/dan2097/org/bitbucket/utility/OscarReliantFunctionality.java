package dan2097.org.bitbucket.utility;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistryPOSTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.OpenNLPTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.OscarTokeniser;
import uk.ac.cam.ch.wwmm.chemicaltagger.RegexTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.Tagger;
import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.opsin.OpsinDictionary;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import dan2097.org.bitbucket.chemicaltagging.CustomisedOscarTagger;
import dan2097.org.bitbucket.chemicaltagging.OpsinTagger;

public class OscarReliantFunctionality {
		private ChemNameDictRegistry chemNameRegistry;
		private ChemistryPOSTagger posTagger;
	
		private OscarReliantFunctionality() {
			Oscar oscar = new Oscar();
			chemNameRegistry = new ChemNameDictRegistry();
			chemNameRegistry.register(new OpsinDictionary());
			oscar.setDictionaryRegistry(chemNameRegistry);
			MEMMRecogniser recogniser = new MEMMRecogniser();
			recogniser.setDeprioritiseOnts(true);
			recogniser.setCprPseudoConfidence(0);
			recogniser.setOntPseudoConfidence(0);
			oscar.setRecogniser(recogniser);
			List<Tagger> taggersOrderedInDescendingPriority = new ArrayList<Tagger>();
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
		
		public ChemNameDictRegistry getChemNameDictRegistry() {
			return chemNameRegistry;
		}
}

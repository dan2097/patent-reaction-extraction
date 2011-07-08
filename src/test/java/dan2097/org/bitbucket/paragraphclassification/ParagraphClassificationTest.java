package dan2097.org.bitbucket.paragraphclassification;

import static junit.framework.Assert.*;

import net.sf.classifier4J.ClassifierException;
import net.sf.classifier4J.bayesian.WordsDataSourceException;

import org.junit.Test;

public class ParagraphClassificationTest {

	@Test
	public void nonExperimentalParagraphTest() throws WordsDataSourceException, ClassifierException {
		ParagraphClassifier classifier = new ParagraphClassifier();
		boolean isExperimental = classifier.isExperimental("It has now been found that, in order to accelerate replacement of halide by amine on the bispyridyl-pyrimidine structure, it is also possible to use catalytic amounts of non-transition metal salts, such as, for example, zinc(II) salts, which substantially simplifies the reaction procedure and working-up. ");
		assertEquals(false, isExperimental);
	}
	
	@Test
	public void experimentalParagraphTest() throws WordsDataSourceException, ClassifierException {
		ParagraphClassifier classifier = new ParagraphClassifier();
		boolean isExperimental = classifier.isExperimental("10.0 ml of (0.130 mol) of N,N-dimethylformamide are added dropwise at 40° C., with stirring, to 295 ml (4.06 mol) of thionyl chloride. Then, in the course of half an hour, 100 g (0.812 mol) of picolinic acid are added. The mixture is cautiously heated to 70° C. and stirred at that temperature for 24 hours, the gases formed being conveyed away through a wash bottle charged with sodium hydroxide solution. Concentration, and coevaporation a further three times with 100 ml of toluene each time, are carried out; the product is diluted with that solvent to 440 ml, and the solution is introduced into a mixture of 120 ml of absolute ethanol and 120 ml of toluene. The mixture is concentrated to approximately half its volume, cooled to 4° C., filtered off under suction and washed with toluene. 4-Chloropyridine-2-carboxylic acid ethyl ester hydrochloride is obtained in the form of a beige hygroscopic powder.");
		assertEquals(true, isExperimental);
	}
}

package dan2097.org.bitbucket.paragraphclassification;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.classifier4J.ClassifierException;
import net.sf.classifier4J.bayesian.BayesianClassifier;
import net.sf.classifier4J.bayesian.WordsDataSourceException;

import org.apache.commons.io.IOUtils;

public class ParagraphClassifier {

	private final BayesianClassifier bayesianClassifier = new BayesianClassifier(); 

	public ParagraphClassifier(){
		try {
			List<String> experimentalParas;
			InputStream experimentalIs = ParagraphClassifier.class.getResourceAsStream("experimental.txt");
			try {
				experimentalParas = IOUtils.readLines(experimentalIs, "UTF-8");
			}
			finally {
				IOUtils.closeQuietly(experimentalIs);
			}
			
			List<String> nonExperimentalParas;
			InputStream nonExperimentalIs = ParagraphClassifier.class.getResourceAsStream("non-experimental.txt");
			try {
				nonExperimentalParas = IOUtils.readLines(nonExperimentalIs, "UTF-8");
			}
			finally {
				IOUtils.closeQuietly(nonExperimentalIs);
			}
			trainClassifier(experimentalParas, nonExperimentalParas);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void trainClassifier(List<String> experimentalParas, List<String> nonExperimentalParas){
		try{
			for (String expText : experimentalParas) {
				bayesianClassifier.teachMatch(expText);
			}
			for (String expText : nonExperimentalParas) {
				bayesianClassifier.teachNonMatch(expText);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Paragraph classifier training failed", e);
		}
	}

	/**
	 * Returns the probability the bayesian classifier gives of the string being experimental 
	 * @param string
	 * @return
	 * @throws WordsDataSourceException
	 * @throws ClassifierException
	 */
	public double classify(String string) throws WordsDataSourceException, ClassifierException {
		return bayesianClassifier.classify(string);
	}
	
	/**
	 * Uses a confidence of 0.5 to determine between experimental and non-experimental
	 * Returns null if the classifier throws an exception.
	 * @param string
	 * @return
	 */
	public Boolean isExperimental(String string){
		try {
			double probability = classify(string);
			if (probability >= 0.5){
				return true;
			}
			else{
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

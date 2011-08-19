package dan2097.org.bitbucket.paragraphclassification;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.classifier4J.ClassifierException;
import net.sf.classifier4J.bayesian.BayesianClassifier;
import net.sf.classifier4J.bayesian.WordsDataSourceException;
import nu.xom.Builder;
import nu.xom.Document;

public class ParagraphClassifier {

	private static final URL EXPERIMENTAL_URL = ClassLoader.getSystemResource("dan2097/org/bitbucket/paragraphclassification/experimental");
	private static final URL NON_EXPERIMENTAL_URL = ClassLoader.getSystemResource("dan2097/org/bitbucket/paragraphclassification/non-experimental");
	private static Pattern matchWhiteSpace = Pattern.compile("\\s+");
	
	private static class xmlFileFilter implements FileFilter {
		public boolean accept(File f) {
			if (f.getName().endsWith(".xml")) {
				return true;
			}
			return false;
		}
	}
	
	private BayesianClassifier bayesianClassifier = new BayesianClassifier(); 
	
	public ParagraphClassifier(){
		File[] expFiles = getXMLFiles(EXPERIMENTAL_URL);
		File[] nonExpFiles = getXMLFiles(NON_EXPERIMENTAL_URL);
		if (expFiles.length != nonExpFiles.length) {
			throw new RuntimeException("need same number of experimental and non experimental paragraphs");
		}
		List<String> experimentalParas = readData(expFiles);
		List<String> nonExperimentalParas = readData(nonExpFiles);
		trainClassifier(experimentalParas, nonExperimentalParas);
	}

	private File[] getXMLFiles(URL url){
		File directory;
		try {
			directory = new File(url.toURI());
		} catch (Exception e) {
			throw new RuntimeException("Unable to read paragraph classifier training data", e);
		}
		File [] files = directory.listFiles(new xmlFileFilter());
		return files;
	}

	private List<String> readData(File[] files){
		Builder builder = new Builder();
		List<String> data = new ArrayList<String>();
		for (File file : files) {
			Document doc;
			try {
				doc = builder.build(file);
			} catch (Exception e) {
				throw new RuntimeException("Unable to read paragraph classifier training data", e);
			}
			data.add(matchWhiteSpace.matcher(doc.getValue()).replaceAll(" "));
		}
		return data;
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

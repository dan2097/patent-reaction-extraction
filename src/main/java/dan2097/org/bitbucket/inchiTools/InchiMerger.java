package dan2097.org.bitbucket.inchiTools;

import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jniinchi.INCHI_RET;
import net.sf.jniinchi.JniInchiInput;
import net.sf.jniinchi.JniInchiInputInchi;
import net.sf.jniinchi.JniInchiOutput;
import net.sf.jniinchi.JniInchiOutputStructure;
import net.sf.jniinchi.JniInchiWrapper;

public class InchiMerger {
	private static Logger LOG = Logger.getLogger(InchiMerger.class);
	private final List<String> inchis;
	
	/**
	 * Input the inchis you would like merged
	 * @param inchis
	 */
	public InchiMerger(List<String> inchis) {
		if (inchis==null || inchis.size() <2){
			throw new IllegalArgumentException("A list of at least two InChIs was expected");
		}
		this.inchis = inchis;
	}
	
	/**
	 * Uses JNI-InChI to load the given InChIs and merge them into a new InChI
	 * Returns null if merging failed
	 */
	String generateMergedInchi(){
		try {
			JniInchiInput input = new JniInchiInput();
			for (String inchi : inchis) {
				JniInchiInputInchi inchiInput = new JniInchiInputInchi(inchi);
				JniInchiOutputStructure struct = JniInchiWrapper.getStructureFromInchi(inchiInput);
				for (int i = 0; i < struct.getNumAtoms(); i++){
					input.addAtom(struct.getAtom(i));
				}
				for (int i = 0; i < struct.getNumBonds(); i++){
					input.addBond(struct.getBond(i));
				}
				for (int i = 0; i < struct.getNumStereo0D(); i++){
					input.addStereo0D(struct.getStereo0D(i));
				}
			}
			JniInchiOutput output = JniInchiWrapper.getInchi(input);
	    	INCHI_RET ret = output.getReturnStatus();
	    	if (!INCHI_RET.OKAY.equals(ret) && !INCHI_RET.WARNING.equals(ret)){
	    		LOG.debug("InChI merging failed. Status codewas: " +ret);
	    		return null;
	    	}
			return output.getInchi();
		}
		catch (Exception e) {
			LOG.debug("InChI merging failed", e);
			return null;
		}
	}
	
	/**
	 * Uses JNI-InChI to load the given InChIs and merge them into a new InChI
	 * Then runs the inchi normaliser
	 * Returns null if merging failed
	 */
	public String generateMergedNormalisedInchi(){
		String generateMergedInchi = generateMergedInchi();
		if (generateMergedInchi !=null){
			generateMergedInchi = InchiNormaliser.normaliseInChI(generateMergedInchi);
		}
		return generateMergedInchi;
	}
}

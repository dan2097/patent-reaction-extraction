package dan2097.org.bitbucket.reactionextraction;

/**
 * An immutable SMILES/InChI pair.
 * SMILES can never be null, InChI could be in chemicals such as polymers
 * @author dl387
 *
 */
public class ChemicalIdentifierPair {

	private final String smiles;
	private final String inchi;
	
	/**
	 * Constructs an immutable SMILES/InChI pair.
	 * @param smiles
	 * @param inchi
	 */
	public ChemicalIdentifierPair(String smiles, String inchi) {
		this.smiles = smiles;
		this.inchi = inchi;
	}

	/**
	 * Never null
	 * @return
	 */
	public String getSmiles() {
		return smiles;
	}

	/**
	 * Null for cases such as polymers
	 * @return
	 */
	public String getInchi() {
		return inchi;
	}
}

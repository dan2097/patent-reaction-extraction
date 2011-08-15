package dan2097.org.bitbucket.reactionextraction;

/**
 * A holder for a chemicalName and alias
 * @author dl387
 *
 */
public class ChemicalNameAliasPair {

	private final String chemicalName;
	private final String alias;
	
	/**
	 * A chemical name and an identifier used to refer to said chemical.
	 * The alias may be null;
	 * @param chemicalName
	 * @param alias
	 */
	public ChemicalNameAliasPair(String chemicalName, String alias) {
		this.chemicalName = chemicalName;
		this.alias = alias;
	}
	
	String getChemicalName() {
		return chemicalName;
	}

	String getAlias() {
		return alias;
	}
	
	public String toString(){
		return chemicalName +"\t" + alias;
	}
}

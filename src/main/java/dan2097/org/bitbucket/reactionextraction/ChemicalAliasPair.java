package dan2097.org.bitbucket.reactionextraction;

/**
 * A holder for a chemical and alias
 * @author dl387
 *
 */
public class ChemicalAliasPair {

	private final Chemical chemical;
	private String alias;

	/**
	 * A chemical and an identifier used to refer to said chemical.
	 * The alias may be null;
	 * @param chemical
	 * @param alias
	 */
	public ChemicalAliasPair(Chemical chemical, String alias) {
		this.chemical = chemical;
		this.alias = alias;
	}
	
	Chemical getChemical() {
		return chemical;
	}

	String getAlias() {
		return alias;
	}
	
	void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String toString(){
		return chemical.getName() +"\t" + alias;
	}
}

package dan2097.org.bitbucket.reactionextraction;

import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Element;

public class Chemical{

	private final String name;
	private String smiles;
	private String smarts;
	private String inchi;
	private String massValue;
	private String massUnits;
	private String amountValue;
	private String amountUnits;
	private String molarityValue;
	private String molarityUnits;
	private String volumeValue;
	private String volumeUnits;
	private Double equivalents;
	private String equivalentsUnits;
	private Double pH;
	private Double percentYield;
	private Double stoichiometry;
	private String state;
	private ChemicalRole role = null;
	private ChemicalType type = null;

	private final static Pattern matchCentiLitresOrLarger = Pattern.compile("dm3|(centi|deci|kilo|mega)?lit(er|re)[s]?", Pattern.CASE_INSENSITIVE);


	public Chemical(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * A highly interoperable structural identifier. Null if structure is unavailable
	 * @return
	 */
	public String getSmiles() {
		return smiles;
	}
	
	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}
	
	/**
	 * A description of molecular functionality. Mutually exclusive with the presence of smiles. Null if unavailable
	 * @return
	 */
	public String getSmarts() {
		return smarts;
	}
	
	public void setSmarts(String smarts) {
		this.smarts = smarts;
	}

	/**
	 * A canonical identifier for this chemical. Null if structure is unavailable
	 * @return
	 */
	public String getInchi() {
		return inchi;
	}
	
	public void setInchi(String inchi) {
		this.inchi = inchi;
	}
	
	/**
	 * Gets a value for the mass. Meaningless without reference to mass units
	 * Typically, but not always, this can be expressed as a float
	 * (or null if unavailable)
	 * @return
	 */
	public String getMassValue() {
		return massValue;
	}

	void setMassValue(String massValue) {
		this.massValue = massValue;
	}

	/**
	 * Gets a textual string for the units used to describe the mass
	 * (or null if unavailable)
	 * @return
	 */
	public String getMassUnits() {
		return massUnits;
	}

	void setMassUnits(String massUnits) {
		this.massUnits = massUnits;
	}
	
	/**
	 * Gets a value for the amount. Meaningless without reference to amount units
	 * Typically, but not always, this can be expressed as a float
	 * (or null if unavailable)
	 * @return
	 */
	public String getAmountValue() {
		return amountValue;
	}

	void setAmountValue(String amountValue) {
		this.amountValue = amountValue;
	}

	/**
	 * Gets a textual string for the units used to describe the amount
	 * (or null if unavailable)
	 * @return
	 */
	public String getAmountUnits() {
		return amountUnits;
	}

	void setAmountUnits(String amountUnits) {
		this.amountUnits = amountUnits;
	}
	
	/**
	 * Gets a value for the concentration. Assumed to be in moles
	 * Typically, but not always, this can be expressed as a float
	 * (or null if unavailable)
	 * @return
	 */
	public String getMolarity() {
		return molarityValue;
	}

	void setMolarityValue(String molarityValue) {
		this.molarityValue = molarityValue;
	}
	
	/**
	 * Gets a textual string for the units used to describe the molarity
	 * (or null if unavailable)
	 * @return
	 */
	public String getMolarityUnits() {
		return molarityUnits;
	}

	void setMolarityUnits(String molarityUnits) {
		this.molarityUnits = molarityUnits;
	}

	/**
	 * Gets a value for the volume. Meaningless without reference to volume units
	 * Typically, but not always, this can be expressed as a float
	 * (or null if unavailable)
	 * @return
	 */
	public String getVolumeValue() {
		return volumeValue;
	}

	void setVolumeValue(String volumeValue) {
		this.volumeValue = volumeValue;
	}

	/**
	 * Gets a textual string for the units used to describe the volume
	 * (or null if unavailable)
	 * @return
	 */
	public String getVolumeUnits() {
		return volumeUnits;
	}

	void setVolumeUnits(String volumeUnits) {
		this.volumeUnits = volumeUnits;
	}
	
	public Double getEquivalents() {
		return equivalents;
	}

	void setEquivalents(Double equivalents) {
		this.equivalents = equivalents;
	}

	/**
	 * Gets a textual string for the units used to describe the number of equivalents of the chemical
	 * (or null if unavailable)
	 * @return
	 */
	public String getEquivalentsUnits() {
		return equivalentsUnits;
	}

	void setEquivalentsUnits(String equivalentsUnits) {
		this.equivalentsUnits = equivalentsUnits;
	}

	public Double getpH() {
		return pH;
	}

	void setpH(Double pH) {
		this.pH = pH;
	}

	/**
	 * Gets the percent yield as a double
	 * (or null if unavailable)
	 * @return
	 */
	public Double getPercentYield() {
		return percentYield;
	}

	void setPercentYield(Double percentYield) {
		this.percentYield = percentYield;
	}
	
	/**
	 * Returns the stoichiometry of this chemical.
	 * 1 by default
	 * @return
	 */
	public Double getStoichiometry() {
		return stoichiometry;
	}

	void setStoichiometry(Double stoichiometry) {
		this.stoichiometry = stoichiometry;
	}
	
	/**
	 * Gets the state claimed for this substance in the text
	 * e.g. liquid, gas, crystal, foam etc.
	 * @return
	 */
	public String getState() {
		return state;
	}

	void setState(String state) {
		this.state = state;
	}

	/**
	 * Gets the type assigned to a chemical or null
	 * A type indicates whether the name describes an exact compound, fragment, class etc.
	 * @return
	 */
	public ChemicalType getType() {
		return type;
	}

	void setType(ChemicalType type) {
		this.type = type;
	}
	
	/**
	 * Gets the role assigned to a chemical or null
	 * A role is one of product, reactant or spectator
	 * @return
	 */
	public ChemicalRole getRole() {
		return role;
	}

	void setRole(ChemicalRole role) {
		this.role = role;
	}

	boolean hasImpreciseVolume() {
		if (volumeValue==null || volumeUnits ==null){
			return true;
		}
		if (matchCentiLitresOrLarger.matcher(volumeUnits).matches()){
			return true;
		}
		if (volumeValue.contains(".")){
			return false;
		}
		if (volumeValue.contains("0")){
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the chemical has been associated with any quantities or a yield e.g. amount, volume, weight etc.
	 * @return
	 */
	boolean hasAQuantity() {
		return (massValue !=null || massUnits !=null || amountValue !=null || amountUnits !=null || molarityValue != null || molarityUnits !=null ||
				volumeValue !=null || volumeUnits !=null || equivalents !=null || equivalentsUnits !=null || pH !=null || percentYield !=null);
	}

	public Element toCML(String id){
		Element reactant = new Element("reactant");
		Element molecule = new Element("molecule");
		reactant.appendChild(molecule);
		Element nameEl = new Element("name");
		nameEl.appendChild(name);
		nameEl.addAttribute(new Attribute("dictRef","nameDict:unknown"));
		molecule.appendChild(nameEl);
		molecule.addAttribute(new Attribute("id", id));
		
		if (amountValue!=null){
			Element amount = new Element("amount");
			amount.appendChild(String.valueOf(amountValue));
			if (amountUnits!=null){
				amount.addAttribute(new Attribute("units", amountUnits));
			}
			reactant.appendChild(amount);
		}
		if (molarityValue!=null){
			Element amount = new Element("amount");
			amount.appendChild(String.valueOf(molarityValue));
			if (molarityUnits!=null){
				amount.addAttribute(new Attribute("units", molarityUnits));
			}
			reactant.appendChild(amount);
		}
		if (volumeValue!=null){
			Element amount = new Element("amount");
			amount.appendChild(String.valueOf(volumeValue));
			if (volumeUnits!=null){
				amount.addAttribute(new Attribute("units", volumeUnits));
			}
			reactant.appendChild(amount);
		}
		if (massValue!=null){
			Element amount = new Element("amount");
			amount.appendChild(String.valueOf(massValue));
			if (massUnits!=null){
				amount.addAttribute(new Attribute("units", massUnits));
			}
			reactant.appendChild(amount);
		}
		if (stoichiometry !=null){
			reactant.addAttribute(new Attribute("count", String.valueOf(stoichiometry)));
		}

		if (equivalents!=null){
			Element amount = new Element("amount");
			amount.appendChild(String.valueOf(equivalents));
			amount.addAttribute(new Attribute("units", equivalentsUnits));
			reactant.appendChild(amount);
		}
		
		if (pH!=null){
			Element amount = new Element("amount");
			amount.appendChild(String.valueOf(pH));
			amount.addAttribute(new Attribute("units", "pH"));
			reactant.appendChild(amount);
		}
		
		if (percentYield!=null){
			Element amount = new Element("amount");
			amount.appendChild(String.valueOf(percentYield));
			amount.addAttribute(new Attribute("units", "percent yield"));
			reactant.appendChild(amount);
		}
		
		if (smiles!=null){
			Element identifier = new Element("identifier");
			identifier.appendChild(smiles);
			identifier.addAttribute(new Attribute("title", "SMILES"));
			reactant.appendChild(identifier);
		}
		
		if (inchi!=null){
			Element identifier = new Element("identifier");
			identifier.appendChild(inchi);
			identifier.addAttribute(new Attribute("title", "InChI"));
			reactant.appendChild(identifier);
		}
		
		//not CMLreact
		if (role != null){
			Element roleEl = new Element("role");
			roleEl.appendChild(role.toString());
			reactant.appendChild(roleEl);
		}
		if (type != null){
			Element typeEl = new Element("type");
			typeEl.appendChild(type.toString());
			reactant.appendChild(typeEl);
		}
		if (state != null){
			Element stateEl = new Element("state");
			stateEl.appendChild(state);
			reactant.appendChild(stateEl);
		}
		return reactant;
	}
}

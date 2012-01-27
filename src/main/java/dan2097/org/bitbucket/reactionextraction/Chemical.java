package dan2097.org.bitbucket.reactionextraction;

import java.util.List;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Element;

public class Chemical{
	private static String CML_NAMESPACE = "http://www.xml-cml.org/schema";
	private static String DL_NAMESPACE = "http://bitbucket.org/dan2097";
	
	private final String name;
	private ChemicalIdentifierPair chemicalIdentifierPair = new ChemicalIdentifierPair(null, null);
	private String smarts;
	private List<String> inchiComponents;
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
	private Integer stoichiometry;
	private String state;
	private ChemicalRole role = null;
	private ChemicalEntityType entityType = null;

	private final static Pattern matchCentiLitresOrLarger = Pattern.compile("dm3|(centi|deci|kilo|mega)?lit(er|re)[s]?", Pattern.CASE_INSENSITIVE);


	public Chemical(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Returns the chemicalIdentifierPair. This is never null but does not necessarily contain any identifiers
	 * @return
	 */
	public ChemicalIdentifierPair getChemicalIdentifierPair() {
		return chemicalIdentifierPair;
	}

	public void setChemicalIdentifierPair(ChemicalIdentifierPair chemicalIdentifierPair) {
		if (chemicalIdentifierPair == null){
			throw new IllegalArgumentException("chemicalIdentifierPair was null");
		}
		this.chemicalIdentifierPair = chemicalIdentifierPair;
	}
	
	public String getSmiles(){
		return chemicalIdentifierPair.getSmiles();
	}
	
	public String getInchi(){
		return chemicalIdentifierPair.getInchi();
	}
	
	public boolean hasSmiles(){
		return chemicalIdentifierPair.getSmiles() != null;
	}
	
	public boolean hasInchi(){
		return chemicalIdentifierPair.getInchi() != null;
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
	 * Used internally to assist in handling delimited mixtures e.g. octanol/water
	 * @return
	 */
	List<String> getInchiComponents() {
		return inchiComponents;
	}

	void setInchiComponents(List<String> inchiComponents) {
		this.inchiComponents = inchiComponents;
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
	 * null by default (i.e. unused in reaction)
	 * @return
	 */
	public Integer getStoichiometry() {
		return stoichiometry;
	}

	void setStoichiometry(Integer stoichiometry) {
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
	public ChemicalEntityType getEntityType() {
		return entityType;
	}

	void setEntityType(ChemicalEntityType entityType) {
		this.entityType = entityType;
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
	
	boolean hasAmountOrEquivalentsOrYield() {
		return (amountValue != null || equivalents != null || percentYield != null);
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
		Element reactant = new Element("reactant", CML_NAMESPACE);
		Element molecule = new Element("molecule", CML_NAMESPACE);
		reactant.appendChild(molecule);
		Element nameEl = new Element("name", CML_NAMESPACE);
		nameEl.appendChild(name);
		nameEl.addAttribute(new Attribute("dictRef","nameDict:unknown"));
		molecule.appendChild(nameEl);
		molecule.addAttribute(new Attribute("id", id));
		
		if (role != null){
			reactant.addAttribute(new Attribute("role", role.toString()));
		}
		
		if (amountValue!=null){
			Element amount = new Element("amount", CML_NAMESPACE);
			amount.appendChild(String.valueOf(amountValue));
			if (amountUnits!=null){
				amount.addAttribute(new Attribute("units", "unit:" + amountUnits));
			}
			reactant.appendChild(amount);
		}
		if (molarityValue!=null){
			Element amount = new Element("amount", CML_NAMESPACE);
			amount.appendChild(String.valueOf(molarityValue));
			if (molarityUnits!=null){
				amount.addAttribute(new Attribute("units", "unit:" + molarityUnits));
			}
			reactant.appendChild(amount);
		}
		if (volumeValue!=null){
			Element amount = new Element("amount", CML_NAMESPACE);
			amount.appendChild(String.valueOf(volumeValue));
			if (volumeUnits!=null){
				amount.addAttribute(new Attribute("units", "unit:" + volumeUnits));
			}
			reactant.appendChild(amount);
		}
		if (massValue!=null){
			Element amount = new Element("amount", CML_NAMESPACE);
			amount.appendChild(String.valueOf(massValue));
			if (massUnits!=null){
				amount.addAttribute(new Attribute("units", "unit:" + massUnits));
			}
			reactant.appendChild(amount);
		}
		if (stoichiometry !=null){
			reactant.addAttribute(new Attribute("count", String.valueOf(stoichiometry)));
		}

		if (equivalents!=null){
			Element amount = new Element("amount", CML_NAMESPACE);
			amount.appendChild(String.valueOf(equivalents));
			amount.addAttribute(new Attribute("units", "unit:" + equivalentsUnits));
			reactant.appendChild(amount);
		}
		
		if (pH!=null){
			Element amount = new Element("amount", CML_NAMESPACE);
			amount.appendChild(String.valueOf(pH));
			amount.addAttribute(new Attribute("units", "unit:" + "pH"));
			reactant.appendChild(amount);
		}
		
		if (percentYield!=null){
			Element amount = new Element("amount", CML_NAMESPACE);
			amount.appendChild(String.valueOf(percentYield));
			amount.addAttribute(new Attribute("units", "unit:percentYield"));
			reactant.appendChild(amount);
		}
		
		if (hasSmiles()){
			Element smilesIdentifier = new Element("identifier", CML_NAMESPACE);
			smilesIdentifier.addAttribute(new Attribute("dictRef", "cml:smiles"));
			smilesIdentifier.addAttribute(new Attribute("value", getSmiles()));
			reactant.appendChild(smilesIdentifier);
		}

		if (hasInchi()){
			Element inchiIdentifier = new Element("identifier", CML_NAMESPACE);
			inchiIdentifier.addAttribute(new Attribute("dictRef", "cml:inchi"));
			inchiIdentifier.addAttribute(new Attribute("value", getInchi()));
			reactant.appendChild(inchiIdentifier);
		}
		
		//not CMLreact
		if (entityType != null){
			Element typeEl = new Element("entityType", DL_NAMESPACE);
			typeEl.setNamespacePrefix("dl");
			typeEl.appendChild(entityType.toString());
			reactant.appendChild(typeEl);

		}
		if (state != null){
			Element stateEl = new Element("state", DL_NAMESPACE);
			stateEl.setNamespacePrefix("dl");
			stateEl.appendChild(state);
			reactant.appendChild(stateEl);
		}
		return reactant;
	}
}

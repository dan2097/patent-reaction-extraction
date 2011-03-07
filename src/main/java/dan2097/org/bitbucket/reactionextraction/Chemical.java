package dan2097.org.bitbucket.reactionextraction;

import nu.xom.Attribute;
import nu.xom.Element;
import dan2097.org.bitbucket.utility.InchiNormaliser;
import dan2097.org.bitbucket.utility.Utils;

public class Chemical{

	private final String name;
	private final String smiles;
	private final String inchi;
	private String massValue;
	private String massUnits;
	private String amountValue;
	private String amountUnits;
	private String volumeValue;
	private String volumeUnits;


//	private Element cml;
	
	public Chemical(String name) {
		this.name = name;
		smiles = Utils.resolveNameToSmiles(name);
		String rawInchi = Utils.resolveNameToInchi(name);
		if (rawInchi!=null){
			inchi = InchiNormaliser.normaliseInChI(rawInchi);
		}
		else{
			inchi =null;
		}
	}
	
	
	
	String getName() {
		return name;
	}

	/**
	 * A highly interoperable structural identifier. Null if structure is unavailable
	 * @return
	 */
	String getSmiles() {
		return smiles;
	}
	
	/**
	 * A canonical identifier for this chemical. Null if structure is unavailable
	 * @return
	 */
	String getInchi() {
		return inchi;
	}
	
	/**
	 * Gets a value for the mass. Meaningless without reference to mass units
	 * Typically, but not always, this can be expressed as a float
	 * (or null if unavailable)
	 * @return
	 */
	String getMassValue() {
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
	String getMassUnits() {
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
	String getAmountValue() {
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
	String getAmountUnits() {
		return amountUnits;
	}

	void setAmountUnits(String amountUnits) {
		this.amountUnits = amountUnits;
	}

	/**
	 * Gets a value for the volume. Meaningless without reference to volume units
	 * Typically, but not always, this can be expressed as a float
	 * (or null if unavailable)
	 * @return
	 */
	String getVolumeValue() {
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
	String getVolumeUnits() {
		return volumeUnits;
	}

	void setVolumeUnits(String volumeUnits) {
		this.volumeUnits = volumeUnits;
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
		return reactant;
	}
	
}

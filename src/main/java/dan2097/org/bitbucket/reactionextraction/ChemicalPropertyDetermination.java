package dan2097.org.bitbucket.reactionextraction;

import dan2097.org.bitbucket.utility.ChemicalTaggerTags;

import nu.xom.Element;
import nu.xom.Elements;

/**
 * Determine a chemicals properties e.g. quantity, phase etc. from local semantic information
 * @author dl387
 *
 */
public class ChemicalPropertyDetermination {

	static void determineProperties(Chemical chemical, Element molecule){
	    Elements quantityElements = molecule.getChildElements(ChemicalTaggerTags.QUANTITY_Container);
//		if (quantityElements.size()>1){//TODO are cases like indicative of a bug in chemical tagger e.g. 2 quantitys back to back
//			throw new IllegalArgumentException("More than 1 quantity element given for same chemical");
//		}
		if (quantityElements.size()>0){
		    Element quantityElement = quantityElements.get(0);
			determineMass(chemical, quantityElement);
			determineAmount(chemical, quantityElement);
			determineQuantity(chemical, quantityElement);
		}
	}

	private static void determineQuantity(Chemical chemical, Element quantityElement) {
		Elements volumes = quantityElement.getChildElements(ChemicalTaggerTags.VOLUME_Container);
		if (volumes.size()>1){
			throw new IllegalArgumentException("More than 1 volume given for same chemical");
		}
		if (volumes.size()>0){
			Element volume = volumes.get(0);
			chemical.setVolumeValue(volume.getFirstChildElement(ChemicalTaggerTags.CD).getValue());
			chemical.setVolumeUnits(volume.getFirstChildElement(ChemicalTaggerTags.NN_VOL).getValue());
		}
	}

	private static void determineAmount(Chemical chemical, Element quantityElement) {
		Elements amounts = quantityElement.getChildElements(ChemicalTaggerTags.AMOUNT_Container);
		if (amounts.size()>1){
			throw new IllegalArgumentException("More than 1 amount given for same chemical");
		}
		if (amounts.size()>0){
			Element amount = amounts.get(0);
			chemical.setAmountValue(amount.getFirstChildElement(ChemicalTaggerTags.CD).getValue());
			chemical.setAmountUnits(amount.getFirstChildElement(ChemicalTaggerTags.NN_AMOUNT).getValue());
		}
	}

	private static void determineMass(Chemical chemical, Element quantityElement) {
		Elements masses = quantityElement.getChildElements(ChemicalTaggerTags.MASS_Container);
		if (masses.size()>1){
			throw new IllegalArgumentException("More than 1 mass given for same chemical");
		}
		if (masses.size()>0){
			Element mass = masses.get(0);
			chemical.setMassValue(mass.getFirstChildElement(ChemicalTaggerTags.CD).getValue());
			chemical.setMassUnits(mass.getFirstChildElement(ChemicalTaggerTags.NN_MASS).getValue());
		}
	}
}

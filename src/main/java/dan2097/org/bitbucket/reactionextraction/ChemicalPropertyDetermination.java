package dan2097.org.bitbucket.reactionextraction;

import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.opsin.XOMTools;

import dan2097.org.bitbucket.utility.ChemicalTaggerTags;

import nu.xom.Element;
import nu.xom.Elements;

/**
 * Determine a chemicals properties e.g. quantity, phase etc. from local semantic information
 * @author dl387
 *
 */
public class ChemicalPropertyDetermination {

	static Logger LOG = Logger.getLogger(ChemicalPropertyDetermination.class);
	
	static void determineProperties(Chemical chemical, Element molecule){
	    List<Element> quantityElements = XOMTools.getDescendantElementsWithTagName(molecule, ChemicalTaggerTags.QUANTITY_Container);
		for (int i = 0; i < quantityElements.size(); i++) {
		    Element quantityElement = quantityElements.get(i);
			determineVolume(chemical, quantityElement);
			determineAmount(chemical, quantityElement);
			determineMass(chemical, quantityElement);
			determineMolarity(chemical, quantityElement);
			determineEquivalents(chemical, quantityElement);
			determinepH(chemical, quantityElement);
			determineYield(chemical, quantityElement);
		}
	}

	private static void determineVolume(Chemical chemical, Element quantityElement) {
		Elements volumes = quantityElement.getChildElements(ChemicalTaggerTags.VOLUME_Container);
		if (volumes.size()>1){
			LOG.debug("More than 1 volume given for same chemical");
		}
		else if (volumes.size()>0){
			if (chemical.getVolumeValue()!=null){
				LOG.debug("More than 1 volume given for same chemical");
			}
			else{
				Element volume = volumes.get(0);
				chemical.setVolumeValue(volume.getFirstChildElement(ChemicalTaggerTags.CD).getValue());
				chemical.setVolumeUnits(volume.getFirstChildElement(ChemicalTaggerTags.NN_VOL).getValue());
			}
		}
	}

	private static void determineAmount(Chemical chemical, Element quantityElement) {
		Elements amounts = quantityElement.getChildElements(ChemicalTaggerTags.AMOUNT_Container);
		if (amounts.size()>1){
			LOG.debug("More than 1 amount given for same chemical");
		}
		else if (amounts.size()>0){
			if (chemical.getAmountValue()!=null){
				LOG.debug("More than 1 amount given for same chemical");
			}
			else{
				Element amount = amounts.get(0);
				chemical.setAmountValue(amount.getFirstChildElement(ChemicalTaggerTags.CD).getValue());
				chemical.setAmountUnits(amount.getFirstChildElement(ChemicalTaggerTags.NN_AMOUNT).getValue());
			}
		}
	}
	
	private static void determineMass(Chemical chemical, Element quantityElement) {
		Elements masses = quantityElement.getChildElements(ChemicalTaggerTags.MASS_Container);
		if (masses.size()>1){
			LOG.debug("More than 1 mass given for same chemical");
		}
		else if (masses.size()>0){
			if (chemical.getMassValue()!=null){
				LOG.debug("More than 1 mass given for same chemical");
			}
			else{
				Element mass = masses.get(0);
				chemical.setMassValue(mass.getFirstChildElement(ChemicalTaggerTags.CD).getValue());
				chemical.setMassUnits(mass.getFirstChildElement(ChemicalTaggerTags.NN_MASS).getValue());
			}
		}
	}

	private static void determineMolarity(Chemical chemical, Element quantityElement) {
		Elements molarAmounts = quantityElement.getChildElements(ChemicalTaggerTags.MOLAR_Container);
		if (molarAmounts.size()>1){
			LOG.debug("More than 1 molarity given for same chemical");
		}
		else if (molarAmounts.size()>0){
			if (chemical.getAmountValue()!=null){
				LOG.debug("More than 1 molarity given for same chemical");
			}
			else{
				Element molarity = molarAmounts.get(0);
				chemical.setMolarityValue(molarity.getFirstChildElement(ChemicalTaggerTags.CD).getValue());
				chemical.setMolarityUnits(molarity.getFirstChildElement(ChemicalTaggerTags.NN_MOLAR).getValue());
			}
		}
	}
	
	private static void determinepH(Chemical chemical, Element quantityElement) {
		Elements pH_els = quantityElement.getChildElements(ChemicalTaggerTags.PH_Container);
		if (pH_els.size()>1){
			LOG.debug("More than 1 pH given for same chemical");
		}
		else if (pH_els.size()>0){
			if (chemical.getpH()!=null){
				LOG.debug("More than 1 pH given for same chemical");
			}
			else{
				String ph = pH_els.get(0).getFirstChildElement(ChemicalTaggerTags.CD).getValue();
				try{ 
					double d = Double.parseDouble(ph);
					chemical.setpH(d);
				}
				catch (NumberFormatException e) {
					LOG.debug("pH was not numeric!");
				}
			}
		}
	}
	
	private static void determineEquivalents(Chemical chemical, Element quantityElement) {
		Elements equivalentsEls = quantityElement.getChildElements(ChemicalTaggerTags.EQUIVALENT_Container);
		if (equivalentsEls.size()>1){
			LOG.debug("More than 1 value for equivalents given for same chemical");
		}
		else if (equivalentsEls.size()>0){
			if (chemical.getEquivalents()!=null){
				LOG.debug("More than 1 value for equivalents given for same chemical");
			}
			else{
				String equivalentVal = equivalentsEls.get(0).getFirstChildElement(ChemicalTaggerTags.CD).getValue();
				try{ 
					double d = Double.parseDouble(equivalentVal);
					chemical.setEquivalents(d);
				}
				catch (NumberFormatException e) {
					LOG.debug("equivalents value was not numeric!");
				}
			}
		}
	}


	private static void determineYield(Chemical chemical, Element quantityElement) {
		Elements yields = quantityElement.getChildElements(ChemicalTaggerTags.YIELD_Container);
		if (yields.size()>1){
			LOG.debug("More than 1 yield given for same chemical");
		}
		else if (yields.size()>0){
			if (chemical.getPercentYield()!=null){
				LOG.debug("More than 1 yield given for same chemical");
			}
			else{
				Element yield = yields.get(0);
				String value = yield.query(".//" + ChemicalTaggerTags.CD).get(0).getValue();
				try{ 
					double d = Double.parseDouble(value);
					chemical.setPercentYield(d);
				}
				catch (NumberFormatException e) {
					LOG.debug("Yield was not a numeric percentage");
				}
			}
		}
	}
}

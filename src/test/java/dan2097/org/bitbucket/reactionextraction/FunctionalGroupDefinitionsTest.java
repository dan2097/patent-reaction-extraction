package dan2097.org.bitbucket.reactionextraction;

import java.util.Collection;
import java.util.Set;

import static junit.framework.Assert.*;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureException;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;

import dan2097.org.bitbucket.utility.IndigoHolder;

public class FunctionalGroupDefinitionsTest {

	@Test
	public void checkIntegrityOfKeysTest() {
		Set<String> keys = FunctionalGroupDefinitions.functionalClassToSmartsMap.keySet();
		for (String key : keys) {
			for (char charac : key.toCharArray()) {
				if (Character.isUpperCase(charac)){
					fail(key +" contained upper case character/s");
				}
			}
		}
		keys = FunctionalGroupDefinitions.functionalGroupToSmartsMap.keySet();
		for (String key : keys) {
			for (char charac : key.toCharArray()) {
				if (Character.isUpperCase(charac)){
					fail(key +" contained upper case character/s");
				}
			}
		}
	}
	
	@Test
	public void checkIntegrityOfDefinitionsTest() {
		Collection<String> values = FunctionalGroupDefinitions.functionalClassToSmartsMap.values();
		for (String value : values) {
			for (char charac : value.toCharArray()) {
				if (Character.isWhitespace(charac)){
					fail(value +" contained white space");
				}
			}
			try{
				IndigoHolder.getInstance().loadSmarts(value);
			}
			catch (Exception e) {
				fail("indigo threw an exception when parsing the following SMARTS: " + value);
			}
		}
		values = FunctionalGroupDefinitions.functionalGroupToSmartsMap.values();
		for (String value : values) {
			for (char charac : value.toCharArray()) {
				if (Character.isWhitespace(charac)){
					fail(value +" contained white space");
				}
			}
			try{
				IndigoHolder.getInstance().loadSmarts(value);
			}
			catch (Exception e) {
				fail("indigo threw an exception when parsing the following SMARTS: " + value);
			}
		}
	}
	
	@Test
	public void checkCategorisationBetweenFunctionalClassAndFunctionalGroupTest() throws NameToStructureException {
		NameToStructure opsin = NameToStructure.getInstance();
		Set<String> keys = FunctionalGroupDefinitions.functionalClassToSmartsMap.keySet();
		for (String key : keys) {
			assertEquals(key +" should not have been interpretable by OPSIN", OPSIN_RESULT_STATUS.FAILURE, opsin.parseChemicalName(key).getStatus());
		}
		keys = FunctionalGroupDefinitions.functionalGroupToSmartsMap.keySet();
		for (String key : keys) {
			assertEquals(key +" should have been interpretable by OPSIN", OPSIN_RESULT_STATUS.SUCCESS, opsin.parseChemicalName(key).getStatus());
		}
	}
}

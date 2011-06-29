package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;

import dan2097.org.bitbucket.utility.Utils;
public class ExperimentSectionHeadingDetectionTest {

	@Test
	public void justAChemicalNameTest() {
		assertEquals(1, Utils.getSystematicChemicalNamesFromText("ether").size());
	}
	
	@Test
	public void justAChemicalNameTest2() {
		assertEquals(1, Utils.getSystematicChemicalNamesFromText("Alkyl Ether Sulfates ").size());
	}
}

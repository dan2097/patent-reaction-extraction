package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;

import dan2097.org.bitbucket.utility.Utils;
public class ExperimentSectionHeadingDetectionTest {

	@Test
	public void justAChemicalNameTest() {
		assertEquals(1, Utils.getSystematicChemicalNamesFromText("ethane").size());
	}

}

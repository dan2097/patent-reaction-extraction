package dan2097.org.bitbucket.utility;

import static junit.framework.Assert.*;
import org.junit.Test;

public class ErroneousSpaceRemovalTest {

	@Test
	public void oxidationStateErroneousSpace(){
		assertEquals("The compound copper(I) chloride is normally misrecognised.",
				Utils.correctErroneousSpaceBeforeChargeOrOxidationNumber("The compound copper (I) chloride is normally misrecognised."));
	}

	@Test
	public void chargeErroneousSpace(){
		assertEquals("The compound copper(1+) chloride is normally misrecognised.",
				Utils.correctErroneousSpaceBeforeChargeOrOxidationNumber("The compound copper (1+) chloride is normally misrecognised."));
	}
	
	@Test
	public void compoundReference(){
		assertEquals("This reference to compound (I) should be untouched.",
				Utils.correctErroneousSpaceBeforeChargeOrOxidationNumber("This reference to compound (I) should be untouched."));
	}
}

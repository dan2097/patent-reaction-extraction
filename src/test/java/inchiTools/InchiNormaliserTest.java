package inchiTools;

import org.junit.Test;

import dan2097.org.bitbucket.inchiTools.InchiNormaliser;

import static junit.framework.Assert.*;

public class InchiNormaliserTest {

	@Test
	public void normaliseStandardInChI() {
		assertEquals("InChI=1/C6H6/c1-2-4-6-5-3-1/h1-6H", InchiNormaliser.normaliseInChI("InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H"));
	}

	@Test
	public void normaliseNonStandardInChI() {
		assertEquals("InChI=1/C2H5NO/c1-2(3)4/h1H3,(H2,3,4)/i1D", InchiNormaliser.normaliseInChI("InChI=1/C2H5NO/c1-2(3)4/h1H3,(H2,3,4)/i1D/f/h3H2"));
	}
}

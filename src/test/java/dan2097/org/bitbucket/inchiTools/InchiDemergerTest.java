package dan2097.org.bitbucket.inchiTools;

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class InchiDemergerTest {

	@Test
	public void demergeOctanolWater() {
		InchiDemerger demerger = new InchiDemerger("InChI=1/C8H18O.H2O/c1-2-3-4-5-6-7-8-9;/h9H,2-8H2,1H3;1H2");
		assertEquals(Arrays.asList("InChI=1/C8H18O/c1-2-3-4-5-6-7-8-9/h9H,2-8H2,1H3", "InChI=1/H2O/h1H2"), demerger.generateDemergedInchis());
	}
	
	@Test
	public void demergeDiWater() {
		InchiDemerger demerger = new InchiDemerger("InChI=1/2H2O/h2*1H2");
		assertEquals(Arrays.asList("InChI=1/H2O/h1H2", "InChI=1/H2O/h1H2"), demerger.generateDemergedInchis());
	}
	
	@Test
	public void demergePyridineEthanolAcetonitrile() {
		InchiDemerger demerger = new InchiDemerger("InChI=1/C5H5N.C2H3N.C2H6O/c1-2-4-6-5-3-1;2*1-2-3/h1-5H;1H3;3H,2H2,1H3");
		assertEquals(Arrays.asList("InChI=1/C5H5N/c1-2-4-6-5-3-1/h1-5H", "InChI=1/C2H3N/c1-2-3/h1H3", "InChI=1/C2H6O/c1-2-3/h3H,2H2,1H3"), demerger.generateDemergedInchis());
	}
	
	@Test
	public void demergeChlorine() {
		InchiDemerger demerger = new InchiDemerger("InChI=1/Cl");
		assertEquals(Arrays.asList("InChI=1/Cl"), demerger.generateDemergedInchis());
	}
}

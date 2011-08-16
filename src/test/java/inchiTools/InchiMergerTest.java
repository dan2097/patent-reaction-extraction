package inchiTools;

import static junit.framework.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import dan2097.org.bitbucket.inchiTools.InchiMerger;

public class InchiMergerTest {

	@Test
	public void mergeNormalInChIs() {
		List<String> inchis = Arrays.asList("InChI=1/C6H6/c1-2-4-6-5-3-1/h1-6H", "InChI=1/C3H7NO/c1-4(2)3-5/h3H,1-2H3");
		InchiMerger merger = new InchiMerger(inchis);
		assertEquals("InChI=1/C6H6.C3H7NO/c1-2-4-6-5-3-1;1-4(2)3-5/h1-6H;3H,1-2H3", merger.generateMergedNormalisedInchi());
	}
	
	@Test
	public void mergeInChIsWithStereochemistry() {
		List<String> inchis = Arrays.asList("InChI=1/C6H12O6/c7-1-3(9)5(11)6(12)4(10)2-8/h1,3-6,8-12H,2H2/t3-,4+,5+,6+/m0/s1", "InChI=1/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)/t2-/m0/s1/f/h5H");
		InchiMerger merger = new InchiMerger(inchis);
		assertEquals("InChI=1/C6H12O6.C3H7NO2/c7-1-3(9)5(11)6(12)4(10)2-8;1-2(4)3(5)6/h1,3-6,8-12H,2H2;2H,4H2,1H3,(H,5,6)/t3-,4+,5+,6+;2-/m00/s1", merger.generateMergedNormalisedInchi());
	}

	@Test
	public void mergeThreeInChIs() {
		List<String> inchis = Arrays.asList("InChI=1/C8H18O/c1-2-3-4-5-6-7-8-9/h9H,2-8H2,1H3", "InChI=1/C7H16O/c1-2-3-4-5-6-7-8/h8H,2-7H2,1H3", "InChI=1/C6H14O/c1-2-3-4-5-6-7/h7H,2-6H2,1H3");
		InchiMerger merger = new InchiMerger(inchis);
		assertEquals("InChI=1/C8H18O.C7H16O.C6H14O/c1-2-3-4-5-6-7-8-9;1-2-3-4-5-6-7-8;1-2-3-4-5-6-7/h9H,2-8H2,1H3;8H,2-7H2,1H3;7H,2-6H2,1H3", merger.generateMergedNormalisedInchi());
	}
}

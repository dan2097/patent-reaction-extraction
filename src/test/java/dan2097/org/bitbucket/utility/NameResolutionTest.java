package dan2097.org.bitbucket.utility;

import static junit.framework.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import dan2097.org.bitbucket.inchiTools.InchiNormaliser;

public class NameResolutionTest {

	@Test
	public void resolveNameToSmiles(){
		assertEquals("N", Utils.resolveNameToSmiles("NH3"));
		assertEquals("O", Utils.resolveNameToSmiles("H2O"));
	}
	
	@Test
	public void resolveNameListToSmiles1(){
		List<String> nameComponents = Arrays.asList("NH3", "H2O");
		assertEquals("N.O", Utils.resolveNameToSmiles(nameComponents));
	}
	
	@Test
	public void resolveNameListToSmiles2(){
		List<String> nameComponents = Arrays.asList("NH3.H2O");
		assertEquals("N.O", Utils.resolveNameToSmiles(nameComponents));
	}
	
	@Test
	public void resolveNameListToSmiles3(){
		List<String> nameComponents = Arrays.asList("NH3/H2O");
		assertEquals("N.O", Utils.resolveNameToSmiles(nameComponents));
	}
	
	@Test
	public void resolveNameToInChI(){
		assertEquals("InChI=1/H3N/h1H3", InchiNormaliser.normaliseInChI(Utils.resolveNameToInchi("NH3")));
		assertEquals("InChI=1/H2O/h1H2", InchiNormaliser.normaliseInChI(Utils.resolveNameToInchi("H2O")));
	}
	
	@Test
	public void resolveNameListToInChI1(){
		List<String> nameComponents = Arrays.asList("NH3", "H2O");
		assertEquals("InChI=1/H3N.H2O/h1H3;1H2", Utils.resolveNameToInchi(nameComponents));
	}
	
	@Test
	public void resolveNameListToInChI2(){
		List<String> nameComponents = Arrays.asList("NH3.H2O");
		assertEquals("InChI=1/H3N.H2O/h1H3;1H2", Utils.resolveNameToInchi(nameComponents));
	}
	
	@Test
	public void resolveNameListToInChI3(){
		List<String> nameComponents = Arrays.asList("NH3/H2O");
		assertEquals("InChI=1/H3N.H2O/h1H3;1H2", Utils.resolveNameToInchi(nameComponents));
	}
}

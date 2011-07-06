package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;
public class ChemicalTest {

	
	@Test
	public void hasImpreciseVolumeTest1() {
		Chemical cm = new Chemical("foo");
		cm.setVolumeValue("5");
		cm.setVolumeUnits("ml");
		assertEquals(false, cm.hasImpreciseVolume());
	}
	
	@Test
	public void hasImpreciseVolumeTest2() {
		Chemical cm = new Chemical("foo");
		cm.setVolumeValue("5");
		cm.setVolumeUnits("litres");
		assertEquals(true, cm.hasImpreciseVolume());
	}
	
	@Test
	public void hasImpreciseVolumeTest3() {
		Chemical cm = new Chemical("foo");
		cm.setVolumeValue("20");
		cm.setVolumeUnits("ml");
		assertEquals(true, cm.hasImpreciseVolume());
	}
	
	@Test
	public void hasImpreciseVolumeTest4() {
		Chemical cm = new Chemical("foo");
		cm.setVolumeValue("20.0");
		cm.setVolumeUnits("ml");
		assertEquals(false, cm.hasImpreciseVolume());
	}
}

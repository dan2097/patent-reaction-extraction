package dan2097.org.bitbucket.reactionextraction;

import java.util.Map;

import static junit.framework.Assert.*;

import nu.xom.Document;

import org.junit.Test;

import com.ggasoftware.indigo.IndigoObject;

import dan2097.org.bitbucket.utility.Utils;

public class IntegrationTests{
	
	@Test
	public void integrationTest1() throws Exception{
		Document doc = Utils.buildXmlFile(IntegrationTests.class.getResourceAsStream("patentText1.xml"));
		ExperimentalParser parser = Utils.extractReactions(doc);
		Map<Reaction, IndigoObject> reactions = parser.getAllFoundReactions();
		assertEquals(1, reactions.size());
		Reaction reaction = reactions.keySet().iterator().next();
		assertEquals(1, reaction.getProducts().size());
		Chemical product = reaction.getProducts().get(0);
		assertEquals(null, product.getAmountUnits());
		assertEquals(null, product.getAmountValue());
		assertEquals("InChI=1/C14H9ClN4O/c15-9-4-6-17-11(7-9)12-8-13(20)19-14(18-12)10-3-1-2-5-16-10/h1-8H,(H,18,19,20)", product.getInchi());
		assertEquals(null, product.getMassUnits());
		assertEquals(null, product.getMassValue());
		assertEquals(null, product.getMolarity());
		assertEquals("6-(4-chloropyrid-2-yl)-2-pyrid-2-yl-pyrimidin-4-ol", product.getName());
		assertEquals(ChemicalRole.product, product.getRole());
		assertEquals(ChemicalType.exact, product.getType());
		assertEquals(null, product.getVolumeUnits());
		assertEquals(null, product.getVolumeValue());

		assertEquals(3, reaction.getReactants().size());
		Chemical reactant1 = reaction.getReactants().get(0);
		assertEquals("mmol", reactant1.getAmountUnits());
		assertEquals("58", reactant1.getAmountValue());
		assertEquals("InChI=1/C10H10ClNO3/c1-2-15-10(14)6-9(13)8-5-7(11)3-4-12-8/h3-5H,2,6H2,1H3", reactant1.getInchi());
		assertEquals("g", reactant1.getMassUnits());
		assertEquals("13.15", reactant1.getMassValue());
		assertEquals(null, reactant1.getMolarity());
		assertEquals("3-(4-chloropyrid-2-yl)-3-oxopropionic acid ethyl ester", reactant1.getName());
		assertEquals(ChemicalRole.reactant, reactant1.getRole());
		assertEquals(ChemicalType.exact, reactant1.getType());
		assertEquals(null, reactant1.getVolumeUnits());
		assertEquals(null, reactant1.getVolumeValue());
		
		Chemical reactant2 = reaction.getReactants().get(1);
		assertEquals("mmol", reactant2.getAmountUnits());
		assertEquals("58", reactant2.getAmountValue());
		assertEquals("InChI=1/C6H7N3.ClH/c7-6(8)5-3-1-2-4-9-5;/h1-4H,(H3,7,8);1H", reactant2.getInchi());
		assertEquals("g", reactant2.getMassUnits());
		assertEquals("9.10", reactant2.getMassValue());
		assertEquals(null, reactant2.getMolarity());
		assertEquals("2-amidinopyridine hydrochloride", reactant2.getName());
		assertEquals(ChemicalRole.reactant, reactant2.getRole());
		assertEquals(ChemicalType.exact, reactant2.getType());
		assertEquals(null, reactant2.getVolumeUnits());
		assertEquals(null, reactant2.getVolumeValue());
		
		Chemical reactant3 = reaction.getReactants().get(2);
		assertEquals(null, reactant3.getAmountUnits());
		assertEquals(null, reactant3.getAmountValue());
		assertEquals("InChI=1/Na.H2O/h;1H2/q+1;/p-1", reactant3.getInchi());
		assertEquals(null, reactant3.getMassUnits());
		assertEquals(null, reactant3.getMassValue());
		assertEquals("4", reactant3.getMolarity());
		assertEquals("sodium hydroxide", reactant3.getName());
		assertEquals(ChemicalRole.reactant, reactant3.getRole());
		assertEquals(ChemicalType.exact, reactant3.getType());
		assertEquals("ml", reactant3.getVolumeUnits());
		assertEquals("14.44", reactant3.getVolumeValue());
		
		assertEquals(1, reaction.getSpectators().size());
		Chemical spectator = reaction.getSpectators().get(0);
		assertEquals(null, spectator.getAmountUnits());
		assertEquals(null, spectator.getAmountValue());
		assertEquals("InChI=1/C2H6O/c1-2-3/h3H,2H2,1H3", spectator.getInchi());
		assertEquals(null, spectator.getMassUnits());
		assertEquals(null, spectator.getMassValue());
		assertEquals(null, spectator.getMolarity());
		assertEquals("ethanol", spectator.getName());
		assertEquals(ChemicalRole.solvent, spectator.getRole());
		assertEquals(ChemicalType.exact, spectator.getType());
		assertEquals("ml", spectator.getVolumeUnits());
		assertEquals("400", spectator.getVolumeValue());
	}
	
	@Test
	public void integrationTest2() throws Exception{
		//Logger.getLogger("dan2097.org.bitbucket.reactionextraction").setLevel(Level.TRACE);
		Document doc = Utils.buildXmlFile(IntegrationTests.class.getResourceAsStream("patentText2.xml"));
		ExperimentalParser parser = Utils.extractReactions(doc);
		Map<Reaction, IndigoObject> reactions = parser.getAllFoundReactions();
		assertEquals(1, reactions.size());
		Reaction reaction = reactions.keySet().iterator().next();
		assertEquals(1, reaction.getProducts().size());//resolved by reference to title compound
		Chemical product = reaction.getProducts().get(0);
		assertEquals(null, product.getAmountUnits());
		assertEquals(null, product.getAmountValue());
		assertEquals("InChI=1/C14H10ClF3/c15-13(10-5-2-1-3-6-10)11-7-4-8-12(9-11)14(16,17)18/h1-9,13H", product.getInchi());
		assertEquals("g", product.getMassUnits());
		assertEquals("4.9", product.getMassValue());
		assertEquals(null, product.getMolarity());
		assertEquals("title compound", product.getName());
		assertEquals(ChemicalRole.product, product.getRole());
		assertEquals(ChemicalType.definiteReference, product.getType());
		assertEquals(null, product.getVolumeUnits());
		assertEquals(null, product.getVolumeValue());
		assertEquals(89d, product.getPercentYield(), 0.1d);

		assertEquals(2, reaction.getReactants().size());
		Chemical reactant1 = reaction.getReactants().get(0);
		assertEquals("mmol", reactant1.getAmountUnits());
		assertEquals("20", reactant1.getAmountValue());
		assertEquals("InChI=1/C14H11F3O/c15-14(16,17)12-8-4-7-11(9-12)13(18)10-5-2-1-3-6-10/h1-9,13,18H", reactant1.getInchi());
		assertEquals(null, reactant1.getMassUnits());
		assertEquals(null, reactant1.getMassValue());
		assertEquals(null, reactant1.getMolarity());
		assertEquals("3-(trifluoromethyl)benzhydrol", reactant1.getName());
		assertEquals(ChemicalRole.reactant, reactant1.getRole());
		assertEquals(ChemicalType.exact, reactant1.getType());
		assertEquals("mL", reactant1.getVolumeUnits());
		assertEquals("5", reactant1.getVolumeValue());
		
		Chemical reactant2 = reaction.getReactants().get(1);
		assertEquals("mmol", reactant2.getAmountUnits());
		assertEquals("41", reactant2.getAmountValue());
		assertEquals("InChI=1/Cl2OS/c1-4(2)3", reactant2.getInchi());
		assertEquals(null, reactant2.getMassUnits());
		assertEquals(null, reactant2.getMassValue());
		assertEquals(null, reactant2.getMolarity());
		assertEquals("thionyl chloride", reactant2.getName());
		assertEquals(ChemicalRole.reactant, reactant2.getRole());
		assertEquals(ChemicalType.exact, reactant2.getType());
		assertEquals("mL", reactant2.getVolumeUnits());
		assertEquals("3", reactant2.getVolumeValue());
		
		assertEquals(1, reaction.getSpectators().size());
		Chemical spectator = reaction.getSpectators().get(0);
		assertEquals(null, spectator.getAmountUnits());
		assertEquals(null, spectator.getAmountValue());
		assertEquals("InChI=1/CH2Cl2/c2-1-3/h1H2", spectator.getInchi());
		assertEquals(null, spectator.getMassUnits());
		assertEquals(null, spectator.getMassValue());
		assertEquals(null, spectator.getMolarity());
		assertEquals("CH2Cl2", spectator.getName());
		assertEquals(ChemicalRole.solvent, spectator.getRole());
		assertEquals(ChemicalType.exact, spectator.getType());
		assertEquals("mL", spectator.getVolumeUnits());
		assertEquals("10", spectator.getVolumeValue());
	}
}

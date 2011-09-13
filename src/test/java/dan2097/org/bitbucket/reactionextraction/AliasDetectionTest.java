package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;
public class AliasDetectionTest {

	@Test
	public void aliasFromTitleText1() {
		assertEquals("3", TitleTextAliasExtractor.findAlias("benzene (3)"));
	}
	
	@Test
	public void aliasFromTitleText2() {
		assertEquals("11", TitleTextAliasExtractor.findAlias("benzene (11)"));
	}
	
	@Test
	public void aliasFromTitleText3() {
		assertEquals("3a", TitleTextAliasExtractor.findAlias("benzene (3a)"));
	}
	
	@Test
	public void aliasFromTitleText4() {
		assertEquals("121", TitleTextAliasExtractor.findAlias("1,4-dioxane(121)"));
	}
	
	@Test
	public void aliasFromTitleText5() {
		assertEquals("9b", TitleTextAliasExtractor.findAlias("pentan-3-one 9b:"));
	}
	
	@Test
	public void aliasFromTitleText6() {
		assertEquals("(R,R)-5", TitleTextAliasExtractor.findAlias("chiralthing (R,R)-5"));
	}
	
	@Test
	public void aliasFromTitleText7() {
		assertEquals("R,R-5", TitleTextAliasExtractor.findAlias("chiralthing R,R-5"));
	}
	
	@Test
	public void aliasFromTitleText8() {
		assertEquals("(R)-1", TitleTextAliasExtractor.findAlias("chiralthing (R)-1"));
	}
	
	@Test
	public void aliasFromTitleText9() {
		assertEquals("(E)-7", TitleTextAliasExtractor.findAlias("chiralthing (E)-7"));
	}
	
	@Test
	public void aliasFromTitleText10() {
		assertEquals("2′", TitleTextAliasExtractor.findAlias("benzene 2′"));
	}
	
	@Test
	public void aliasFromTitleText11() {
		assertEquals("9a", TitleTextAliasExtractor.findAlias("benzene (9a):"));
	}
	
	@Test
	public void aliasFromTitleText12() {
		assertEquals("12", TitleTextAliasExtractor.findAlias("benzene 12"));
	}

	@Test
	public void aliasFromTitleText13() {
		assertEquals("(R)-3", TitleTextAliasExtractor.findAlias("chiralthing ((R)-3)"));
	}
	
	@Test
	public void aliasFromTitleText14() {
		assertEquals("3ad", TitleTextAliasExtractor.findAlias("benzene (3ad)."));
	}
	
	@Test
	public void aliasFromTitleText15() {
		assertEquals("(4aS,8R,8aS)-9", TitleTextAliasExtractor.findAlias("chiralthing ((4aS,8R,8aS)-9)"));
	}
	
	@Test
	public void aliasFromTitleText16() {
		assertEquals("5g", TitleTextAliasExtractor.findAlias("chiralthing ((\u00B1)-5g)"));//+-
	}
	
	@Test
	public void aliasFromTitleText17() {
		assertEquals("ent-2", TitleTextAliasExtractor.findAlias("chiralthing ent-2"));
	}
	
	@Test
	public void aliasFromTitleText18() {
		assertEquals("ent-2", TitleTextAliasExtractor.findAlias("chiralthing (ent-2)"));
	}
	
	@Test
	public void aliasFromTitleText19() {
		assertEquals("ligand PM4", TitleTextAliasExtractor.findAlias("2,6-Di(2-pyridyl)-4-pyrimidinol (ligand PM4)"));
	}
	
	@Test
	public void aliasFromTitleText20() {//NOTE something like "1)" could be a list indicator
		assertEquals("19.45", TitleTextAliasExtractor.findAlias("(19.45) 2-Amino-5-isobutyl-4-{2-[5-(1-(3-chlorophenyl)-1,3-propyl)phosphono]furanyl}thiazole, minor isomer. Anal. Calcd. for C"));
	}
	
	@Test
	public void aliasFromTitleText21() {
		assertEquals("Formula 12", TitleTextAliasExtractor.findAlias("Formula 12, benzene"));
	}
	
	@Test
	public void aliasFromTitleText22() {
		assertEquals("Compound XVI", TitleTextAliasExtractor.findAlias("benzene (Compound XVI)"));
	}
	
	@Test
	public void aliasFromTitleText23() {
		assertEquals("III", TitleTextAliasExtractor.findAlias("benzene (III)"));
	}
	
	@Test
	public void aliasFromTitleText24() {
		assertEquals("Compound 3", TitleTextAliasExtractor.findAlias("benzene (Compound 3)"));
	}

	@Test
	public void aliasFromTitleText25() {
		assertEquals("Intermediate 23", TitleTextAliasExtractor.findAlias("Intermediate 23: benzene"));
	}
	
	@Test
	public void aliasFromTitleText26() {
		assertEquals("Compound 6J", TitleTextAliasExtractor.findAlias("benzene (Compound 6J)"));
	}

	@Test
	public void aliasFromTitleText27() {
		assertNull(TitleTextAliasExtractor.findAlias("dimethyl ether"));
	}

	@Test
	public void aliasFromTitleText28() {
		assertEquals("(+)-5", TitleTextAliasExtractor.findAlias("benzene (+)-5"));
	}

	@Test
	public void aliasFromTitleText29() {
		assertNull(TitleTextAliasExtractor.findAlias("3-methylpetane"));
	}
	
}

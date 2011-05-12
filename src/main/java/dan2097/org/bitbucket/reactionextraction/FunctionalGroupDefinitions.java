package dan2097.org.bitbucket.reactionextraction;

import java.util.HashMap;
import java.util.Map;

public class FunctionalGroupDefinitions {

	public static final Map<String, String> functionalClassToSmartsMap = new HashMap<String, String>();
	public static final Map<String, String> functionalGroupToSmartsMap = new HashMap<String, String>();
	static {
		functionalClassToSmartsMap.put("alkene", "[$([CX3]=[CX3])]");
		functionalClassToSmartsMap.put("alkyne", "[$([CX2]#C)]");
		functionalClassToSmartsMap.put("arene", "c");
		functionalClassToSmartsMap.put("carbonyl", "[CX3]=[OX1]");//maybe should be more specific...
		functionalClassToSmartsMap.put("acyl halide", "[CX3](=[OX1])[F,Cl,Br,I]");
		functionalClassToSmartsMap.put("aldehyde", "[CX3H1](=O)[#6]");
		functionalClassToSmartsMap.put("anhydride", "[CX3](=[OX1])[OX2][CX3](=[OX1])");
		functionalClassToSmartsMap.put("amide", "[NX3][CX3](=[OX1])[#6]");
		functionalClassToSmartsMap.put("amidinium", "[NX3][CX3]=[NX3+]");
		functionalClassToSmartsMap.put("carbamic ester", "[NX3][CX3](=[OX1])[OX2H0]");
		functionalClassToSmartsMap.put("carbonic ester", "[CX3](=[OX1])(O)O");
		functionalClassToSmartsMap.put("carbonic diester", "C[OX2][CX3](=[OX1])[OX2]C");
		functionalClassToSmartsMap.put("carbonic acid diester", "C[OX2][CX3](=[OX1])[OX2]C");
		functionalClassToSmartsMap.put("carboxylic acid", "[CX3](=O)[OX2H1]");
		functionalClassToSmartsMap.put("ester", "[#6][CX3](=O)[OX2H0][#6]");
		functionalClassToSmartsMap.put("ketone", "[#6][CX3](=O)[#6]");
		functionalClassToSmartsMap.put("ether", "[OD2]([#6])[#6]");
		functionalClassToSmartsMap.put("amine", "[NX3;H2,H1;!$(NC=O)]");
		functionalClassToSmartsMap.put("enamine", "[NX3][CX3]=[CX3]");
		functionalClassToSmartsMap.put("amino acid", "[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]");
		functionalClassToSmartsMap.put("hydrazone", "[NX3][NX2]=[*]");
		functionalClassToSmartsMap.put("imine", "[$([CX3]([#6])[#6]),$([CX3H][#6])]=[$([NX2][#6]),$([NX2H])]");
		functionalClassToSmartsMap.put("dicarboximide", "[CX3](=[OX1])[NX3][CX3](=[OX1])");
		functionalClassToSmartsMap.put("nitrile", "[NX1]#[CX2]");
		functionalClassToSmartsMap.put("nitro", "[$([NX3](=O)=O),$([NX3+](=O)[O-])][!#8]");
		functionalClassToSmartsMap.put("n-oxide", "[$([#7+][OX1-]),$([#7v5]=[OX1]);!$([#7](~[O])~[O]);!$([#7]=[#7])]");
		functionalClassToSmartsMap.put("enol", "[OX2H][#6X3]=[#6]");
		functionalClassToSmartsMap.put("peroxide", "[OX2,OX1-][OX2,OX1-]");
		functionalClassToSmartsMap.put("phosphoric ester", "[$(P(=[OX1])([OX2][#6])([$([OX2H]),$([OX1-]),$([OX2][#6])])[$([OX2H]),$([OX1-]),$([OX2][#6]),$([OX2]P)]),$([P+]([OX1-])([OX2][#6])([$([OX2H]),$([OX1-]),$([OX2][#6])])[$([OX2H]),$([OX1-]),$([OX2][#6]),$([OX2]P)])]");
		functionalClassToSmartsMap.put("sulfide", "[#16X2H0]");
		functionalClassToSmartsMap.put("thiol", "[#16X2H]");
		functionalClassToSmartsMap.put("thioamide", "[NX3][CX3]=[SX1]");
		functionalClassToSmartsMap.put("disulfide", "[#16X2H0][#16X2H0]");
		functionalClassToSmartsMap.put("sulfinate", "[$([#16X3](=[OX1])[OX2H0]),$([#16X3+]([OX1-])[OX2H0])]");
		functionalClassToSmartsMap.put("sulfinic acid", "[$([#16X3](=[OX1])[OX2H,OX1H0-]),$([#16X3+]([OX1-])[OX2H,OX1H0-])]");
		functionalClassToSmartsMap.put("sulfone", "[$([#16X4](=[OX1])(=[OX1])([#6])[#6]),$([#16X4+2]([OX1-])([OX1-])([#6])[#6])]");
		functionalClassToSmartsMap.put("sulfonic acid", "[$([#16X4](=[OX1])(=[OX1])([#6])[OX2H,OX1H0-]),$([#16X4+2]([OX1-])([OX1-])([#6])[OX2H,OX1H0-])]");
		
		
		functionalGroupToSmartsMap.put("carbamate", "[NX3,NX4+][CX3](=[OX1])[OX2,OX1-]");
		functionalGroupToSmartsMap.put("carbamic acid", "[NX3,NX4+][CX3](=[OX1])[OX2H,OX1-]");
		functionalGroupToSmartsMap.put("carbonic acid", "[CX3](=[OX1])(O)O");
		functionalGroupToSmartsMap.put("cyanamide", "[NX3][CX2]#[NX1]");
		functionalGroupToSmartsMap.put("azide", "[$(*-[NX2-]-[NX2+]#[NX1]),$(*-[NX2]=[NX2+]=[NX1-])]");
		functionalGroupToSmartsMap.put("isonitrile", "[CX1-]#[NX2+]");
		functionalGroupToSmartsMap.put("nitrate", "[$([NX3](=[OX1])(=[OX1])O),$([NX3+]([OX1-])(=[OX1])O)]");
		functionalGroupToSmartsMap.put("phosphoric acid", "[$(P(=[OX1])([$([OX2H]),$([OX1-]),$([OX2]P)])([$([OX2H]),$([OX1-]),$([OX2]P)])[$([OX2H]),$([OX1-]),$([OX2]P)]),$([P+]([OX1-])([$([OX2H]),$([OX1-]),$([OX2]P)])([$([OX2H]),$([OX1-]),$([OX2]P)])[$([OX2H]),$([OX1-]),$([OX2]P)])]");
	
	
	}
}

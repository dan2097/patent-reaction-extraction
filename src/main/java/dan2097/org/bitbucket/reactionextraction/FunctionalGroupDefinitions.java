package dan2097.org.bitbucket.reactionextraction;

import java.util.HashMap;
import java.util.Locale;
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
		functionalClassToSmartsMap.put("thioester", "[#6][CX3](=O)[SX2H0][#6]");
		functionalClassToSmartsMap.put("ketone", "[#6][CX3](=O)[#6]");
		functionalClassToSmartsMap.put("ether", "[OX2]([#6])[#6]");
		functionalClassToSmartsMap.put("thioether", "[SX2]([#6])[#6]");
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
		functionalClassToSmartsMap.put("sulfonamide", "[$([SX4](=[OX1])(=[OX1])([!O])[NX3]),$([SX4+2]([OX1-])([OX1-])([!O])[NX3])]");
		functionalClassToSmartsMap.put("sulfoxide", "[$([#16X3]=[OX1]),$([#16X3+][OX1-])]");
		functionalClassToSmartsMap.put("sulfenic acid", "[#16X2][OX2H,OX1H0-]");
		functionalClassToSmartsMap.put("sulfenate", "[#16X2][OX2H0]");
		functionalClassToSmartsMap.put("halogen", "[F,Cl,Br,I]");
		functionalClassToSmartsMap.put("alkyl halide", "[CX4][F,Cl,Br,I]");
		functionalClassToSmartsMap.put("amino acid", "[NX3,NX4+][CX4H]([*])[CX3](=[OX1])[O,N]");
		functionalClassToSmartsMap.put("sulfide", "[#16X2H0]");
		functionalClassToSmartsMap.put("aryl", "[R1]");
		functionalClassToSmartsMap.put("alcohol", "[CX4][OX2H]");
		functionalClassToSmartsMap.put("alkane", "[CX4]");
		functionalClassToSmartsMap.put("alkene", "[CX3]=[CX3]");
		functionalClassToSmartsMap.put("alkyne", "[CX2]#[CX2]");
		functionalClassToSmartsMap.put("alkoxy", "[CX4][OX2H0]");
		functionalClassToSmartsMap.put("aryloxy", "[R1][OX2H0]");
		functionalClassToSmartsMap.put("azo", "[NX3]=[NX3]");
		functionalClassToSmartsMap.put("haloalkane", "[CX4][F,Cl,Br,I]");
		functionalClassToSmartsMap.put("oxime", "[CX3]=[NX2][OX2H]");
		functionalClassToSmartsMap.put("nitroso", "[NX3]=[OX1]");
		
		functionalGroupToSmartsMap.put("carbamate", "[NX3,NX4+][CX3](=[OX1])[OX2,OX1-]");
		functionalGroupToSmartsMap.put("carbamic acid", "[NX3,NX4+][CX3](=[OX1])[OX2H,OX1-]");
		functionalGroupToSmartsMap.put("carbonic acid", "[CX3](=[OX1])(O)O");
		functionalGroupToSmartsMap.put("cyanamide", "[NX3][CX2]#[NX1]");
		functionalGroupToSmartsMap.put("azide", "[$(*-[NX2-]-[NX2+]#[NX1]),$(*-[NX2]=[NX2+]=[NX1-])]");
		functionalGroupToSmartsMap.put("isonitrile", "[CX1-]#[NX2+]");
		functionalGroupToSmartsMap.put("isocyanide", "[CX1-]#[NX2+]");
		functionalGroupToSmartsMap.put("nitrate", "[$([NX3](=[OX1])(=[OX1])O),$([NX3+]([OX1-])(=[OX1])O)]");
		functionalGroupToSmartsMap.put("phosphoric acid", "[$(P(=[OX1])([$([OX2H]),$([OX1-]),$([OX2]P)])([$([OX2H]),$([OX1-]),$([OX2]P)])[$([OX2H]),$([OX1-]),$([OX2]P)]),$([P+]([OX1-])([$([OX2H]),$([OX1-]),$([OX2]P)])([$([OX2H]),$([OX1-]),$([OX2]P)])[$([OX2H]),$([OX1-]),$([OX2]P)])]");
		functionalGroupToSmartsMap.put("sulfate", "[$([SX4](=O)(=O)(O)O),$([SX4+2]([O-])([O-])(O)O)]");
		functionalGroupToSmartsMap.put("sulfamate", "[$([#16X4]([NX3])(=[OX1])(=[OX1])[OX2][#6]),$([#16X4+2]([NX3])([OX1-])([OX1-])[OX2][#6])]");
		functionalGroupToSmartsMap.put("cyanate", "[NX1]#[CX2][OX2H0]");
		functionalGroupToSmartsMap.put("thiocyanate", "[NX1]#[CX2][SX2H0]");
		functionalGroupToSmartsMap.put("isocyanate", "[OX1]=[CX2]=[NX3H0]");
		functionalGroupToSmartsMap.put("isothiocyanate", "[SX1]=[CX2]=[NX3H0]");
	}
	
	/**
	 * Retrieves the appropriate SMARTS from functionalClassToSmartsMap or functionalGroupToSmartsMap
	 * Returns null if no SMARTS are available
	 * @param name
	 * @return
	 */
	public static String getSmartsFromChemicalName(String name) {
		name = name.toLowerCase(Locale.ROOT);
		if (functionalClassToSmartsMap.get(name)!=null){
			return functionalClassToSmartsMap.get(name);
		}
		if (functionalGroupToSmartsMap.get(name)!=null){
			return functionalGroupToSmartsMap.get(name);
		}
		return null;
	}
	
	/**
	 * Retrieves the appropriate SMARTS from functionalClassToSmartsMap
	 * Returns null if no SMARTS are available
	 * @param name
	 * @return
	 */
	public static String  getFunctionalClassSmartsFromChemicalName(String name) {
		name = name.toLowerCase(Locale.ROOT);
		if (functionalClassToSmartsMap.get(name)!=null){
			return functionalClassToSmartsMap.get(name);
		}
		return null;
	}
}

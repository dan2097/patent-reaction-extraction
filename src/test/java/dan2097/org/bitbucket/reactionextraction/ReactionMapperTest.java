package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;

import dan2097.org.bitbucket.utility.IndigoHolder;

public class ReactionMapperTest {

	@Test
	public void trivialReactionTest(){
		Indigo indigo = IndigoHolder.getInstance();
		IndigoObject reaction = indigo.createReaction();
		reaction.addProduct(indigo.loadMolecule("C(C)O"));
		reaction.addReactant(indigo.loadMolecule("C(O)C"));
		ReactionMapper mapper = new ReactionMapper(reaction);
		assertEquals(true, mapper.mapReaction());
		assertEquals(true, mapper.allProductAtomsAreMapped());
	}
	
	@Test
	public void trivialNotAReactionTest(){
		Indigo indigo = IndigoHolder.getInstance();
		IndigoObject reaction = indigo.createReaction();
		reaction.addProduct(indigo.loadMolecule("[Pb]"));
		reaction.addReactant(indigo.loadMolecule("[Au]"));
		ReactionMapper mapper = new ReactionMapper(reaction);
		assertEquals(true, mapper.mapReaction());
		assertEquals(false, mapper.allProductAtomsAreMapped());
	}
	
	@Test
	public void reactionTest1(){
		Indigo indigo = IndigoHolder.getInstance();
		IndigoObject reaction = indigo.createReaction();
		reaction.addProduct(indigo.loadMolecule("c1ccccc1CCC"));
		reaction.addReactant(indigo.loadMolecule("c1ccccc1"));
		reaction.addReactant(indigo.loadMolecule("ClCCC"));
		reaction.addCatalyst(indigo.loadMolecule("[Al](Cl(Cl)Cl"));
		ReactionMapper mapper = new ReactionMapper(reaction);
		assertEquals(true, mapper.mapReaction());
		assertEquals(true, mapper.allProductAtomsAreMapped());
	}
	
	@Test
	public void reactionTest2(){
		Indigo indigo = IndigoHolder.getInstance();
		IndigoObject reaction = indigo.createReaction();
		IndigoObject product = indigo.loadMolecule("O=C1CCC[C@]2([H])C3C=CC(C3)[C@@]21[H]");
		product.foldHydrogens();//Hopefully this can be done within the reaction in later versions
		reaction.addProduct(product);
		reaction.addReactant(indigo.loadMolecule("C1C=CC=C1"));
		reaction.addReactant(indigo.loadMolecule("O=C1CCCC=C1"));
		reaction.addCatalyst(indigo.loadMolecule("[Nb+5].[Cl-].[Cl-].[Cl-].[Cl-].[Cl-]"));
		ReactionMapper mapper = new ReactionMapper(reaction);
		assertEquals(true, mapper.mapReaction());
		assertEquals(true, mapper.allProductAtomsAreMapped());
	}
}

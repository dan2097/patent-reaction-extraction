package dan2097.org.bitbucket.reactionextraction;

import org.apache.log4j.Logger;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;

public class ReactionMapper {
	private static Logger LOG = Logger.getLogger(ReactionMapper.class);
	private final IndigoObject reaction;
	
	public ReactionMapper(IndigoObject indigoReaction) {
		this.reaction = indigoReaction;
	}
	
	/**
	 * Attempts to perform AAM (atom-to-atom) mapping.
	 * Returns false if for whatever reason this fails.
	 * Unexpected outcomes will be logged
	 * @return
	 */
	public boolean mapReaction() {
		if (reaction.countProducts()==0 || reaction.countReactants()==0){
			return false;
		}
		for (IndigoObject m: reaction.iterateMolecules()){
			for (IndigoObject b: m.iterateBonds()){
				reaction.setReactingCenter(b, Indigo.RC_UNCHANGED | Indigo.RC_ORDER_CHANGED | Indigo.RC_MADE_OR_BROKEN);
			}
		}
		try{
			reaction.automap("discard ignore_charges ignore_valence");
		}
		catch (Exception e) {
			LOG.error("Indigo reaction mapping failed", e);
			return false;
		}
		finally {
			for (IndigoObject m: reaction.iterateMolecules()){
				for (IndigoObject b: m.iterateBonds()){
					reaction.setReactingCenter(b, Indigo.RC_UNCHANGED);
				}
			}
		}
		return true;
	}
	
	public boolean allProductAtomsAreMapped(){
		for (IndigoObject product : reaction.iterateProducts()) {
			for (IndigoObject atom : product.iterateAtoms()) {
				if(reaction.atomMappingNumber(atom) == 0){
					return false;
				}
			}
		}
		return true;
	}
}

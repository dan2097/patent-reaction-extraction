package dan2097.org.bitbucket.reactionextraction;

import org.apache.log4j.Logger;

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
		try{
			reaction.automap("discard");
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.debug("Indigo reaction mapping failed", e);
			return false;
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

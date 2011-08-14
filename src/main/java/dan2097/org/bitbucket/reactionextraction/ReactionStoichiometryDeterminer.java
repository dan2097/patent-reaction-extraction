package dan2097.org.bitbucket.reactionextraction;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;



public class ReactionStoichiometryDeterminer {
	private static Logger LOG = Logger.getLogger(ReactionStoichiometryDeterminer.class);
	private final static Pattern matchAmount = Pattern.compile("([mn\u00b5]|pico|nano|micro|milli)?mol[e]?[s]?");
	private final Reaction reaction;

	ReactionStoichiometryDeterminer(Reaction reaction) {
		this.reaction = reaction;
	}
	
	
	/**
	 * Uses the amount(mols) of reactants to give the correct stoichiometry
	 */
	void processReactionStoichiometry() {
		Map<Chemical, Double> reactantToAmount = new HashMap<Chemical, Double>();
		for (Chemical reactant : reaction.getReactants()) {
			if (reactant.getAmountValue()!=null){
				try{
					double amount = Double.parseDouble(reactant.getAmountValue());
					String units = reactant.getAmountUnits();
					Matcher m = matchAmount.matcher(units);
					if (m.matches()){
						String scale = m.group(1);
						if (scale != null){
							if (scale.equalsIgnoreCase("m") || scale.equalsIgnoreCase("milli")){
								amount = amount/1000;
							}
							if (scale.equalsIgnoreCase("\u00b5") || scale.equalsIgnoreCase("micro")){
								amount = amount/1000000;
							}
							if (scale.equalsIgnoreCase("n") || scale.equalsIgnoreCase("nano")){
								amount = amount/1000000000;
							}
							if (scale.equalsIgnoreCase("pico")){
								amount = amount/1000000000000d;	
							}
						}
						reactantToAmount.put(reactant, amount);
					}
					
				}
				catch (NumberFormatException e) {
					LOG.trace(reactant.getAmountValue() +" was not a valid double");
				}
			}
		}
		if (reactantToAmount.keySet().size()>1){
			double lowestAmount = Double.MAX_VALUE;
			for (double amountInMols: reactantToAmount.values()) {
				if (amountInMols < lowestAmount){
					lowestAmount = amountInMols;
				}
			}
			for (Entry<Chemical, Double> entries: reactantToAmount.entrySet()) {
				double stoichiometry = 1;
				if (entries.getValue() != lowestAmount){
					stoichiometry = entries.getValue()/lowestAmount;
				}
				entries.getKey().setStoichiometry(stoichiometry);
			}
		}
	}
}

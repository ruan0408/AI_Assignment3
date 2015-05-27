
public class AgentState {

	boolean axe;
	boolean boat;
	boolean gold;
	int dynamites;
	
	public AgentState(boolean axe, boolean boat, boolean gold, int dynammites) {
		this.axe = axe;
		this.boat = boat;
		this.gold = gold;
		this.dynamites = dynammites;
	}
	
	public AgentState(AgentState prototype) {
		axe = prototype.axe;
		boat = prototype.boat;
		gold = prototype.gold;
		dynamites = prototype.dynamites;
	}
}

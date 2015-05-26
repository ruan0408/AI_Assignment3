
public class AgentState {

	boolean axe;
	boolean boat;
	int dynamites;
	
	public AgentState(boolean axe, boolean boat, int dynammites) {
		this.axe = axe;
		this.boat = boat;
		this.dynamites = dynammites;
	}
	
	public AgentState(AgentState prototype) {
		axe = prototype.axe;
		boat = prototype.boat;
		dynamites = prototype.dynamites;
	}
}

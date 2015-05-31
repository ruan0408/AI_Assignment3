/*
By:

Gervasio Protasio dos Santos Neto - 5050769
Ruan De Menezes Costa - 5050761
*/


import java.util.Comparator;


public class StateComparator implements Comparator<MyState> {
    /*Compares two states given their F values. This is necessary for
     the Priority Queue used by A* to know which one has highest
     priority.*/
	@Override
	public int compare(MyState o1, MyState o2) {
		return o1.getFValue() - o2.getFValue();
	}

}

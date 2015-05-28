import java.util.Comparator;


public class StateComparator implements Comparator<MyState> {

	@Override
	public int compare(MyState o1, MyState o2) {
		return o1.getFValue() - o2.getFValue();
	}

}

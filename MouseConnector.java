import java.util.Vector;
import java.util.Hashtable;

public class MouseConnector extends DBNconnector {
	Hashtable loc;
	{name = new String("mouse");}
		
	public void ready(DrawSpace ds) {
		super.ready(ds);
		loc = ds.loc;
	}
	
	public int get(Vector numbers) {
		int which;
		
		if(numbers == null || numbers.isEmpty()) return 0;
		which = ((Integer)numbers.elementAt(0)).intValue();
		if(which < 1 || which > 3) return 0;
		
		Integer key = new Integer(which);
		if(!loc.containsKey(key))
			loc.put(key, new Integer(0));
		return ((Integer) loc.get(key)).intValue();
	}
}

import java.util.Vector;
import java.util.Hashtable;

public class random extends DBNconnector {
	Hashtable loc;
	{name = new String("random");}
		
	public int get(Vector numbers) {
		int which, k;
                double r;
		
		if(numbers == null || numbers.isEmpty()) return 0;
		which = ((Integer)numbers.elementAt(0)).intValue()+1;
 		r = Math.random();
                k = (int)(1000 * r); 
                return (k%which);
	}
}

import java.util.Vector;
import java.util.Hashtable;

public class DBNconnector {
	public DrawSpace ds;
	public String name=null;

	public void ready(DrawSpace ds) {
		this.ds = ds;
		this.ds.connectors.put(this.name, this);
	}

	public void set(Vector numbers, int newVal) {
		// default implementation, do nothing
	}

	public int get(Vector numbers) {
		// default implementation, return 0
		return 0;
	}
}


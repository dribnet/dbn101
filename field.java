import java.util.Vector;
import java.util.Hashtable;

public class field extends DBNconnector {
	{name = new String("field");}
		
	public int get(Vector numbers) {
		int l, r, t, b;
                int a, i, j;
                int col = ds.getPen();
		
		if(numbers == null || numbers.size()<4) return 0;
	 	l = ((Integer)numbers.elementAt(0)).intValue();
	 	b = ((Integer)numbers.elementAt(1)).intValue();
	 	r = ((Integer)numbers.elementAt(2)).intValue();
	 	t = ((Integer)numbers.elementAt(3)).intValue();
                
                if(l>r) {
                   a=l; l=r; r=a;
                }
                if(b>t) {
                   a=b; b=t; t=a;
                }

                for(i=l;i<=r;i++) 
                   for(j=b;j<t;j++) 
                      ds.setDot(i, j, col);

                return 0;
	}
}

import java.awt.*;
import java.util.Hashtable;
import java.util.Vector;
import java_cup.runtime.Symbol;

public class DrawSpace extends Panel
{
	Button butLoad, butPause, butResume;
	Scrollbar speedDial;
	TextArea ta;
	//public static String parseString;
	FreshPanel can;
	int pCount;
	byte[] pixels;
	byte penColor = 100;
	ParserThread pt = null;
	public Hashtable loc, key, connectors;
	boolean antialias = false;
	Label errorLabel;
		
	public void updateCan() {
		String ps;
		
		can.doBackground(false);
		errorLabel.setText("");
		if(pt!=null && pt.isAlive()) pt.stop();	
		ps = ta.getText() + "\n";
		ps = ps.toLowerCase();
		pt = new ParserThread(this, ps);
		pt.start();
		//can.loadString(parser.outputString);
		//can.repaint();
	}

	public void syntaxError(String errString) {
		can.doBackground(true);
		if(errorLabel!=null) errorLabel.setText(errString);
	}
	
	public DrawSpace(TextArea text) {
		GridBagLayout gc = new GridBagLayout();
				
		ta = text;
		connectors = new Hashtable();
		key = new Hashtable(30);
		loc = new Hashtable(10);

		MouseConnector mc = new MouseConnector();
		mc.ready(this);
		//connectors.put("mouse", loc);
		//connectors.put("key", key);
		
		this.setLayout(gc);

	    // Create a constraints object, and specify some default values
    	GridBagConstraints c = new GridBagConstraints();
    	c.fill = GridBagConstraints.BOTH;  // components grow in both dimensions
    	c.insets = new Insets(5,5,5,5);    // 5-pixel margins on all sides
    	gc.setConstraints(this, c);


    	// Create and add a bunch of buttons, specifying different grid
    	// position, and size for each.
    	// Give the first button a resize weight of 1.0 and all others
    	// a weight of 0.0.  The first button will get all extra space.
    	c.gridx = 0; c.gridy = 0; c.gridwidth = 4; c.gridheight=4;
    	c.weightx = c.weighty = 1.0;
    	can = new FreshPanel();
    	this.add(can);
    	gc.setConstraints(can, c);

	    c.gridx = 0; c.gridy = 4; c.gridwidth = 1; c.gridheight=1;
	    c.weightx = c.weighty = 0.0;
	    butLoad = new Button("Get new program");
	    this.add(butLoad);
 		gc.setConstraints(butLoad, c);
 		
 	    c.gridx = 1; c.gridy = 4; c.gridwidth = 3; c.gridheight=1;
	    c.weightx = c.weighty = 0.0;
	    errorLabel = new Label("(errors go here)");
	    this.add(errorLabel);
 		gc.setConstraints(errorLabel, c);
	    
	    /*
		speedDial = new Scrollbar(Scrollbar.HORIZONTAL, 0, 10, 0, 100);
	    this.add(speedDial);
 		gc.setConstraints(speedDial, c);
		*/
	
	    c.gridx = 4; c.gridy = 0; c.gridwidth = 1; c.gridheight=1;
	    c.weightx = c.weighty = 0.0;
	    butPause = new Button("Stop");
	    this.add(butPause);
 		gc.setConstraints(butPause, c);

	    c.gridx = 4; c.gridy = 1; c.gridwidth = 1; c.gridheight=1;
	    c.weightx = c.weighty = 0.0;
	    butResume = new Button("Dummy");
	    this.add(butResume);
 		gc.setConstraints(butResume, c);

	    /*
	    c.gridx = 0; c.gridy = 5; c.gridwidth = 4; c.gridheight=1;
	    c.weightx = c.weighty = 0.0;
	    errorLabel = new Label("(errors go here)");
	    this.add(errorLabel);
 		gc.setConstraints(errorLabel, c);
		*/
		
		can.init(loc, key);
		pCount = can.pixelCount;
		pixels = can.pixels;
		can.start();		
		updateCan();
	}
	
	public void resume() {
/*
		if(pt!=null && pt.isAlive()) {
			try {pt.start();}
			catch (Exception e) {System.out.println("Resuming problem: " + e);}
		}
		can.resume();
*/
	}
	
	public void pause() {
		if(pt!=null && pt.isAlive()) {
			try {pt.stop();}
			catch (Exception e) {System.out.println("Pausing problem: " + e);}
		}
		can.pause();
	}
	
	public void destroy() {
		if(pt!=null && pt.isAlive()) pt.stop();	
		can.stop();
	}
	
	public boolean action(Event e, Object arg) {
		if(e.target == butLoad) {
			updateCan();
			return true;
		}
		else if(e.target == butPause) {
			pause();
			return true;
		}
		else if(e.target == butResume) {
			resume();
			return true;
		}
		else return false;
	}
	
	/*
	public boolean handleEvent(Event e) {
		if(e.target == speedDial) {
			if(pt != null) {
				pt.parser_obj.delay = ((Integer)e.arg).intValue();
				System.out.println("delay is now " + pt.parser_obj.delay);
			}
			return true;
		}
		else return false;
	}
	*/
	
	public void setPaper(int val) {
		byte bVal;
		if(val > 100) val = 100;
		else if(val < 0) val = 0;
		bVal = (byte) val;
		for(int i=0;i<pCount;i++) pixels[i] = bVal;
		//can.setPaper(val);
		//can.repaint();
	}

	public void setPen(int val) {
		if(val > 100) val = 100;
		else if(val < 0) val = 0;
		penColor = (byte)val;
		//can.setPen(val);
		//can.repaint();
	}
	
	public byte getPen() {
		return penColor;
	}
	
	public boolean hasConnector(String conName) {
		return(connectors.containsKey(conName));
	}
	
	public void setConnector(String conName, Vector numbers, int conValue) {
		if(!connectors.containsKey(conName)) return;
		DBNconnector dc = (DBNconnector)connectors.get(conName);
		dc.set(numbers, conValue);
	}

	public int getConnector(String conName, Vector numbers) {
		if(!connectors.containsKey(conName)) return 0;
		DBNconnector dc = (DBNconnector)connectors.get(conName);
		return dc.get(numbers);
	}

	public void doLoad(String className) {
		Class cl;
		Object o;
		DBNconnector dc;
		
		try {
			cl = Class.forName(className);
			o = cl.newInstance();
			if(o instanceof DBNconnector) {
				dc = (DBNconnector)o;
				dc.ready(this);
			}
		}
		catch(Exception e) {
			System.out.println("problem loading " + className + ": " + e);
		}
	}
				

	/*
	void checkForConnector(String conName) {
		if(!connectors.containsKey(conName)) {
			Hashtable h = new Hashtable(5);
			connectors.put(conName, h);
		}
	}
	
	public void setConnector(String conName, int which, int conValue) {
		Hashtable h;
		
		//System.out.println("setConnector("+conName+","+which+","+conValue+") called");
		checkForConnector(conName);
		h = (Hashtable)connectors.get(conName);
		Integer newKey = new Integer(which);
		Integer newValue = new Integer(conValue);
		h.put(newKey, newValue);
	}

	public int getConnector(String conName, int which) {
		Hashtable h;
		
		//System.out.println("getConnector("+conName+","+which+") called");
		checkForConnector(conName);
		h = (Hashtable)connectors.get(conName);
		Integer key = new Integer(which);
		if(!h.containsKey(key))
			h.put(key, new Integer(0));
		return ((Integer) h.get(key)).intValue();
	}
	*/	
		
	public void setDot(int x, int y, int val) {
		//System.out.println("setDot("+x+","+y+","+val+") called");
		int checkX, checkY, checkVal;
		
		if(x<0 || x>100 || y<0 || y>100) return;
		if(val < 0) checkVal = 0;
		else if (val > 100) checkVal = 100;
		else checkVal = val;
		/* who wants this? nobody, that's who
		if(x < 0) checkX = 0;
		else if(x>100) checkX = 100;
		else checkX = x;
		if(y < 0) checkY = 0;
		else if(y>100) checkY = 100;
		else checkY = y;
		*/
		pixels[(100-y)*101 + x] = (byte)checkVal;
	}
	
	void intensifyPixel(int x, int y, float dist) {
		int oldVal, newVal, val, index;
		
		if(x<0 || x>100 || y<0 || y>100) return;
		if(dist < 0) dist = 0.0f - dist;
		if(dist > 1.0f) return;
		index = (100-y)*101 + x;
		val = (int)(pixels[index]*dist + penColor*(1.0f-dist));
		//val = (int)(penColor*(1.0f-dist));
		
		if(val > 100) val = 100;
		else if(val < 0) val = 0;

		pixels[index] = (byte)val;
	}
	
	public void drawLine(int ox1, int oy1, int ox2, int oy2) {
		int dx, dy, incrE, incrNE, d, x, y, twoV, sdx=0, sdy=0;
		float invDenom, twoDX, scratch, slope;
		int x1, x2, y1, y2, incy=0, incx=0, which;
		boolean backwards = false, slopeDown = false;

		/* first do horizontal line */
		if(ox1==ox2) {
			x = ox1;
			if(oy1 < oy2) { y1 = oy1; y2 = oy2; }
			else { y1 = oy2; y2 = oy1; }
			for(y=y1;y<=y2;y++) {
				intensifyPixel(x, y, 0);
			}
			return;
		}				
		slope = (float)(oy2 - oy1)/(float)(ox2 - ox1);
		/* check for which 1, 0 <= slope <= 1 */
		if(slope >= 0 && slope <= 1) which = 1;
		else if(slope > 1) which = 2;
		else if(slope < -1) which = 3;
		else which=4;
		if(((which==1 || which==2 ||which==4) && ox1 > ox2) || ((which==3) && ox1 < ox2)) {
			x1 = ox2;
			x2 = ox1;
			y1 = oy2;
			y2 = oy1;
		}
		else {
			x1 = ox1;
			y1 = oy1;
			x2 = ox2;
			y2 = oy2;
		}
		dx = x2 - x1;
		dy = y2 - y1;
		if(which==1) {
			sdx = dx;
			d = 2 * dy - dx;
			incy = 1;
			incrE = 2*dy;
			incrNE = 2*(dy-dx);
		}
		else if(which==2) {
			sdy = dy;
			d = 2 * dx - dy;
			incx = 1;
			incrE = 2*dx;
			incrNE = 2*(dx-dy);
		}
		else if(which==3) {
			sdy = -dy;
			d = -2 * dx - dy;
			incx = -1;
			incrE = -2*dx;
			incrNE = -2*(dx+dy);
		}
		else /*if(which == 4)*/ {
			sdx = -dx;
			d = -2 * dy - dx;
			incy = -1;
			incrE = -2*dy;
			incrNE = -2*(dy+dx);
		}
		if(which==1 || which==4) {
			twoV = 0;
			invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
			twoDX = 2 * sdx * invDenom;
			x = x1;
			y = y1;
			if(antialias) {
				intensifyPixel(x, y, 0);
				intensifyPixel(x, y+1, twoDX);
				intensifyPixel(x, y-1, twoDX);
			}
			else
				intensifyPixel(x, y, 0);				
			while(x < x2) {
				if(d<0) {
					twoV = d + dx;
					d += incrE;
					++x;
				}
				else {
					twoV = d - dx;
					d += incrNE;
					++x;
					y+=incy;
				}
				scratch = twoV * invDenom;
				if(antialias) {
					intensifyPixel(x, y, scratch);
					intensifyPixel(x, y+1, twoDX - scratch);
					intensifyPixel(x, y-1, twoDX + scratch);
				}
				else
					intensifyPixel(x, y, 0);				
			}
		}
		else {
			twoV = 0;
			invDenom = 1.0f / (2.0f * (float)Math.sqrt(dx*dx + dy*dy));
			twoDX = 2 * sdy * invDenom;
			x = x1;
			y = y1;
			if(antialias) {
				intensifyPixel(x, y, 0);
				intensifyPixel(x+1, y, twoDX);
				intensifyPixel(x-1, y, twoDX);
			}
			else 
				intensifyPixel(x, y, 0);			
			while(y < y2) {
				if(d<0) {
					twoV = d + dy;
					d += incrE;
					++y;
				}
				else {
					twoV = d - dy;
					d += incrNE;
					++y;
					x+=incx;
				}
				scratch = twoV * invDenom;
				if(antialias) {
					intensifyPixel(x, y, scratch);
					intensifyPixel(x+1, y, twoDX - scratch);
					intensifyPixel(x-1, y, twoDX + scratch);
				}
				else 
					intensifyPixel(x, y, 0);			
			}
		}
		
					
//		setDot(x1, y1, penColor);
//		setDot(x2, y2, penColor);
		
//		pixels[(100-checkY)*101 + checkX] = (byte)checkVal;
	}

	public void setAlias() {
		antialias = false;
	}
	
	public void setAntialias() {
		antialias = true;
	}

	public int getDot(int x, int y) {
		//System.out.println("getDot("+x+","+y+") called");
		int checkX, checkY;
		if(x < 0) checkX = 0;
		else if(x>100) checkX = 100;
		else checkX = x;
		if(y < 0) checkY = 0;
		else if(y>100) checkY = 100;
		else checkY = y;
		return (int)pixels[(100-checkY)*101 + checkX];
	}
}

class ParserThread extends Thread {

 	static boolean do_debug_parse = false;
	public parser parser_obj = new parser();
	Symbol parse_tree;
	DrawSpace ds;
	String ps;
   
   public ParserThread(DrawSpace ds, String ps) {
      this.ds = ds;
      this.ps = ps;
   }

   public void run() {      
		parser_obj = new parser();
		parse_tree = null;
		parser_obj.setParseString(ps);
		parser_obj.setDrawSpace(ds);
		//try {parse_tree = parser_obj.debug_parse();}
		try {parse_tree = parser_obj.parse();}
		catch (Exception e) {
			System.out.println("Parsin' err: " +e);
		}
	}
}


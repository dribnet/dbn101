import java.net.*;
import java.awt.*;
import java.awt.image.*;
import java.applet.Applet;
import java.util.*;

public class FreshPanel extends Canvas implements Runnable {
	public byte[ ]			pixels;
	Image					drawIm;
	Dimension				drawD, offD;
	int						width = 101;
	int						height = 101;
	public int				pixelCount, curPos=0;
	int						x, y;
	long					delay = 33;
	final int				ssize = 40;
	boolean					running = true;
	Hashtable				loc, key;
	IndexColorModel 		greyScale;
	Thread					thread;
	boolean 				syntaxError=false, doPosition=true;

	public void init(Hashtable loc, Hashtable key) {
		offD = null;
		byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		int i;
		
		pixelCount = width * height;
		pixels = new byte[ pixelCount ];

		byte value = 30;
		for(i = 0; i < pixelCount; ++ i ) {
			pixels[ i ] = value;
		}

		for(i=0;i<256;i++) {
			if(i<101) 
				r[i] = g[i] = b[i] = (byte)((100-i) * 255 / 100);
			else
				r[i] = g[i] = b[i] = (byte)0;
		}
		greyScale = new IndexColorModel(8, 256, r, g, b);

		drawD = new Dimension(101,101);
        drawIm = createImage(new MemoryImageSource(width,height, greyScale, pixels,0,width));

		this.loc = loc;
		this.key = key;
	}
	
	public void updateLoc(int atx, int aty) {
		Integer xVal = new Integer(atx-x);
		Integer yVal = new Integer(100 - (aty-y));
		
		loc.put(new Integer(1), xVal);
		loc.put(new Integer(2), yVal);
	}

	public void updateState(boolean isDown) {
		Integer state;
		if(isDown) state=new Integer(100);
		else state=new Integer(0);
		loc.put(new Integer(3), state);
	}

	public boolean mouseMove(Event evt, int x, int y) {
		updateLoc(x, y);
		return(true);
	}

	public boolean mouseDrag(Event evt, int x, int y) {
		updateLoc(x, y);
		return(true);
	}
	
	public boolean mouseDown(Event evt, int x, int y) {
		updateState(true);
		return(true);
	}
	
	public boolean mouseUp(Event evt, int x, int y) {
		updateState(false);
		return(true);
	}

	public void start( ) {
		thread = new Thread( this );
		running = true;
		thread.start( );
	}

	public void stop( ) {
//		System.out.println("Destroying can");
		thread.stop( );
		thread = null;
	}

	public void pause() {
//		System.out.println("Pausing can");
		if(running) {
//			System.out.println("NOW");
//			try {thread.suspend();}
//			catch (Exception e) { System.out.println("Problem pausing: " +e);}
			running = false;
		}
	}

	public void resume() {
//		System.out.println("Resuming can");
		if(!running) {
//			System.out.println("NOW");
//			try {thread.resume(); }
//			catch (Exception e) { System.out.println("Problem resuming: " +e);}
			running = true;
		}
	}
		
	public void position(Graphics g) {
		int i;
		
		if(!syntaxError) g.setColor(new Color(100, 100, 150));
		else g.setColor(new Color(150, 100, 100));
		g.fillRect(0, 0, offD.width, offD.height);
		x = (offD.width - drawD.width)/2;
		y = (offD.height - drawD.height)/2;
		g.setColor(Color.white);
		for(i=0;i<11;i++) {
			g.drawLine(x+i*10-1, y+100, x+i*10-1, y+100+2);
			g.drawLine(x-2, y+i*10-1, x, y+i*10-1);
		}
	}

	public void doBackground(boolean isError) {
		syntaxError = isError;
		doPosition = true;
	}

	public void paint(Graphics g) { 
		update(g); 
	}

	public void update( Graphics g ) {
		Dimension d = size();
		
		if((offD == null) || (d.width != offD.width) || (d.height != offD.height)) {
			offD = d;
			position(g);
		}
		if(doPosition) {
			position(g);
			doPosition = false;
		}
        drawIm.flush();
		g.drawImage(drawIm, x, y, this);
	}

	public void run( ) {
		Thread		thread = Thread.currentThread( );
		int i;
		byte color;
		int where;
		

		for( ; ; ) {
//			delay = curPos/50;
			if(!running) {
				try {thread.sleep( 500 );} 
				catch( InterruptedException e ) {};
			}				
			if( delay != 0 ) {
				try {thread.sleep( delay );} 
				catch( InterruptedException e ) {};
			}
			repaint();			
			//source.editPixelsEnd( left, top, right - left, bottom - top );
			curPos = (curPos+pixelCount-1)%pixelCount;
			
		}
	}
}

import java.net.*;
import java.awt.*;
import java.awt.image.*;
import java.applet.Applet;
import java.util.*;

import gad.awt.*;
import gad.awt.image.*;
import gad.util.*;

public class LotionPanel extends Canvas implements Runnable {
	public byte[ ]			pixels;
	MemoryAnimationSource	source;
	Image					drawIm;
	Dimension				drawD, offD;
	SharedCondition			sync;
	Thread					thread;

	int						width = 101;
	int						height = 101;
	public int				pixelCount, curPos=0;
	int						x, y;
	int						priority = 3;
	long					delay = 1;
	final int				ssize = 40;
	boolean					lockstep = false;
	boolean					running = true;
	Hashtable				loc, key;

	public void init(Hashtable loc, Hashtable key) {
		lockstep=false;
		delay=0;
		priority = 3;
		offD = null;
		byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		int i;
		IndexColorModel greyScale;
		
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

		source = new MemoryAnimationSource( width, height, greyScale, pixels, 0, width, false );
		drawIm = createImage( source );
		sync = new SharedCondition( );
		drawD = new Dimension(101,101);
		sync = new SharedCondition( );
		
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
		thread.start( );
	}

	public void stop( ) {
		thread.stop( );
		thread = null;
	}


	public boolean oldMouseDown( Event event, int x, int y ) {
		running = ! running;

		if( running )
			sync.put( );
		
		return true;
	}

	public void position(Graphics g) {
		int i;
		
		g.setColor(new Color(100, 100, 150));
		g.fillRect(0, 0, offD.width, offD.height);
		x = (offD.width - drawD.width)/2;
		y = (offD.height - drawD.height)/2;
		g.setColor(Color.white);
		for(i=0;i<11;i++) {
			g.drawLine(x+i*10-1, y+100, x+i*10-1, y+100+2);
			g.drawLine(x-2, y+i*10-1, x, y+i*10-1);
		}
	}

	public void paint(Graphics g) {
		Dimension d = size();
		
		if((offD == null) || (d.width != offD.width) || (d.height != offD.height)) {
			offD = d;
			position(g);
		}
		if (drawIm != null) 
			g.drawImage(drawIm, x, y, this);
		sync.put();
	}

	public void update( Graphics g ) {
		paint( g );
	}

	public void run( ) {
		Thread		thread = Thread.currentThread( );
		int i;
		byte color;
		int where;

		//thread.setPriority( priority );

		for( ; ; ) {
//			delay = curPos/50;
			if( delay != 0 )
				try {thread.sleep( delay );} catch( InterruptedException e ) {};

			if( lockstep )
				sync.get( );

			source.editPixelsBegin( );

			pixels[100] ^= 0x3F;

			sync.clear( );
			//source.editPixelsEnd( left, top, right - left, bottom - top );
			source.editPixelsEnd();
			curPos = (curPos+pixelCount-1)%pixelCount;
			
		}
	}
}

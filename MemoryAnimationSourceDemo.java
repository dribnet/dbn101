import java.net.*;
import java.awt.*;
import java.awt.image.*;
import java.applet.Applet;
import java.util.*;

import gad.awt.*;
import gad.awt.image.*;
import gad.util.*;

public class MemoryAnimationSourceDemo extends Applet implements Runnable {
	int[ ]					pixels;
	MemoryAnimationSource	source;
	Image					image;
	SharedCondition			sync;
	Thread					thread;

	int						width = 50;
	int						height = 50;
	int						priority = 3;
	long					delay = 0;
	final int				ssize = 10;
	boolean					lockstep = false;

	boolean					running = true;

	public void init( ) {
		try {
			width = Integer.parseInt( getParameter( "width" ));
		} catch( NumberFormatException e ) {
			width = 50;
		}

		try {
			height = Integer.parseInt( getParameter( "height" ));
		} catch( NumberFormatException e ) {
			height = 50;
		}

		try {
			priority = Integer.parseInt( getParameter( "priority" ));
		} catch( NumberFormatException e ) {
			priority = 3;
		}

		try {
			delay = Long.parseLong( getParameter( "delay" ));
		} catch( NumberFormatException e ) {
			delay = 0;
		}

		lockstep = "true".equals( getParameter( "lockstep" ));

		int count = width * height;
		pixels = new int[ count ];

		int value = getBackground( ).getRGB( ) | 0xFF000000;
		for( int i = 0; i < count; ++ i )
			pixels[ i ] = value;

		source = new MemoryAnimationSource( width, height, ColorModel.getRGBdefault( ), pixels, 0, width, false );
		image = createImage( source );
		sync = new SharedCondition( );
	}

	public void start( ) {
		thread = new Thread( this );
		thread.start( );
	}

	public void stop( ) {
		thread.stop( );
		thread = null;
	}


	public boolean mouseDown( Event event, int x, int y ) {
		running = ! running;

		if( running )
			sync.put( );
		
		return true;
	}


	public void paint( Graphics g ) {
		if( image != null )
			g.drawImage( image, 0, 0, this );

		sync.put( );
	}

	public void update( Graphics g ) {
		paint( g );
	}


	public void run( ) {
		Random		r = new Random( );
		Thread		thread = Thread.currentThread( );

		thread.setPriority( priority );

		for( ; ; ) {
			if( delay != 0 )
				try {thread.sleep( delay );} catch( InterruptedException e ) {};

			if( lockstep )
				sync.get( );

			int color = r.nextInt( );

			int alpha = 0x80; // ( color >> 24 ) & 0xFF;
			int notalpha = 0xFF - alpha;

			int red = (( color >> 16 ) & 0xFF ) * alpha + 0x80;
			int green = (( color >> 8 ) & 0xFF ) * alpha + 0x80;
			int blue = ( color & 0xFF ) * alpha + 0x80;

			int x = ( r.nextInt( ) & 0x7FFFFFFF ) % width;
			int y = ( r.nextInt( ) & 0x7FFFFFFF ) % height;
			
			int top = y < ssize ? 0 : y - ssize;
			int bottom = y > height - ssize ? height : y + ssize;
			int left = x < ssize ? 0 : x - ssize;
			int right = x > width - ssize ? width : x + ssize;

			source.editPixelsBegin( );

			int row = width * top + left;
			for( int i = top; i < bottom; ++ i ) {
				int pixel = row;
				for( int j = left; j < right; ++ j ) {
					int value = pixels[ pixel ];
					pixels[ pixel ] =
						((((( value >> 16 ) & 0xFF ) * notalpha + red ) >> 8 ) << 16 )
						| ((((( value >> 8 ) & 0xFF ) * notalpha + green ) >> 8 ) << 8 )
						| ((( value & 0xFF ) * notalpha + blue ) >> 8 )
						| 0xFF000000;
					pixel ++;
				}
				row += width;
			}

			sync.clear( );
			source.editPixelsEnd( left, top, right - left, bottom - top );
			//source.editPixelsEnd();
		}
	}
}

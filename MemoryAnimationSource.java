package gad.awt.image;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import gad.util.*;

class Producer implements Runnable {
	private MemoryAnimationSource	source;
	private ImageConsumer			consumer;
	private	ReadWriteWaitLock		lock;

	private boolean					remove = false;

	protected Producer(
		MemoryAnimationSource	source,
		ImageConsumer			consumer,
		ReadWriteWaitLock		lock ) {

		this.source		= source;
		this.consumer	= consumer;
		this.lock		= lock;
	}

	protected void stop( ) {
		remove = true;
	}

	public void run( ) {
		Thread.currentThread( ).setPriority( 2 );

		source.sendDimensions( consumer );
		if( remove ) return;
		source.sendProperties( consumer );
		if( remove ) return;
		source.sendColorModel( consumer );
		if( remove ) return;
		source.sendHints( consumer );
		if( remove ) return;

		lock.getReader( );
		source.sendPixels( consumer );
		for( ; ; ) {
			if( remove ) break;

			consumer.imageComplete( ImageConsumer.SINGLEFRAMEDONE );
			if( remove ) break;

			lock.readerWaitForWriter( );
			if( remove ) break;

			source.sendChangedPixels( consumer );
		}
		lock.putReader( );
	}
}

public class MemoryAnimationSource implements ImageProducer {
	private int						width;
	private int						height;
	private ColorModel				model;
	private byte[ ]					bytePixels;
	private int[ ]					intPixels;
	private int						offset;
	private int						scan;
	private Hashtable				properties;
	private boolean					updateAll;

	private int						hints;

	private ReadWriteWaitLock		lock = new ReadWriteWaitLock( );
	private Hashtable				producers = new Hashtable( );

	private int						changedX;
	private int						changedY;
	private int						changedWidth;
	private int						changedHeight;

	/*	The constructors for a MemoryAnimationSource are modeled after the
	**	constructors for a MemoryImageSource.  The pixels parameter may be
	**	either an int[ ] or a byte[ ].  The properties parameter is optional,
	**	with a new properties Hashtable constructed if none is provided.
	*/

	private void initialize(
		int			width,
		int			height,
		ColorModel	model,
		byte[ ]		bytePixels,
		int[ ]		intPixels,
		int			offset,
		int			scan,
		Hashtable	properties,
		boolean		updateAll ) {
		
		this.width		= width;
		this.height		= height;
		this.model		= model;
		this.bytePixels	= bytePixels;
		this.intPixels	= intPixels;
		this.offset		= offset;
		this.scan		= scan;
		this.properties	= properties == null ? new Hashtable( ) : properties;
		this.updateAll	= updateAll;

		hints = ImageConsumer.SINGLEPASS |
			( updateAll ? ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES
					: ImageConsumer.RANDOMPIXELORDER );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		byte[ ]		pixels,
		int			offset,
		int			scan ) {

		initialize( width, height, model, pixels, null, offset, scan, null, true );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		int[ ]		pixels,
		int			offset,
		int			scan ) {

		initialize( width, height, model, null, pixels, offset, scan, null, true );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		byte[ ]		pixels,
		int			offset,
		int			scan,
		Hashtable	properties ) {

		initialize( width, height, model, pixels, null, offset, scan, properties, true );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		int[ ]		pixels,
		int			offset,
		int			scan,
		Hashtable	properties ) {

		initialize( width, height, model, null, pixels, offset, scan, properties, true );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		byte[ ]		pixels,
		int			offset,
		int			scan,
		boolean		updateAll ) {

		initialize( width, height, model, pixels, null, offset, scan, null, updateAll );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		int[ ]		pixels,
		int			offset,
		int			scan,
		boolean		updateAll ) {

		initialize( width, height, model, null, pixels, offset, scan, null, updateAll );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		byte[ ]		pixels,
		int			offset,
		int			scan,
		Hashtable	properties,
		boolean		updateAll ) {

		initialize( width, height, model, pixels, null, offset, scan, properties, updateAll );
	}

	public MemoryAnimationSource(
		int			width,
		int			height,
		ColorModel	model,
		int[ ]		pixels,
		int			offset,
		int			scan,
		Hashtable	properties,
		boolean		updateAll ) {

		initialize( width, height, model, null, pixels, offset, scan, properties, updateAll );
	}




	public void editPixelsBegin( ) {
		lock.getWriter( );
	}

	public void editPixelsEnd( ) {
		editPixelsEnd( 0, 0, width, height );
	}

	public void editPixelsEnd( int changedX, int changedY, int changedWidth, int changedHeight ) {
		if( updateAll ) {
			this.changedX = 0;
			this.changedY = 0;
			this.changedWidth = width;
			this.changedHeight = height;
		} else {
			this.changedX = changedX;
			this.changedY = changedY;
			this.changedWidth = changedWidth;
			this.changedHeight = changedHeight;
		}

		lock.putWriter( );
	}

	public void replacePixels( byte[ ] pixels ) {
		editPixelsBegin( );

		bytePixels = pixels;
		intPixels = null;

		editPixelsEnd( );
	}

	public void replacePixels( int[ ] pixels ) {
		editPixelsBegin( );

		bytePixels = null;
		intPixels = pixels;

		editPixelsEnd( );
	}




	public synchronized void addConsumer( ImageConsumer consumer ) {
		Producer produce = ( Producer ) producers.get( consumer );
		if( produce != null ) produce.stop( );

		produce = new Producer( this, consumer, lock );
		producers.put( consumer, produce );
		Thread thread = new Thread( produce );
		thread.start( );
	}

	public synchronized boolean isConsumer( ImageConsumer consumer ) {
		return producers.containsKey( consumer );
	}

	public synchronized void removeConsumer( ImageConsumer consumer ) {
		Producer produce = ( Producer ) producers.get( consumer );
		produce.stop( );
		producers.remove( consumer );
	}

	public void startProduction( ImageConsumer consumer ) {
		addConsumer( consumer );
	}

	public void requestTopDownLeftRightResend( ImageConsumer consumer ) {
	}




	protected void sendDimensions( ImageConsumer consumer ) {
		consumer.setDimensions( width, height );
	}

	protected void sendProperties( ImageConsumer consumer ) {
		consumer.setProperties( properties );
	}

	protected void sendColorModel( ImageConsumer consumer ) {
		consumer.setColorModel( model );
	}

	protected void sendHints( ImageConsumer consumer ) {
		consumer.setHints( hints );
	}

	protected void sendPixels( ImageConsumer consumer ) {
		if( bytePixels != null )
			consumer.setPixels( 0, 0, width, height, model, bytePixels, offset, scan );
		else
			consumer.setPixels( 0, 0, width, height, model, intPixels, offset, scan );
	}

	protected void sendChangedPixels( ImageConsumer consumer ) {
		if( bytePixels != null )
			consumer.setPixels(
				changedX,
				changedY,
				changedWidth,
				changedHeight,
				model,
				bytePixels,
				offset + changedX + changedY * scan,
				scan );
		else
			consumer.setPixels(
				changedX,
				changedY,
				changedWidth,
				changedHeight,
				model,
				intPixels,
				offset + changedX + changedY * scan,
				scan );
	}
}

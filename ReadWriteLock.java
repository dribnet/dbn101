package gad.util;

//	ReadWriteLock provides synchronization methods which prevent
//	an object from having more than one writer, or both a writer and
//	a reader, simultaneously.  The writer calls getWriter before it
//	modifies the protected object, and putWriter when done.  The reader
//	calls getReader before reading the protected object, and putReader
//	when done.
//
//	The implementation is not optimal, in the sense that all waiting
//	readers and writers are notified when a writer is done; a better
//	solution would notify either one waiting writer or all waiting
//	readers.

public class ReadWriteLock {
	private boolean		hasWriter = false;
	private int			readerCount = 0;

	public synchronized void getWriter( ) {
		while( hasWriter || readerCount != 0 )
			try {wait( );} catch( InterruptedException e ) {};
		
		hasWriter = true;
	}

	public synchronized void putWriter( ) {
		hasWriter = false;

		notifyAll( );
	}

	public synchronized void getReader( ) {
		while( hasWriter )
			try {wait( );} catch( InterruptedException e ) {};
		
		readerCount ++;
	}

	public synchronized void putReader( ) {
		readerCount --;

		if( readerCount == 0 )
			notify( );
	}
}

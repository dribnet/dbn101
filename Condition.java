package gad.util;

//	An object of class Condition can be used for synchronization.
//
//	In the sequence
//
//		// Set up some information for another thread.
//
//		condition.clear( );
//
//		// Notify the other thread that the information is available.
//		// That thread should process the information, then make a
//		// call to condition.put( ).
//
//		condition.get( );
//
//		// overwrite the information.
//
//	it is gauranteed that the other thread has called condition.put( )
//	sometime after the call to condition.clear( ) returns, but before
//	the call to condition.get( ) returns.

public abstract class Condition {
	public final static boolean waiting = false;
	public final static boolean ready = true;

	public abstract void clear( );
	public abstract void get( );
	public abstract void put( );
}

package gad.util;

public class Lock {
	private boolean state;

	public final static boolean locked = false;
	public final static boolean unlocked = true;

	public Lock( ) {
		this( unlocked );
	}

	public Lock( boolean state ) {
		this.state = state;
	}

	public synchronized void get( ) {
		while( state == locked )
			try {wait( );} catch( InterruptedException e ) {};

		state = locked;
	}

	public synchronized void put( ) {
		state = unlocked;

		notify( );
	}
}

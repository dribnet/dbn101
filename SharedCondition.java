package gad.util;

//	A call to SharedCondition.put( ) allows all presently waiting
//	and all subsequent calls to SharedCondition.get( ) to succeed.
//	The method SharedCondition.clear( ) is used to reset the condition.
//
//	Examples of constuction:
//	    new SharedCondition( ); // waiting, by default
//	    new SharedCondition( Condition.ready );
//	    new SharedCondition( Condition.waiting );

public class SharedCondition extends Condition {
	private boolean state;

	public SharedCondition( boolean state ) {
		this.state = state;
	}

	public SharedCondition( ) {
		this( waiting );
	}

	public synchronized void clear( ) {
		state = waiting;
	}

	public synchronized void get( ) {
		while( state == waiting ) try {wait( );} catch( InterruptedException e ) {};
	}

	public synchronized void put( ) {
		state = ready;
		notifyAll( );
	}
}

package gad.util;

//	A call to ExclusiveCondition.put( ) allows exactly one call to
//	ExclusiveCondition.get( ) to return.
//
//	Examples of constuction:
//	    new ExclusiveCondition( ); // waiting, by default
//	    new ExclusiveCondition( Condition.ready );
//	    new ExclusiveCondition( Condition.waiting );

public class ExclusiveCondition extends Condition {
	private boolean state;

	public ExclusiveCondition( boolean state ) {
		this.state = state;
	}

	public ExclusiveCondition( ) {
		this( waiting );
	}

	public synchronized void clear( ) {
		state = waiting;
	}

	public synchronized void get( ) {
		while( state == waiting ) try {wait( );} catch( InterruptedException e ) {};
		state = waiting;
	}

	public synchronized void put( ) {
		state = ready;
		notify( );
	}
}

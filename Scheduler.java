package gad.util;

//	Scheduler can be used to force threads of the same priority to share
//	the processor.  If you wish to have several threads working at priority
//	level 2 which should share the processor equally in quanta of 1 ms, use:
//
//		scheduler = new Scheduler( 3, 1L );
//		scheduler.start( );
//
//	This causes the scheduler object to become runnable once every millisecond,
//	with priority 3.  It will interrupt any executing thread of lower priority.
//	The scheduler immediately goes back to sleep, and another lower priority
//	thread is allowed to run.

public class Scheduler extends Thread {
	long		quantum;

	public Scheduler( int priority, long quantum ) {
		super( "scheduler priority " + priority + "  quantum " + quantum );

		this.quantum = quantum;
		setPriority( priority );
		setDaemon( true );
	}

	public void run( ) {
		for( ; ; )
			try { sleep( quantum ); }
			catch( InterruptedException e ) {}
				;
	}
}

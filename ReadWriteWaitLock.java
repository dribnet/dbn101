package gad.util;

//	ReadWriteWaitLock provides the same getWriter, putWriter,
//	getReader, putReader methods as ReadWriteLock, but provides
//	the additional assurance that all waiting readers will be allowed
//	to run before any waiting writers.
//
//	An additional method readerWaitForWriter has semantics similar
//	to putReader followed by getReader, with a gaurantee that exactly
//	one writer will have been allowed to run before the method returns.
//
//	Invariants maintained by the synchronized methods:
//
//		readerQueue is in state Condition.waiting when there is a writer,
//			and in state Condition.ready when there is no writer.
//
//		nextReaderQueue is always in state Condition.waiting.
//
//		readerCount is the number of threads which have not finished
//			reading the current data.
//
//		nextReaderCount is the number of threads which have finished
//			reading the current data, and wish to read again after
//			a writer has finished.

public class ReadWriteWaitLock {
	private ExclusiveCondition	writerQueue = new ExclusiveCondition( Condition.ready );
	private SharedCondition		readerQueue = new SharedCondition( Condition.ready );
	private SharedCondition		nextReaderQueue = new SharedCondition( Condition.waiting );

	private int					readerCount = 0;
	private int					nextReaderCount = 0;

	public void getWriter( ) {
		//	Fight with other writers to be the next writer:
		writerQueue.get( );

		//	Get permission from the readers to actually write, and swap queues.
		readerSwapQueues( );
	}

	public void putWriter( ) {
		//	Allow another writer to try to write.
		//	( It will first wait for readerQueue to empty. )
		writerQueue.put( );

		//	Wake up all of the readers.
		readerQueue.put( );
	}

	public void getReader( ) {
		//	increment readerCount, so writers know there is a reader.
		readerEnqueue( );

		//	wait for any current writer to finish.
		readerQueue.get( );
	}

	public void putReader( ) {
		//	decrement readerCount, so writers know a reader has finished.
		readerDequeue( );
	}

	public void readerWaitForWriter( ) {
		//	putReader followed by a getReader on the next frame.
		//	readerRequeue returns the queue for the next frame.
		//	Can't do the get( ) inside readerQueue because of deadlock possibility.
		readerRequeue( ).get( );
	}


	private synchronized void readerEnqueue( ) {
		readerCount ++;
	}

	private synchronized void readerDequeue( ) {
		readerCount --;

		if( readerCount == 0 )
			notify( );
	}

	private synchronized SharedCondition readerRequeue( ) {
		readerCount --;
		nextReaderCount ++;

		//	during the call to notify below, a thread might wake up inside
		//	the getWriter method and call readerSwapQueues.  So we cache
		//	the queue that we want to wait on here, to be returned laeter.

		SharedCondition queue = nextReaderQueue;

		if( readerCount == 0 )
			notify( );
		
		return queue;
	}

	private synchronized void readerSwapQueues( ) {
		//	Wait until all readers are done before queueing next set.
		
		while( readerCount != 0 )
			try {wait( );} catch( InterruptedException e ) {};
		
		//	readerQueue should be empty now.  nextReaderQueue holds
		//	the readers who want the next frame, and nextReaderCount
		//	has the number of readers waiting on nextReaderQueue.
		
		//	We put these into readerQueue and readerCount; any readers
		//	added during the write will be added to these.  We also
		//	clear nextReaderQueue.  Since the new readerQueue used to be
		//	nextReaderQueue, and was cleared last time around, the readers
		//	are not going to be able to run until putWriter does a put
		//	on readerQueue.
		
		SharedCondition temp = readerQueue;
		readerQueue = nextReaderQueue;
		nextReaderQueue = temp;
		
		readerCount = nextReaderCount;
		nextReaderCount = 0;
		
		//	Condition.clear( ) never blocks, so is OK inside a
		//	synchronized method.
		nextReaderQueue.clear( );
	}
}


package lusc.net.sourceforge;
//
//  Waiter.java
//  Luscinia
//
//  Created by Robert Lachlan on 1/17/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

public class Waiter {

	private int _activeThreads = 0;
	protected int waitListSize =30;
 
	private boolean _started = false;

	synchronized public void waitDone() {
		try {
			while ( _activeThreads>0 ) {
				wait();
			}
		} catch ( InterruptedException e ) {
		}
	}

	synchronized public void waitBegin(){
		try {
			while ( !_started ) {
				wait();
			}
		} catch ( InterruptedException e ) {
		}
	}
	
	synchronized public void waitUnclog(int t){		
		waitListSize=t;
		try {
			while (waitListSize>30) {
				wait();
			}
		} catch ( InterruptedException e ) {
		}
	}

	synchronized public void workerBegin(){
		_activeThreads++;
		_started = true;
		notify();
	}

	synchronized public void workerEnd(){
		_activeThreads--;
		notify();
	}

	synchronized public void reset() {
		_activeThreads = 0;
	}

}
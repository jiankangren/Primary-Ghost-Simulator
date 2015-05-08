package com.independentstudy;


/**
 * A signal shows current event with some properties.
 * @author lian
 *
 */
public class SingleSignal {
	
	TempSingleTask  candidate ;    // next event happens with this task
	double  eventPoint;        // time point for next event
	int eventType;                 // a flag shows type of next event : 0: release a task;  1: finish a task
	
	
	public SingleSignal(){
		candidate = new TempSingleTask(0,0,0,0,0,0,0,0,0,0,true);
		eventPoint = 0;
		eventType = 0;
		
	}

}

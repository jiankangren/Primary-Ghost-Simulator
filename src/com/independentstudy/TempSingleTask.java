package com.independentstudy;

/**
 * A single task with some properties for FirstFit assignment algorithm
 * @author lian
 *
 */

public class TempSingleTask {
	
	double  phase;            //first release point of the task
	double  period;           //time length of two release point of the task
	double  releasePoint;     //time point at which the task is released
	double  relaDeadline;     // relative deadline of a task
	double  abDeadline;       //time point before which a task must be finished 
	double  interPoint;       //time point at which the task stop execution without finishing the remaining part
	double  endPoint;         //time point at which the task is finished
	int     taskIndex;        //index# of the task in the task set
	double  leftTime;         //the remaining part of the task when it is interrupted
	double  startPoint;       //time point at which the task starts execution
	double  continPoint;      //time point at which the task continues execution after interruption
	double  pWorstCet;        // worst case execution time of the primary task 
	double  fpWorstCet;       // never changed worst case execution time of the primary task
	double  pwcetMean;
	double  pwcetStd;
	double  gWorstCet;        // worst case execution time of the task
	double  fgWorstCet;       // never changed worst case execution time of the ghost task
	double  gwcetMean;
	double  gwcetStd;
	double  emptySlack;       // slack length of high critical task
	double  actualExe;        // actual execution time length
	double  isLack;           // unit of time used by other HI critical task for this high critical task  
	boolean interrupted;      //a flag shows the task was interrupted or not 
	double  taskUtili ;        // utilization of a task
	boolean primary;          // a flag shows whether the task is a primary task or a ghost task
	double  remainSlack;      // the remaining slack of a high critical task
	double  givenSlack;       // the execution time of low critical task from remaining slack or empty slack of high critical tasks
	int     priProcessor;     // index of processor to which this task is assigned to
	int     ghoProcessor;     // index of processor to which the ghost task is assigned to
	int     taskState;        // a flag shows the state of this task:  0: Not released ; 1: waiting ; 2: executing
	
	/**
	 * Creates a new SingleTask and initialize it with given values.
	 * @param phase : first releasing point of a task.
	 * @param period : time length between two release point of a task.
	 * @param relative_deadline : relative deadline of a task.
	 * @param task_index : the task order # in the given task set.
	 * @param worst_cet : high worst case execution time of a task.
	 * @param low_worst_cet : low worst case execution time of a task.
	 * @param HC_task : flag to show a task is high critical task or low critical task.
	 */
	
	public TempSingleTask(double phase, double period, double relative_deadline, int taskIndex,
			               double pWorstCet, double pwcetMean, double pwcetStd,
			               double gWorstCet, double gwcetMean, double gwcetStd,boolean primary){
		this.phase = phase;
		this.period = period;
		this.releasePoint = relative_deadline;
		this.taskIndex = taskIndex;
		this.pWorstCet = pWorstCet;
		this.pwcetMean = pwcetMean;
		this.pwcetStd = pwcetStd;
		this.gWorstCet = gWorstCet;
		this.gwcetMean = gwcetMean;
		this.gwcetStd = gwcetStd;
		this.primary = primary;
		fpWorstCet = pWorstCet ;
		fgWorstCet  = gWorstCet ;
		releasePoint = phase;
		abDeadline = relative_deadline;
		actualExe = 0;
		interPoint = 0;
		endPoint = 0;
		leftTime = 0;
		startPoint = 0;
		continPoint = 0;
		//utilization = 
		emptySlack = 0;
		isLack = 0;
		interrupted = false;
		remainSlack = 0;
		givenSlack = 0;
		priProcessor = 0;
		ghoProcessor = 0;
		taskUtili = primary? pWorstCet/period:gWorstCet/period;
	}

}

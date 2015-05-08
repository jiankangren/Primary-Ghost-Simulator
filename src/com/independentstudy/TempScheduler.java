package com.independentstudy;


import java.util.ArrayList;

/**
 * A system scheduler to schedule all tasks with multiple processors
 * @author lian
 *
 */
public class TempScheduler {
	
	ArrayList<TempSingleProcessor> processorList;      // a list contains all the processors in the system
	double totalEnergy;                                // total energy consumption of the system
	double totalTime;                                  // the simulation time length of the system
	double curPoint;                                   // current time point
	double maxPoint;                                   // an invalid time point out of simulation time length
    SingleSignal eventSignal;                          // event happens now
    ArrayList<String> recorder;                        // a list contains all information of events during simulation 
    double[] voltages;
	double vm;             // maximum voltage of all voltages choices of the system
	double pm;             // maximum energy consumption corresponding to maximum voltage value in a unit time
    
    
    /**
     * Create a new system scheduler.
     * @param processorList : a list contains all processors with assigned tasks on each processor.
     * @param totalTime     : total simulation time length.
     */
    public TempScheduler(ArrayList<TempSingleProcessor> processorList, double totalTime,double[] voltages,double pm){
    	
    	this.processorList = processorList;
    	this.totalTime = totalTime;
    	this.voltages = voltages;
    	this.pm = pm;
    	vm = voltages[voltages.length-1];
    	curPoint = 0;
    	maxPoint = totalTime+1;
    	eventSignal = new SingleSignal();
    	recorder = new ArrayList<String>();
    	
    	StringBuilder message1 = new StringBuilder();
		message1.append("Start initializing system");
		recorder.add(message1.toString());
    	initSystem();
    	StringBuilder message2 = new StringBuilder();
		message2.append("Finish initializing system");
		recorder.add(message2.toString());
    }
    
    

    
    
    /**
     * To schedule all tasks in one system with multiple processors
     */
    public void scheForSys(){
    	
    	while(curPoint<totalTime){
    		
    		eventSignal = getSysSignal();
    		
    		if(eventSignal.eventPoint>=totalTime) break;
    		
    	    scheduleSignal(eventSignal);
    		
    		
    		curPoint = eventSignal.eventPoint;
    		
    	}
    	
    	// simulation ends at this point
    	StringBuilder lastmessage = new StringBuilder();
    	lastmessage.append("Simulation ends at point : ").append(totalTime);
    	recorder.add(lastmessage.toString());
    	
    }
    
    /**
     * To initialize the system before simulation
     */
    
    public void initSystem(){
    	
    	for(int i=0;i<processorList.size();i++){
    		
    		processorList.get(i).initProcessor(totalTime);
    	}
    	
    }
    
    
    /**
     * To get next event for the system through comparing each processor's next event point
     * @return : a next event for system
     */
    public SingleSignal getSysSignal(){
    	
    	SingleSignal res = new SingleSignal();
    	res = processorList.get(0).getProNextEve();
    	for(int i=0;i<processorList.size();i++){
    		SingleSignal tempone = processorList.get(i).getProNextEve();
    		if(tempone!=null&&tempone.eventPoint<res.eventPoint){
    			res = tempone;
    		}
    	}
    	return res;
    	
    }
    
    /**
     * Schedule current event
     * @param eventSignal
     */
    
     public void scheduleSignal(SingleSignal eventSignal){
        
        int priProIndex = eventSignal.candidate.priProcessor;
		int ghoProIndex = eventSignal.candidate.ghoProcessor;
		
		// next event is to release a task
		if(eventSignal.eventType==0){
			if(eventSignal.candidate.primary){
				    				
				System.out.println("processor " + priProIndex + " release primary task " + eventSignal.candidate.taskIndex + " at time point " + eventSignal.eventPoint);
				processorList.get(priProIndex).releaseTask(eventSignal.eventPoint);
				
			}
			
			else{
				
				System.out.println("processor " + ghoProIndex + " release ghost task " + eventSignal.candidate.taskIndex + " at time point " + eventSignal.eventPoint);
				processorList.get(ghoProIndex).releaseTask(eventSignal.eventPoint);
				
			}
			   			
		}
		
		// next event is to finish a task
		else{
			
			
			
			// a primary task completes
			if(eventSignal.candidate.primary){    				    				
				   				
				System.out.println("processor " + priProIndex + " finish primary task " + eventSignal.candidate.taskIndex + " at time point " + eventSignal.eventPoint);
				processorList.get(priProIndex).finishPrimary(eventSignal.eventPoint);
				
				System.out.println("processor " + ghoProIndex + " remove ghost task " + eventSignal.candidate.taskIndex + " at time point " + eventSignal.eventPoint);
				processorList.get(ghoProIndex).removeGhost(eventSignal.eventPoint,eventSignal.candidate.taskIndex);
			
			}
			// a ghost task completes
			else{
				
				System.out.println("processor " + ghoProIndex + " finish ghost task " + eventSignal.candidate.taskIndex + " at time point " + eventSignal.eventPoint);
				processorList.get(ghoProIndex).finishGhost(eventSignal.eventPoint);
			    
			}
			
		}
        
     }
        

}

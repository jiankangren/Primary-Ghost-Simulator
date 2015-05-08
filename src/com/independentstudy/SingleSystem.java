package com.independentstudy;

import java.util.ArrayList;

/**
 * A system to process a given task set with multiple processors based on different algorithms.
 * @author lian
 *
 */
public class SingleSystem {
	
	TempSingleTask[] taskSet;
	int processorNum;
	double simuDuration;
	double sysEnergy;
	ArrayList<TempSingleProcessor> processorList;
	ArrayList<String> recorder;
	double[] voltages;
	double vm;             // maximum voltage of all voltages choices of the system
	double pm;             // maximum energy consumption corresponding to maximum voltage value in a unit time
	
	public SingleSystem(TempSingleTask[] taskSet, int processorNum, double[]voltages,double pm){
		
		this.taskSet = taskSet;
		this.processorNum = processorNum;
		this.voltages= voltages;
		this.pm = pm;
		simuDuration = 0;
		sysEnergy = 0;
		processorList = new ArrayList<TempSingleProcessor>();
		recorder = new ArrayList<String>();		
		vm = voltages[voltages.length-1];
		 
	}
	
	public void start(){
		
		FirstFitAssignAlg  firstAssignAlg = new FirstFitAssignAlg(taskSet,processorNum, voltages,  pm);
		firstAssignAlg.AssignPrimary();
		firstAssignAlg.AssignGhost();
		
		processorList = firstAssignAlg.processorList;
		recorder = firstAssignAlg.assignRecorder;
		// get simulation duration!!!
		
		TempScheduler sysScheduler = new TempScheduler(processorList, simuDuration,voltages,pm);
		sysScheduler.scheForSys();
		
		/*LeastUtiAssignAlg leastAssignAlg = new LeastUtiAssignAlg(taskSet,processorNum,voltages,pm);
		leastAssignAlg.AssignPrimary();
		leastAssignAlg.AssignGhost();
		
		processorList = leastAssignAlg.processorList;
		recorder = leastAssignAlg.assignRecorder;
		TempScheduler sysScheduler = new TempScheduler(processorList, simuDuration,voltages,pm);
		sysScheduler.scheForSys();*/
		
	}
	
	public static void main(String[] args){
		
		
		TempSingleTask[] taskset = new TempSingleTask[3];
		//taskset[0] = new TempSingleTask(0,5,5,0,2,0.1,true);
		//taskset[1] = new TempSingleTask(0,4,4,1,3,1,true);
		//taskset[2] = new TempSingleTask(0,10,10,2,4,3,true);
		taskset[0] = new TempSingleTask(0,5,5,0,2,2,0.1,2,2,0.1,true);
		taskset[1] = new TempSingleTask(0,4,4,1,2,2,0.1,2,2,0.1,true);
		taskset[2] = new TempSingleTask(0,10,10,2,4,4,0.1,4,4,0.1,true);
		double[] voltages = {1,2,3,4};
		double pm = 10;
		SingleSystem mySystem = new SingleSystem(taskset,3, voltages,pm);
		mySystem.simuDuration = 20;
		mySystem.start();
		//FirstFitAssignAlg  firstAssignAlg = new FirstFitAssignAlg(taskset,3);
		//firstAssignAlg.AssignPrimary();
		//firstAssignAlg.AssignGhost();
		ArrayList<TempSingleProcessor> tempprolist = mySystem.processorList;
		ArrayList<String> recorder = mySystem.recorder;
		
		
		//System.out.println(tempprolist.size());
		//for(int i=0;i<recorder.size();i++){
			//System.out.println(recorder.get(i));
		//}
		
		//for(int j=0;j<tempprolist.size();j++){
			//TempSingleProcessor temppro = tempprolist.get(j);
			//for(int k=0;k<temppro.taskList.size();k++){
				//System.out.println("processor "+ j + " : "+ temppro.taskList.get(k).primary + " task " + temppro.taskList.get(k).taskIndex);
			//}
		//}
		
		/*for(int i=0;i<tempprolist.size();i++){
			
			TempSingleProcessor temppro = tempprolist.get(i);
			//System.out.println("processor" + i + ": idlesize  "+temppro.idleList.size() + ", primarywaitlistsize "+ temppro.priWaitList.size() + ", ghostwaitlistsize "+temppro.ghoWaitList.size() );
			
			System.out.println("processor" + i + ": idlesize  "+temppro.idleList.size()+ " ghostlistsize " + temppro.ghostList.size());
			
			for(int k=0;k<temppro.taskList.size();k++){
				if(!temppro.taskList.get(k).primary){
					System.out.println("ghosttask "+ temppro.taskList.get(k).taskIndex + " gp  is " + temppro.taskList.get(k).ghoProcessor+" pp is " + temppro.taskList.get(k).priProcessor);
				}
				else{
					System.out.println("primarytask "+ temppro.taskList.get(k).taskIndex + " pp  is " + temppro.taskList.get(k).priProcessor+" gp is " + temppro.taskList.get(k).ghoProcessor);
				}
			}
			
			for(int k=0;k<temppro.idleList.size();k++){
				if(!temppro.idleList.get(k).primary){
					//System.out.println("ghosttask "+ temppro.idleList.get(k).taskIndex + " release point  is " + temppro.idleList.get(k).releasePoint );
					System.out.println("ghosttask "+ temppro.idleList.get(k).taskIndex + " gp  is " + temppro.idleList.get(k).ghoProcessor+" pp is " + temppro.idleList.get(k).priProcessor);
				}
				else{
					//System.out.println("primarytask "+ temppro.idleList.get(k).taskIndex + " release point  is " + temppro.idleList.get(k).releasePoint);
					
					System.out.println("primarytask "+ temppro.idleList.get(k).taskIndex + " pp  is " + temppro.idleList.get(k).priProcessor+" gp is " + temppro.idleList.get(k).ghoProcessor);
				}
			}
			
		}*/
		
		for(int i=0;i<tempprolist.size();i++){
			
			mySystem.sysEnergy+=tempprolist.get(i).proEnergy;
			
			System.out.println("processor " + i +  " total energy consumption is " + tempprolist.get(i).proEnergy);
		}
		
		System.out.println("System total energy consumption is " + mySystem.sysEnergy); 
		
	}

}

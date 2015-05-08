package com.independentstudy;

import java.util.ArrayList;


/**
 * An algorithm to assign tasks on multiple processors 
 * with the rule that current task should be assigned to the first fit processor with utilization less than 1.
 * @author lian
 *
 */
public class FirstFitAssignAlg {
	
	int processorNum;          // number of processors to schedule tasks
	TempSingleTask[] primaryTaskset;    // all primary tasks to be assigned
	ArrayList<TempSingleProcessor> processorList;   // a list contains all processors
	ArrayList<String> assignRecorder;    // a list contains all assignment information
	
	
	/**
	 * Initialize the algorithm with given parameters
	 * @param primaryTaskset : task set of all primary tasks
	 * @param processorNum : number of total processors
	 */
	public FirstFitAssignAlg(TempSingleTask[] primaryTaskset, int processorNum, double[] voltages, double pm){
		
		this.primaryTaskset = primaryTaskset;
		this.processorNum = processorNum;
		processorList = new ArrayList<TempSingleProcessor>();
		assignRecorder = new ArrayList<String>();
		
		//---Initialize the processor list with given number of processors----//
		for(int i=0;i<processorNum;i++){
			TempSingleProcessor temppro = new TempSingleProcessor(i,voltages,pm);
			processorList.add(temppro);
		}
	}
	
	/**
	 * Assign primary tasks to processors
	 */
	public void AssignPrimary(){
		
		//----Assign each primary task-----//
		for(int i=0;i<primaryTaskset.length;i++){
			
			TempSingleTask temptask = primaryTaskset[i];
			temptask.taskUtili = temptask.pWorstCet/temptask.period;
			
			int j=0;
			boolean assigned = false;
			
			//----find an appropriate processor to assign the given primary task based on first fit algorithm----//
			while(j<processorList.size()&& !assigned){
				
				if((processorList.get(j).proUtili+temptask.taskUtili)<=1){
					temptask.priProcessor = j;
					processorList.get(j).taskList.add(temptask);
					processorList.get(j).proUtili += temptask.taskUtili;
					
					assigned = true;
					StringBuilder mess1 = new StringBuilder();
					mess1.append("primary task "+ i + " is assigned to prcessor " + j + " with " + temptask.taskUtili + " and total uti of processor " + j + " is " + processorList.get(j).proUtili);
					assignRecorder.add(mess1.toString());
				}
				
				j++;
				
			}
			
			if(!assigned){
				
				StringBuilder mess2 = new StringBuilder();
				mess2.append("primary task " + primaryTaskset[i].taskIndex +" can not be assigned ");
				assignRecorder.add(mess2.toString());
			}
			
		}
	}
	
	
	
	/**
	 * Assign ghost tasks to processors
	 */
	public void AssignGhost(){
		
		//----assign each ghost task to processors which does not contain primary task of the ghost task----//
		for(int i=0;i<primaryTaskset.length;i++){
			TempSingleTask tempPri = primaryTaskset[i];
			TempSingleTask temptask = new TempSingleTask(tempPri.phase, tempPri.period, tempPri.relaDeadline, tempPri.taskIndex,
					tempPri.pWorstCet,tempPri.pwcetMean,tempPri.pwcetStd,tempPri.gWorstCet,tempPri.gwcetMean, tempPri.gwcetStd,tempPri.primary);
			temptask.primary = false;
			temptask.priProcessor = tempPri.priProcessor;
			int tempindex = temptask.taskIndex;
			temptask.taskUtili = temptask.gWorstCet/temptask.period;
			
			int j=0;
			boolean assigned = false;
			
			//-----find an appropriate processor to assign the given ghost task----//
			while(j<processorList.size()&& !assigned){
				
				if((processorList.get(j).proUtili+temptask.taskUtili)<=1){
					
					boolean have_primary = false;
					for(int k=0;k<processorList.get(j).taskList.size();k++){
						if(processorList.get(j).taskList.get(k).taskIndex==tempindex){
							have_primary = true;
						}
					}
					if(!have_primary){
						
						temptask.ghoProcessor = j;
						processorList.get(j).taskList.add(temptask);
						processorList.get(j).proUtili += temptask.taskUtili;
						assigned = true;
						for(int m=0;m<processorList.get(temptask.priProcessor).taskList.size();m++){
							if(processorList.get(temptask.priProcessor).taskList.get(m).taskIndex==temptask.taskIndex){
								processorList.get(temptask.priProcessor).taskList.get(m).ghoProcessor = j;
							}
						}
						StringBuilder mess1 = new StringBuilder();
						mess1.append("ghost   task "+ i + " is assigned to prcessor " + j + " with " + temptask.taskUtili + " and total uti of processor " + j + " is " + processorList.get(j).proUtili);
						assignRecorder.add(mess1.toString());
						
					}
					
				}
				
				j++;
			}
			
            if(!assigned){
				
				StringBuilder mess2 = new StringBuilder();
				mess2.append("ghost task " + primaryTaskset[i].taskIndex +" can not be assigned ");
				assignRecorder.add(mess2.toString());
			}  
			
		}
		
	}

}

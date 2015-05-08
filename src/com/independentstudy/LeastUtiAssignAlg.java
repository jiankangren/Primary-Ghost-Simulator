package com.independentstudy;


import java.util.ArrayList;

/**
 * An algorithm to assign tasks on multiple processors 
 * with the rule that current task should be assigned to the processor with least utilization.
 * @author lian
 *
 */
public class LeastUtiAssignAlg {
	
	int processorNum;       //number of processors to schedule tasks
	TempSingleTask[] primaryTaskset;      // all primary tasks to be assigned
	ArrayList<TempSingleProcessor> processorList;     //a list contains all processors
	ArrayList<String> assignRecorder;      // a list contains all assignment information
	
	/**
	 * Initialize the algorithm with given parameters
	 * @param primaryTaskset :task set of all primary tasks
	 * @param processorNum : number of total processors
	 * @param voltages : voltage set to be chosen
	 * @param pm
	 */
	public LeastUtiAssignAlg(TempSingleTask[] primaryTaskset, int processorNum, double[] voltages, double pm){
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
		
		//-----Assign each primary task---//
		for(int i=0;i<primaryTaskset.length;i++){
			
			StringBuilder tempmess = new StringBuilder();
			TempSingleTask temptask = primaryTaskset[i];
			temptask.taskUtili = temptask.pWorstCet/temptask.period;
			
			//----find an appropriate processor to assign the given primary task -----//
			//---based on least utilization algorithm----//
			
			int tempIndex = -1;
			double minUtili = 1.1;
			for(int j=0;j<processorList.size();j++){
				if(processorList.get(j).proUtili<minUtili){
					tempIndex = j;
					minUtili = processorList.get(j).proUtili;
				}
			}
			
			if(tempIndex==-1||minUtili+temptask.taskUtili>1){

				tempmess.append("Fail to assign the task set with least utilization algorithm");
				assignRecorder.add(tempmess.toString());
			}
			
			else{
				temptask.priProcessor = tempIndex;
				processorList.get(tempIndex).taskList.add(temptask);
				processorList.get(tempIndex).proUtili += temptask.taskUtili;
				tempmess.append("primary task "+ i + " is assigned to prcessor " + tempIndex + " with " + temptask.taskUtili + " and total uti of processor " + tempIndex + " is " + processorList.get(tempIndex).proUtili);
				assignRecorder.add(tempmess.toString());
			}
			
		}				
	}
	
	
	public void AssignGhost(){
		
		//----assign each ghost task to processors which does not contain primary task of the ghost task----//		
		for(int i=0;i<primaryTaskset.length;i++){
			
			StringBuilder tempmess = new StringBuilder();
			TempSingleTask tempPri = primaryTaskset[i];
			TempSingleTask temptask = new TempSingleTask(tempPri.phase, tempPri.period, tempPri.relaDeadline, tempPri.taskIndex,
					tempPri.pWorstCet,tempPri.pwcetMean,tempPri.pwcetStd,tempPri.gWorstCet,tempPri.gwcetMean, tempPri.gwcetStd,tempPri.primary);
			temptask.primary = false;
			temptask.priProcessor = tempPri.priProcessor;
			temptask.taskUtili = temptask.gWorstCet/temptask.period;
			
			
			//----find an appropriate processor to assign the corresponding ghost task -----//
			//---based on least utilization algorithm----//
			int tempIndex = -1;
			double minUtili = 1.1;
			
			for(int j=0;j<processorList.size();j++){
				if(processorList.get(j).proUtili<minUtili){
					
					boolean havePrimary = false;
					for(int k=0;k<processorList.get(j).taskList.size();k++){
						if(processorList.get(j).taskList.get(k).taskIndex==temptask.taskIndex) 
							havePrimary=true;
					}
					if(!havePrimary){
						tempIndex = j;
						minUtili = processorList.get(j).proUtili;
					}
				}
			}
			
			if(tempIndex==-1||minUtili+temptask.taskUtili>1){
				tempmess.append("Fail to assign the task set with least utilization algorithm");
				assignRecorder.add(tempmess.toString());
			}
			
			else{
				temptask.ghoProcessor = tempIndex;
				processorList.get(tempIndex).taskList.add(temptask);
				processorList.get(tempIndex).proUtili += temptask.taskUtili;
				for(int m=0;m<processorList.get(temptask.priProcessor).taskList.size();m++){
					if(processorList.get(temptask.priProcessor).taskList.get(m).taskIndex==temptask.taskIndex)
						processorList.get(temptask.priProcessor).taskList.get(m).ghoProcessor = tempIndex;
				}
				
				tempmess.append("ghost   task "+ i + " is assigned to prcessor " + tempIndex + " with " + temptask.taskUtili + " and total uti of processor " + tempIndex + " is " + processorList.get(tempIndex).proUtili);
				assignRecorder.add(tempmess.toString());
			}
		}
	}

}

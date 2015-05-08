package com.independentstudy;

/**
 * A single processor with its properties.
 * @author lian
 *
 */

import java.util.ArrayList;
import java.util.Random;


public class TempSingleProcessor {
	
	
	int proIndex;                              //index of this processor
	double proUtili;                           // total utilization of all tasks assigned to the processor
	ArrayList<TempSingleTask> taskList;        // a list contains all tasks assigned to the processor	
	ArrayList<TempSingleTask> idleList;        // a list contains all unreleased tasks on this processor
	ArrayList<TempSingleTask> priWaitList;     // a list contains all primary waiting tasks on this processor
	ArrayList<TempSingleTask> ghoWaitList;     // a list contains all ghost waiting tasks on this processor
	ArrayList<TempSingleTask> activeList;      // a list contains  a active task which is executing on this processor
	ArrayList<TempSingleTask> ghostList;       // a list contains all ghost tasks
	double proEnergy;                          // energy consumption of this processor
	double endRemainPoint;                     // time point shows a remaining slack ends
	double simuDuration;                       // simulation duration of the system
	double curVoltage;
	double[] voltages;
	double pm;
	double vm;
	double lastPoint;
	
	public TempSingleProcessor(int processor_index,double[] voltages, double pm){
		
		this.proIndex = processor_index;
		this.voltages = voltages;
		this.pm = pm;
		proUtili = 0;
		taskList = new ArrayList<TempSingleTask>();
		proEnergy = 0;
		idleList = new ArrayList<TempSingleTask>();
		priWaitList = new ArrayList<TempSingleTask>();
		ghoWaitList = new ArrayList<TempSingleTask>();
		activeList = new ArrayList<TempSingleTask>();
		ghostList = new ArrayList<TempSingleTask>();
		endRemainPoint = 0;
		simuDuration = 0;
		vm = voltages[voltages.length-1];
		lastPoint=0;
	}
	
	
	/**
	 * Initialize the processor
	 * @param totalTime : total simulation time of system
	 */
	public void initProcessor(double totalTime){
		
		simuDuration = totalTime;
		for(int i=0;i<taskList.size();i++){
			TempSingleTask candi = taskList.get(i);
			candi.releasePoint = candi.phase;
			candi.abDeadline = candi.releasePoint + candi.relaDeadline;
			
			//--put all ghost task on a certain list to calculate and set their empty slack---//
			if(!candi.primary){
				insertGhost(ghostList,candi);
			}
			else{
				idleList.add(candi);
			}
			
			//---generate all jobs during simulation time for each primary task----//
			for(int k=1;k<=(totalTime-candi.phase)/candi.period;k++){
				TempSingleTask tempCandi = new TempSingleTask(candi.phase,candi.period,candi.relaDeadline,
				candi.taskIndex,candi.pWorstCet,candi.pwcetMean,candi.pwcetStd,candi.gWorstCet,
				candi.gwcetMean,candi.gwcetStd,candi.primary);
				tempCandi.priProcessor = candi.priProcessor;
				tempCandi.ghoProcessor = candi.ghoProcessor;

				tempCandi.releasePoint = candi.phase + k*candi.period;
				tempCandi.abDeadline = tempCandi.releasePoint + tempCandi.relaDeadline;
				
				if(!tempCandi.primary){
					insertGhost(ghostList,tempCandi);
				}
				else{
					idleList.add(tempCandi);
				}
			}
		}
		
		if(ghostList.size()>0){
			setEmptySlack();
		}
		
		
	}
	
	
	/**
	 * Release a task and reorganize the state of all tasks. 
	 * @param signalPoint : the point at which a task is released.
	 */
	public void releaseTask(double signalPoint){

		
		TempSingleTask retask = idleList.get(0);
		idleList.remove(0);
		StringBuilder remessage = new StringBuilder();
		//--to release a ghost task--//
		if(!retask.primary){
			
			remessage.append("Ghost task ").append(retask.taskIndex).append(" releases at point ").
			append(signalPoint);
			Random r = new Random();              // set the execution time of high critical task randomly as high/low worst case execution time
			retask.actualExe = r.nextGaussian()*retask.gwcetStd + retask.gwcetMean;
			if(retask.actualExe>retask.fgWorstCet) retask.actualExe = retask.fgWorstCet;				
			retask.gWorstCet= retask.actualExe;
			//--if no task is executing, set the releasing high critical task to execute--//
			if(activeList.size()==0){
				remessage.append(" and no task is executing now, so the releasing task is executing at point ").append(signalPoint);
				retask.continPoint = signalPoint;          
				
				retask.leftTime = retask.gWorstCet;
				retask.endPoint = retask.continPoint + retask.leftTime;
				retask.gWorstCet = 0;
				activeList.add(retask);
				
				//recorder.add(remessage.toString());
				
			}
			
			//---to interrupt the executing task and put executing and releasing one on waiting list to choose next task to execute--//
			else{
				
				ghoWaitList.add(retask);
				TempSingleTask	activeone = activeList.get(0);
				
				if(!activeone.primary){
					activeone.interrupted = true;
					activeone.interPoint = signalPoint;
					activeone.gWorstCet = activeone.gWorstCet + activeone.endPoint - signalPoint;
					activeList.remove(0);
					ghoWaitList.add(activeone);
				}
				else{  // the running one is a primary task
					
					activeone.interrupted = true;
					activeone.interPoint = signalPoint;
					activeone.pWorstCet = activeone.pWorstCet + activeone.endPoint - signalPoint;
					if(activeone.givenSlack>0){
						ghoWaitList.get(0).emptySlack = ghoWaitList.get(0).emptySlack + 
						activeone.givenSlack -(signalPoint- activeone.continPoint);
						activeone.givenSlack = 0;
					}
					activeList.remove(0);
					priWaitList.add(activeone);
				}
				remessage.append(" and task ").append(activeone.taskIndex).append(" is interrupted at point ").
				append(signalPoint);
				//recorder.add(remessage.toString());
				chooseTask(signalPoint);
			}
			
			double newUtili = retask.fgWorstCet/retask.period;
			double lastUtili = getUtili(retask,newUtili);
			//System.out.println("The releasing task is a ghost task and the previous utili is " + lastUtili+ 
			//" and processor utili is " + proUtili);
			proUtili = proUtili - lastUtili + newUtili;
			//System.out.println("The releasing task new utili is " + newUtili + " and processor new utili is " 
			//+ proUtili);
			System.out.println("lasttotal energy is " + proEnergy);
			double tempEnergy = (curVoltage/vm)*(curVoltage/vm)*pm*(signalPoint-lastPoint);  // energy consumption between two events points
			lastPoint = signalPoint;
			proEnergy = proEnergy + tempEnergy;    //update total energy consumption
			System.out.println("temp energy is " + tempEnergy + ", and current total energy consumption is " + 
			proEnergy + " HERE!!!!!");
			curVoltage = setVoltage();
			System.out.println("current voltage is set as " + curVoltage);
		}
		
		
		//--to release a primary task--//
		else{
			
			remessage.append("Primary task ").append(retask.taskIndex).append(" releases at point ").append(signalPoint);
			
			Random r = new Random();              // set the execution time of high critical task randomly as high/low worst case execution time
			retask.actualExe = r.nextGaussian()*retask.pwcetStd + retask.pwcetMean;
			if(retask.actualExe>retask.fpWorstCet) retask.actualExe = retask.fpWorstCet;
			retask.pWorstCet = retask.actualExe;
			
			//--if no task is executing, set the releasing low critical task to execute--//
			if(activeList.size()==0){
				remessage.append(" and no task is executing now, so the releasing task is executing at point ").
				append(signalPoint);
	
				retask.continPoint = signalPoint;
				retask.leftTime = retask.pWorstCet;
				retask.endPoint = retask.continPoint + retask.leftTime;
				retask.pWorstCet = 0;
				activeList.add(retask);
				//recorder.add(remessage.toString());
			}
			
			
			//---to interrupt the executing task and put executing and releasing one on waiting list to choose next task to execute--//
			else{
				
				priWaitList.add(retask);
				TempSingleTask activeone = activeList.get(0);
				
				if(!activeone.primary){
					activeone.interrupted = true;
					activeone.interPoint = signalPoint;
					activeone.gWorstCet = activeone.gWorstCet + activeone.endPoint - signalPoint;
					activeList.remove(0);
					ghoWaitList.add(activeone);
				}
				else{  // the running one is a primary task
					activeone.interrupted = true;
					activeone.interPoint = signalPoint;
					activeone.pWorstCet = activeone.pWorstCet + activeone.endPoint - signalPoint;
					activeList.remove(0);
					priWaitList.add(activeone);
				}
				
				remessage.append(" and task ").append(activeone.taskIndex).append(" is interrupted at point ")
				.append(signalPoint);
				
				//recorder.add(remessage.toString());
				
				//setVoltage();
				chooseTask(signalPoint);
				
			}
			
			double newUtili = retask.fpWorstCet/retask.period;
			double lastUtili = getUtili(retask, newUtili);
			//System.out.println("The releasing task is a primary task and the previous utili is " + 
			lastUtili + " and processor utili is " + proUtili);
			proUtili = proUtili - lastUtili + newUtili;
			//System.out.println("The releasing task new utili is " + newUtili + " and processor new utili is " 
			//+ proUtili);
			System.out.println("lasttotal energy is " + proEnergy);
			double tempEnergy = (curVoltage/vm)*(curVoltage/vm)*pm*(signalPoint-lastPoint);  // energy consumption between two events points
			lastPoint = signalPoint;
			proEnergy = proEnergy + tempEnergy;    //update total energy consumption
			System.out.println("temp energy is "+ tempEnergy + ", and current energy consumption is " + proEnergy + " HERE!!!!!");
			curVoltage = setVoltage();
			System.out.println("current voltage is set as " + curVoltage);
		}
		
		//recorder.add(" ");
	}
	
	/**
	 * Finish a primary task and reorganize the state of all tasks. 
	 * @param signalPoint : time point at which a primary task completes
	 */
	public void finishPrimary(double signalPoint){
		double factor = 0.9;
		
		TempSingleTask finishone = activeList.get(0);
		activeList.remove(0);
		StringBuilder fimessage = new StringBuilder();
		fimessage.append("Primary task ").append(finishone.taskIndex).append(" is finishing at point ").append(signalPoint);
		//recorder.add(fimessage.toString());
		chooseTask(signalPoint);
		//recorder.add(" ");
		double newUtili = factor*finishone.fpWorstCet/finishone.period;
        
		double lastUtili = getUtili(finishone, newUtili);		
		
		//System.out.println("The finishing task is a primary task and the previous utili is " + lastUtili + 
		//" and processor utili is " + proUtili);

		proUtili = proUtili - lastUtili + newUtili;
		
		//System.out.println("The finishing task new utili is " + newUtili + " and processor new utili is " + proUtili);
		System.out.println("lasttotal energy is " + proEnergy);
		
		double tempEnergy = (curVoltage/vm)*(curVoltage/vm)*pm*(signalPoint-lastPoint);  // energy consumption between two events points
		lastPoint = signalPoint;
		proEnergy = proEnergy + tempEnergy;    //update total energy consumption
		System.out.println("temp energy is "+ tempEnergy +", current energy consumption is " + proEnergy + " HERE!!!!!");
		
		if(activeList.size()==0) {
			curVoltage = 0;
			System.out.println("The system is now idle and set the voltage to 0 !!!hahahahahahahahahhahahahahahahahhahahahaha!!!!!!!!!!!");
		}
		else curVoltage = setVoltage();
		System.out.println("current voltage is set as " + curVoltage);
		//setVoltage();
				
	}
	
	
	/**
	 * Finish a ghost task and reorganize the state of all tasks.
	 * @param signalPoint : time point at which a ghost task completes
	 */
	public void finishGhost(double signalPoint){
		double factor = 0.9;
		TempSingleTask finishone = activeList.get(0);
		activeList.remove(0);
		StringBuilder fimessage = new StringBuilder();
		fimessage.append("Ghost task ").append(finishone.taskIndex).append(" is finishing at point ").append(signalPoint);					
		finishone.remainSlack = finishone.abDeadline-signalPoint;
		if(finishone.remainSlack>0){
			endRemainPoint = Math.max(endRemainPoint, finishone.abDeadline);						
		}
		fimessage.append(" And the end remain point is ").append(endRemainPoint);
		//recorder.add(fimessage.toString());
		chooseTask(signalPoint);
					
		double newUtili = factor*finishone.fgWorstCet/finishone.period;
		double lastUtili = getUtili(finishone, newUtili);
		//System.out.println("The finishing task is a ghost task and the previous utili is " + lastUtili 
		//+ " and processor utili is " + proUtili);		
		proUtili = proUtili -lastUtili + newUtili;
		//System.out.println("The finishing task new utili is " + newUtili + " and processor new utili is " + proUtili);
		System.out.println("lasttotal energy is " + proEnergy);
		double tempEnergy = (curVoltage/vm)*(curVoltage/vm)*pm*(signalPoint-lastPoint);  // energy consumption between two events points
		lastPoint = signalPoint;
		proEnergy = proEnergy + tempEnergy;    //update total energy consumption
		System.out.println(" temp energy is "+ tempEnergy + ", and current energy consumption is " +
		proEnergy + " HERE!!!!!");
		
		if(activeList.size()==0)  {
			curVoltage = 0;
			System.out.println("The system is now idle and set the voltage to 0 !!!hahahahahahahahahhahahahahahahahhahahahaha!!!!!!!!!!!");
		}
		else curVoltage = setVoltage();
		System.out.println("current voltage is set as " + curVoltage);
	    //recorder.add(" ");
		//setVoltage();
		
	}
	
	/**
	 * Remove a given ghost task from processor. 
	 * @param signalPoint : time point at which a ghost task is removed from the processor.
	 * @param taskIndex : the index of the ghost task that is to be removed.
	 */
	public void removeGhost(double signalPoint, int taskIndex){
				
		StringBuilder fimessage = new StringBuilder();
		if(activeList.size()>0&&(activeList.get(0).taskIndex==taskIndex)){
			TempSingleTask removeone = activeList.get(0);
			fimessage.append("Ghost task ").append(removeone.taskIndex).append(" is removed at point ")
			.append(signalPoint);					
			removeone.remainSlack = removeone.abDeadline-signalPoint;
			if(removeone.remainSlack>0){
				endRemainPoint = Math.max(endRemainPoint, removeone.abDeadline);						
			}
			fimessage.append(" And the end remain point is ").append(endRemainPoint);
			//recorder.add(fimessage.toString());
			chooseTask(signalPoint);
			
			if(activeList.size()==0){
				
				System.out.println("lasttotal energy is " + proEnergy);
				double tempEnergy = (curVoltage/vm)*(curVoltage/vm)*pm*(signalPoint-lastPoint);  // energy consumption between two events points
				lastPoint = signalPoint;
				proEnergy = proEnergy + tempEnergy;    //update total energy consumption
				System.out.println(" temp energy is "+ tempEnergy + ", and current energy consumption is "
				+ proEnergy + " HERE!!!!!");
				
				curVoltage = 0;
				System.out.println("The system is now idle and set the voltage to 0 !!!hahahahahahahahahhahahahahahahahhahahahaha!!!!!!!!!!!");
			}
		    //recorder.add(" ");

		}
		
		else if(ghoWaitList.size()>0){
			int i = 0;
			boolean found = false;
			while(i<ghoWaitList.size()&&found==false){
				if(ghoWaitList.get(i).taskIndex==taskIndex){
					found = true;
					break;
				}
				i++;
			}
			
			if(found){
				ghoWaitList.remove(i);
				fimessage.append("Ghost task ").append(taskIndex).append(" is removed at point ")
				.append(signalPoint);				
			}
		}
		
		
	}
	
	
	/**
	 * Choose a task to run 
	 * @param signalPoint : time point at which processor chooses a task to run
	 */
	
	public void chooseTask(double signalPoint){
		
        StringBuilder tm = new StringBuilder();
		
		//---no task is waiting to execute----//
		if(priWaitList.size()==0&&ghoWaitList.size()==0){
			tm.append("No task is ready to be scheduled now");
			//recorder.add(tm.toString());
		}
		
		//----there is only low primary tasks are waiting to execute, and choose one to run----//
		else if(ghoWaitList.size()==0){
            
			tm.append("Only primary tasks are waiting to execute.");
			double tpoint = findFirstEve(priWaitList,1);
			TempSingleTask ttask = priWaitList.get(0);
			priWaitList.remove(0);
			ttask.continPoint = signalPoint;
			if(ttask.interrupted){
				ttask.leftTime = ttask.pWorstCet;
				ttask.endPoint = ttask.continPoint + ttask.leftTime;
				ttask.pWorstCet = 0;
			}
			else{				
				ttask.leftTime = ttask.pWorstCet;
				ttask.endPoint = ttask.continPoint + ttask.leftTime;
				ttask.pWorstCet = 0;
			}
			activeList.add(ttask);
			tm.append("And choose a primary  task ").append(ttask.taskIndex).append(" to execute at point ")
			.append(signalPoint);
			//recorder.add(tm.toString());
		}
		
		
		//----there is only ghost tasks are waiting to execute, and choose one to run----//
		else if(priWaitList.size()==0){
			
			tm.append("Only ghost tasks are waiting to execute.");
			double tpoint = findFirstEve(ghoWaitList,1);
			TempSingleTask ttask = ghoWaitList.get(0);
			ghoWaitList.remove(0);
			ttask.continPoint = signalPoint;
			if(ttask.interrupted){
				ttask.leftTime = ttask.gWorstCet;
				ttask.endPoint = ttask.continPoint + ttask.leftTime;
				ttask.gWorstCet = 0;
			}
			else{
				
				ttask.leftTime = ttask.gWorstCet;
				ttask.endPoint = ttask.continPoint + ttask.leftTime;
				ttask.gWorstCet= 0;
				
			}
			
			activeList.add(ttask);
			tm.append("And choose ghost task ").append(ttask.taskIndex).append(" to execute at point ")
			.append(signalPoint);
			//recorder.add(tm.toString());
		}
		
		
		// ----both primary and ghost tasks are waiting to execute----//
		else{
			
			tm.append("Both primary and ghost tasks are waiting to execute.");
			double p_first = findFirstEve(ghoWaitList,1);
			double g_first = findFirstEve(priWaitList,1);
			
			//--if there is remaining slack, choose low critical task to execute----//
			if(endRemainPoint>signalPoint){
				
				tm.append(" And there is remaining slack for primary task to execute.");
				TempSingleTask ttask = priWaitList.get(0);
				priWaitList.remove(0);
				ttask.continPoint = signalPoint;
				ttask.leftTime = Math.min(ttask.pWorstCet, endRemainPoint-signalPoint);
				ttask.endPoint = ttask.continPoint + ttask.leftTime; 
				ttask.pWorstCet= ttask.pWorstCet - ttask.leftTime;
				activeList.add(ttask);
				tm.append(" Primary task ").append(ttask.taskIndex).append(" to execute at point ").
				append(signalPoint).append("for ").append(ttask.leftTime);
				//recorder.add(tm.toString());
			}
			
			//---if there is empty slack at this point, choose a primary task to execute----//
			else if(ghoWaitList.get(0).emptySlack>0 && ghoWaitList.get(0).abDeadline-ghoWaitList.get(0)
			.gWorstCet-signalPoint>0){
				
				tm.append(" And there is empty slack for primary tasks to execute.");
				TempSingleTask ttask = priWaitList.get(0);
				priWaitList.remove(0);
				ttask.continPoint = signalPoint;
				ghoWaitList.get(0).emptySlack = Math.min(ghoWaitList.get(0).abDeadline-ghoWaitList.get(0)
				.gWorstCet-signalPoint, ghoWaitList.get(0).emptySlack);
				ttask.leftTime = Math.min(ttask.pWorstCet, ghoWaitList.get(0).emptySlack);
				ttask.givenSlack = ttask.leftTime;
				ttask.endPoint = ttask.continPoint + ttask.leftTime;
				ttask.pWorstCet = ttask.pWorstCet - ttask.leftTime;
				ghoWaitList.get(0).emptySlack = ghoWaitList.get(0).emptySlack - ttask.leftTime;
				activeList.add(ttask);
				
				tm.append(" Primary task ").append(ttask.taskIndex).append(" to execute at point ").
				append(signalPoint).append(" for ").append(ttask.leftTime);
				//recorder.add(tm.toString());
			}
			
			
			//-----if there is no remaining slack or empty slack, choose a ghost task to execute----//
			else{
				
				tm.append(" And there is no remaining slack or empty slack for primary tasks. ");
				TempSingleTask ttask = ghoWaitList.get(0);
				ghoWaitList.remove(0);
				ttask.continPoint = signalPoint;
				if(ttask.interrupted){
					ttask.leftTime = ttask.gWorstCet;
					ttask.endPoint = ttask.continPoint + ttask.leftTime;
					ttask.gWorstCet = 0;
				}
				else{

					ttask.leftTime = ttask.gWorstCet;
					ttask.endPoint = ttask.continPoint + ttask.leftTime;
					ttask.gWorstCet = 0;
					
				}
				
				activeList.add(ttask);
				tm.append("And choose ghost task ").append(ttask.taskIndex).append(" to execute at point ")
				.append(signalPoint);
				//recorder.add(tm.toString());
				
			}
			
		}
		
	}
	
	
	/**
	 * Insert a ghost task in a list containing all ghosts on one processor with absolute deadline 
	 * in ascending order.
	 * @param ghostList : a list contains all ghost tasks on one processor within simulation duration.
	 * @param intask : a ghost to be inserted in the list in an appropriate position.
	 */
	public void insertGhost(ArrayList<TempSingleTask> ghostList, TempSingleTask intask){
		
		if(ghostList.size()==0){
			ghostList.add(intask);
		}
		else{
			boolean inserted = false;
			for(int j=0;j<ghostList.size();j++){
				TempSingleTask tptask = ghostList.get(j);
				if(intask.abDeadline<=tptask.abDeadline){
					ghostList.add(j,intask);
					inserted = true;
					break;
				}
			}
			if(!inserted) ghostList.add(intask);
		}
		
	}
	
	
	/**
	 * Set empty slack for each ghost task before simulation
	 */
	public void setEmptySlack(){
		
		for(int k=ghostList.size()-1;k>=0;k--){
			
			if(k==ghostList.size()-1){
				ghostList.get(k).isLack = 0;
			}
			
			else{
				if(ghostList.get(k).abDeadline-ghostList.get(k+1).abDeadline+ghostList.get(k+1).isLack
				+ghostList.get(k+1).gWorstCet>0){
					ghostList.get(k).isLack= ghostList.get(k).abDeadline-ghostList.get(k+1)
					.abDeadline+ghostList.get(k+1).isLack+ghostList.get(k+1).gWorstCet;
				}
				else{
					ghostList.get(k).isLack = 0;					
				}
			}
			
			if(k==0){
				if(ghostList.get(k).abDeadline - ghostList.get(k).isLack - ghostList.get(k).gWorstCet>0){
					ghostList.get(k).emptySlack = ghostList.get(k).abDeadline - ghostList.get(k).isLack 
					- ghostList.get(k).gWorstCet;
				}
				else ghostList.get(k).emptySlack = 0;
			}
			else{
				if(ghostList.get(k).abDeadline - ghostList.get(k-1).abDeadline - ghostList.get(k).isLack
				- ghostList.get(k).gWorstCet>0){
					ghostList.get(k).emptySlack =ghostList.get(k).abDeadline- ghostList.get(k-1)
					.abDeadline - ghostList.get(k).isLack - ghostList.get(k).gWorstCet;
				}
				else ghostList.get(k).emptySlack= 0;
			}
			
			idleList.add(ghostList.get(k));
			
		}
		
	}
	
	
	/**
	 * Get next event with task , time point and event type
	 * @return : a SingleSignal
	 */
	public SingleSignal getProNextEve(){
		SingleSignal res = new SingleSignal();
		double nextRePoint = findFirstEve(idleList, 2);   // get next releasing task point 
		double nextFiniPoint = findFirstEve(activeList,0); // get next finishing task point
		
		//next event is to release a task
		if(nextRePoint<nextFiniPoint){
			res.candidate = idleList.get(0);
			res.eventType = 0;
			res.eventPoint = nextRePoint;
		}
		
		//next event is to finish a task
		else{
			res.candidate = activeList.get(0);
			res.eventType = 1;
			res.eventPoint = nextFiniPoint;
		}
		
		
		return res;
	}
	
	/**
	 * Find the next event point and put the corresponding task in the front of the list
	 * Find the task with earliest release point in idle list and put it in the front of idle list
	 * or find the task with earliest deadline in waiting list and put it in the front of waiting list
	 * or  find the task with coming end point
	 * 
	 * @param list  : input idle list or waiting list or active list
	 * @param listtype  : 0 flags it is a active list; 1 flags it is a waiting list; 2 flags it is an idle list.
	 * @return     : the earliest point in the given list
	 */
    public double findFirstEve(ArrayList<TempSingleTask> list, int listType){
    	
    	
    	double maxTime = simuDuration +1;
		if(list.size()==0){       // if the list has no task, return a very large time point  
			return maxTime;      // to show the list is empty
		}
		
		
		double minPoint=0;
		
		//-------For active list, get the end point of the active task ---------//
		if(listType==0){                 
			
			minPoint = list.get(0).endPoint;
			
		}
		
		
		//-------For waiting list, get the earliest deadline and put that task in the front of waiting list-----//
		if(listType==1){    
			
			minPoint = list.get(0).abDeadline;
			for(int i=1;i<list.size();i++){
				if(list.get(i).abDeadline<minPoint){
					
					TempSingleTask temptask= list.get(i);
					list.remove(i);
					list.add(0,temptask);
					minPoint = list.get(0).abDeadline;
				}
			}
			
		}
			
				
		//--------For idle list, get the earliest release point and put that task in the front of idle list-----//
		if(listType==2){  //idle list contains tasks to be released and the next event is to release one task.
			
			minPoint = list.get(0).releasePoint;
			for(int i=1;i<list.size();i++){
				if(list.get(i).releasePoint<minPoint||(list.get(i).releasePoint==minPoint&&list
				.get(i).period<list.get(0).period)){
					TempSingleTask temptask = list.get(i);
					list.remove(i);
					list.add(0,temptask);
					minPoint = list.get(0).releasePoint;
				}
			}
		}
		
		return minPoint;         // return the next event point
		
		
	}
    
    public double setVoltage(){
    	
    	double chosen_vol = voltages[voltages.length-1];  // set the maximum voltage initially
		

		
		// to choose the smallest voltage chosen_vol>=uti_sum* vm (the maximum voltage)
		for(int i=0;i<voltages.length;i++){
			
			if(voltages[i]>=proUtili*chosen_vol){
				return voltages[i];
			}
			
		}
		
		return chosen_vol;
    	
    }
    
    public double getUtili(TempSingleTask temptask, double newUtili){
    	
    	double res = 0;
    	for(int i = 0;i<taskList.size();i++){
    		if(taskList.get(i).taskIndex==temptask.taskIndex){
    			res = taskList.get(i).taskUtili;
    			taskList.get(i).taskUtili = newUtili;
    			break;
    		}
    	}
    	
    	return res;
    }

}

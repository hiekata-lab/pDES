/*
 * Copyright (c) 2018, Design Engineering Laboratory, The University of Tokyo.
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package org.pdes.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.pdes.simulator.base.PDES_AbstractSimulator;
import org.pdes.simulator.model.Component;
import org.pdes.simulator.model.Organization;
import org.pdes.simulator.model.Task;
import org.pdes.simulator.model.Workflow;
import org.pdes.simulator.model.Worker;
import org.pdes.simulator.model.base.BaseFacility;
import org.pdes.simulator.model.base.BaseProjectInfo;
import org.pdes.simulator.model.base.BaseTask;
import org.pdes.simulator.model.base.BaseWorker;

/**
 * Simulation to design "Broker Function Position" in Organization.
 * 
 * @author Yoshiaki Oida<yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class PDES_OidaSimulator extends PDES_AbstractSimulator{
	
	//Additional
	static public final int maxTime = 100;
	
	private boolean considerReworkOfErrorTorelance = false;
	
	public PDES_OidaSimulator(BaseProjectInfo project) {
		super(project);
	}

	@Override
	public void execute() {
		this.initialize();
		
		/**
		 * Project Portfolio Initialize
		 */
		
		//Projects
		ArrayList<Component> projectList =this.productList.stream()
				.flatMap(p -> p.getComponentList().stream())
				.collect(
						() -> new ArrayList<>(),
						(l,c) -> l.add((Component)c),
						(l1,l2) -> l1.addAll(l2)
						);
				
		int numOfProject = projectList.size();
		
		System.out.println();
		System.out.println("*** Project Portfolio Information ***");
		System.out.println("Number of project : " + numOfProject);
		System.out.println("Project portfolio : " + projectList);
		projectList.stream().forEach(c -> System.out.println(c.getName() + " : " + c.getTargetedTaskList()));
		
		//Communication Matrix
		//TO DO IMPLEMENT
		
		/**
		 * 1.Initial Allocation
		 * a.Simulate under all same conditions except the following. 
		 * 	 - NOT UPDATE Remaining Work Amount BY Actual Cost
		 * b.Fixed Workers for each Project on the basis of skill set.
		 *   - ex. 1 Worker for project 1, 2 workers for project2. 
		 *  b(easy) is first.
		 */
		System.out.println();
		System.out.println("*** Initial Allocation ***");
		
		//All Workers
		List<Worker> allWorkerList = this.organization.getWorkerList().stream()
				.map(w -> (Worker)w)
				.collect(Collectors.toList());
		
		//Just confirm worker's Executable Task
		allWorkerList.stream().forEach(w -> 
			projectList.stream().forEach(c -> 
				System.out.println(w.getName() +" : "+c.getName()+" "+w.getExecutableUnfinishedTaskList(c))
				)
			);
		
		//Initial Allocation (Assign Worker to Max Skill-Task Matching Project)
		allWorkerList.stream().forEach(w -> 
			w.setCurrentAssignedProject(
					projectList.stream()
					.max((c1,c2) -> w.getExecutableUnfinishedTaskList(c1).size()-w.getExecutableUnfinishedTaskList(c2).size())
					.orElse(null) //null : ResourcePool
					)
			);
		
		//Update Assigned Project Plan
		allWorkerList.stream().forEach(w -> 
			w.setAssignedProjectPlanArray(
					time, 
					0, //start
					w.getCurrentAssignedProject().getDueDate(), //end (this value is excluded.)
					projectList.indexOf(w.getCurrentAssignedProject()) //projectIndex
				)
			);

		//(vice-versa) Add assignment information in Project 
		projectList.stream().forEach(c ->
				c.setWorkerList(
						allWorkerList.stream()
						.filter(w -> c.equals(w.getCurrentAssignedProject()))
						.collect(Collectors.toList())
						)
				);
		
		//Initial Estimation for Initial Allocation
		for(	Component c : projectList) {
			//Estimate Total Work Amount, Completion Time, Required Resources
			c.estimeate(time);
			System.out.println(c.toString());
		}
		
		//Loop max time 
		while(true){
			if(time >= PDES_OidaSimulator.maxTime) {
				System.out.println();
				System.out.println("Time is over. Time "+time+" is larger than max time"+ PDES_OidaSimulator.maxTime+".");
				break;
			}
			
			//0. Check finished or not.
			if(checkAllTasksAreFinished()) return;
			
			/**
			 * ToDo
			 * - Request Class time_to_execute = N　 N-- (for each time step)
			 * - Broker Interface
			 */
			
			//A. Confirm Assigned Project Members on the basis of Assigned Project Plan
			System.out.println();
			System.out.println("t: " +time+ " *** Update Current Assigned Project ***");
			
			//Update Assigned Project Plan
			allWorkerList.stream().forEach(w -> 
				w.setCurrentAssignedProject(w.getLatestAssignedProjectPlanArray()[time] != -1 
						? projectList.get(w.getLatestAssignedProjectPlanArray()[time]) : null)
			);
			//(vice-versa) Add assignment information in Project 
			projectList.stream().forEach(c ->
					c.setWorkerList(
							allWorkerList.stream()
							.filter(w -> c.equals(w.getCurrentAssignedProject()))
							.collect(Collectors.toList())
							)
					);
			
			//Just confirm worker's Executable Task
			allWorkerList.stream().forEach(w -> 
					System.out.println(w.getName() +" : "+(w.getCurrentAssignedProject()!=null ? w.getCurrentAssignedProject().getName():"Resource Pool"))
				);
			
			/**
			 * Allocation としての実績更新が必要
			 * History
			 */

			//B. Project Execution
			System.out.println();
			System.out.println("t: " +time+ " *** Project Execution ***");
			
			for(	Component c : projectList) {
				//1. Get ready task and free resources for each project.
				List<BaseTask> readyTaskList = this.getReadyTaskList(c);
				List<BaseWorker> freeWorkerList = ((Organization)organization).getFreeWorkerList(c);
				List<BaseFacility> freeFacilityList = organization.getFreeFacilityList();//ignore
				
				/**
				 * Don't want change the following part if possible.
				 */
				//2. Sort ready task and free resources
				this.sortTasks(readyTaskList);
				this.sortWorkers(freeWorkerList);
				this.sortFacilities(freeFacilityList);

				//3. Allocate ready tasks to free resources
				this.allocateReadyTasksToFreeResourcesForSingleTaskWorkerSimulation(readyTaskList, freeWorkerList, freeFacilityList);

				//4. Perform WORKING tasks and update the status of each task.
				this.performAndUpdateAllWorkflow(time, c);
				
				//Just Show Unfinished Task List
				System.out.println(c.getName() + "(Finished) : " +c.getTargetedTaskList().stream()
						.filter(t -> t.isFinished())
						.map(t -> (Task)t)
						.collect(Collectors.toList()));
			}
			
			//C. Re-Allocation
			System.out.println();
			System.out.println("t: " +time+ " *** Resource Re-allocation ***");
			
			for(	Component c : projectList) {
				//Estimate Total Remaining Work Amount, Completion Time, Required Resources
				c.estimeate(time);
				System.out.println(c.toString());
				//System.out.println(c.getTargetedTaskList());
				
				//Just Show Unfinished Task List
				System.out.println(c.getName() + "(Unfinished) : " +c.getTargetedTaskList().stream()
						.filter(t -> !t.isFinished())
						.map(t -> (Task)t)
						.collect(Collectors.toList()));
				
//				workerList.stream().forEach(w ->
//						{
//							System.out.print(w.getName()+" : ");
//							Arrays.asList(w.getLatestAssignedProjectPlanArray()).stream()
//						.forEach(ap -> System.out.print(ap+","));
//							System.out.println();
//						}
//				);
				
				//Necessity of Resources based on comparison between Estimated Completion Time and Project Due Date
				double estimatedDelay = c.getEstimatedCompletionTime(time) - c.getDueDate();
				if(estimatedDelay < 0) {
					/**
					 * Release
					 * ct' < dd
					 * ⇔ time + WL'/|RA| < dd
					 * ⇒ time < dd  ∵ WL'/|RA| > 0
					 */
					//Work amount to be released
					double estimatedReleasableWorkAmount = 0;
					for (int t = time+1; t < c.getDueDate(); t++){//t+1 ~ dd
						double numOfResourceAtTime = 0;
						for (Worker w : allWorkerList) {
							if(c.equals(w.getLatestAssignedProjectPlanArray()[t] != -1 
									? projectList.get(w.getLatestAssignedProjectPlanArray()[t]) : null)) numOfResourceAtTime++;
						}
						estimatedReleasableWorkAmount += numOfResourceAtTime;
					}
					estimatedReleasableWorkAmount -= c.getEstimatedTotalWorkAmount();
					
					//No Release-able Resource -> next Project
					if(estimatedReleasableWorkAmount < 0) continue;
					
					//Priority of Workers to be released. Which worker should be released? "Minimum" Matching Skill.
					List<Worker> workerListToBeReleased = allWorkerList.stream()
						.sorted((w1,w2) -> w1.getExecutableUnfinishedTaskList(c).size() - w2.getExecutableUnfinishedTaskList(c).size())//How About Skill Point 
						.collect(Collectors.toList());
					
					//Select time slots to be released. Priority : 1.Worker -> 2.Time(Backward:from due date to current time).
					int projectIndex = projectList.indexOf(c);
					double workAmountToBeReleased = 0;
					for (Worker w : workerListToBeReleased) {
						boolean updateFlag = false;
						for(int t = c.getDueDate()-1; t > time; t--) {
							if(estimatedReleasableWorkAmount <= workAmountToBeReleased) break;
							if(w.getLatestAssignedProjectPlanArray()[t] == projectIndex) {
								w.getLatestAssignedProjectPlanArray()[t] = -1; //Release
								workAmountToBeReleased += 1;
								updateFlag = true;
							}
						}
						//Update AssignedProjectPlanArray
						if(updateFlag) w.setAssignedProjectPlanArray(time, w.getLatestAssignedProjectPlanArray());
					}
				}else if(estimatedDelay > 0){
					/**
					 * Supply Request
					 * ct' > dd
					 * ⇔ time + WL'/|RA| > dd
					 * ⇒ (time < dd) or (time > dd)
					 */
					//Request Span and Work amount to supply request
					//Initialize
					Integer[] requestSpan = new Integer[PDES_OidaSimulator.maxTime];
					Arrays.fill(requestSpan, -1);
					double estimatedLackOfWorkAmount = 0;
					
					int projectIndex = projectList.indexOf(c);
					if(time <= c.getDueDate()) {//time <= due date
						//Request Span(t+1 ~ dd)
						Arrays.fill(requestSpan, time+1, c.getDueDate(), projectIndex);
						
						//Work Amount to be supply requested
						estimatedLackOfWorkAmount = c.getEstimatedTotalWorkAmount();					
						for (int t = time+1; t < c.getDueDate(); t++){//t+1 ~ dd
							double numOfResourceAtTime = 0;
							for (Worker w : allWorkerList) {
								if(c.equals(w.getLatestAssignedProjectPlanArray()[t] != -1 
										? projectList.get(w.getLatestAssignedProjectPlanArray()[t]) : null)) numOfResourceAtTime++;
							}
							estimatedLackOfWorkAmount -= numOfResourceAtTime;
						}
					}else {//time > due date
						//Request Span(t+1 ~ ct)
						Arrays.fill(requestSpan, time+1, (int)c.getEstimatedCompletionTime(time), projectIndex);
						
						//Work Amount as much as possible to be supply requested 
						estimatedLackOfWorkAmount = c.getEstimatedTotalWorkAmount();											
					}
					
					//No lack of Resource -> next Project
					if(estimatedLackOfWorkAmount < 0) continue;
					
					//Priority of Workers to be supplied. Which worker should be supplied? "Maximum" Matching Skill.
					List<Worker> workerListToBeSupplied = allWorkerList.stream()
						.sorted((w1,w2) -> w2.getExecutableUnfinishedTaskList(c).size() - w1.getExecutableUnfinishedTaskList(c).size())//How About Skill Point 
						.collect(Collectors.toList());
					
					if(time <= c.getDueDate()) {//time <= due date
						//Select time slots to be supplied. Priority : 1.Worker -> 2.Time(Forward)
						double workAmountToBeSupplied = 0;
						for (Worker w : workerListToBeSupplied) {
							boolean updateFlag = false;
							for(int t = time+1; t < c.getDueDate(); t++) {
								if(estimatedLackOfWorkAmount <= workAmountToBeSupplied) break;
								if(requestSpan[t] == projectIndex && w.getLatestAssignedProjectPlanArray()[t] == -1) {
									w.getLatestAssignedProjectPlanArray()[t] = projectIndex; //Supplied
									workAmountToBeSupplied += 1;
									updateFlag = true;
								}
							}
							//Update AssignedProjectPlanArray
							if(updateFlag) w.setAssignedProjectPlanArray(time, w.getLatestAssignedProjectPlanArray());
						}
					}else {//time > due date
						//Select time slots to be supplied. Priority : 1.Time(Forward) -> 2.Worker
						double workAmountToBeSupplied = 0;
						for(int t = time+1; t < c.getEstimatedCompletionTime(time); t++) {
							for (Worker w : workerListToBeSupplied) {
								boolean updateFlag = false;
								if(estimatedLackOfWorkAmount <= workAmountToBeSupplied) break;
								if(requestSpan[t] == projectIndex && w.getLatestAssignedProjectPlanArray()[t] == -1) {
									w.getLatestAssignedProjectPlanArray()[t] = projectIndex; //Supplied
									workAmountToBeSupplied += 1;
									updateFlag = true;
								}
								//Update AssignedProjectPlanArray
								if(updateFlag) w.setAssignedProjectPlanArray(time, w.getLatestAssignedProjectPlanArray());
							}
						}
					}	
				}else {
					//nothing
				}
			}
			time++;
		}
	}
	
	/**
	 * Get the list of tasks.
	 * @return
	 */
	public List<BaseTask> getTaskList(Component c){
		return super.workflowList.stream()
				.map(w -> ((Workflow)w).getTaskList(c))
				.collect(
						() -> new ArrayList<>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}

	/**
	 * Get the list of READY tasks.
	 * @return
	 */
	public List<BaseTask> getReadyTaskList(Component c){
		return super.workflowList.stream()
				.map(w -> ((Workflow)w).getReadyTaskList(c))
				.collect(
						() -> new ArrayList<>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}
//	public List<Task> getReadyTaskList(Component c){
//		return super.workflowList.stream()
//				.flatMap(w -> ((Workflow)w).getReadyTaskList(c).stream())
//				.collect(
//						() -> new ArrayList<>(),
//						(l, t) -> l.add((Task)t),
//						(l1, l2) -> l1.addAll(l2)
//						);
//	}
	
	/**
	 * Perform and update project c workflow in this time.
	 * @param time 
	 */
	public void performAndUpdateAllWorkflow(int time, Component c){
		workflowList.forEach(w -> ((Workflow)w).checkWorking(time, c));//READY -> WORKING
		((Organization)organization).getWorkingWorkerList(c).stream().forEach(w -> w.addLaborCost());//pay labor cost
		//((Organization)organization).getWorkingFacilityList(c).stream().forEach(f -> f.addLaborCost());//pay labor cost //ignore
		workflowList.forEach(w -> ((Workflow)w).perform(time, c));//update information of WORKING task in each workflow
		workflowList.forEach(w -> ((Workflow)w).checkFinished(time, c));// WORKING -> WORKING_ADDITIONALLY or FINISHED
		workflowList.forEach(w -> ((Workflow)w).checkReady(time, c));// NONE -> READY
		workflowList.forEach(w -> ((Workflow)w).updatePERTData(c));//Update PERT information
	}
	
//	/**
//	 * Save the simulation result to the given directory.
//	 * @param outputDir
//	 */
//	@Override
//	public void saveResultFilesInDirectory(String outputDir, String fileName){
//		//TO IMPLEMENT Average Project Delay -> CSV Output
//	}
	

}

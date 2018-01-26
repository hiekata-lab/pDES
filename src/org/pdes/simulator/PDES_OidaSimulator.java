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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.pdes.simulator.base.PDES_AbstractSimulator;
import org.pdes.simulator.model.Component;
import org.pdes.simulator.model.Organization;
import org.pdes.simulator.model.Task;
import org.pdes.simulator.model.Workflow;
import org.pdes.simulator.model.Worker;
import org.pdes.simulator.model.base.BaseComponent;
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
	static public final int maxTime = 1000;
	
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
		
		System.out.println("Number of project : " + numOfProject);
		System.out.println("Project portfolio : " + projectList);
		projectList.stream().forEach(c -> System.out.println(c.getId() + "--" + c.getNodeId() + "--" +c.getName() + " : " + c.getTargetedTaskList()));
		
		//Communication Matrix
		//TO DO IMPLEMENT
		
		/**
		 * 1.Initial Allocation
		 * a.Simulate under all same conditions except the following. 
		 * 	 - NOT UPDATE Remaining Work Amount BY Actual Cost
		 * b.Fixed Workers for each Project on the basis of skill set.
		 *   - ex. 1 Worker for project 1, 2 workers for project2. 
		 *   - 
		 *   
		 *  b(easy) is first.
		 */
		
		//Estimate Total Remaining Work Amount for each Project
		HashMap<Component, Double> projectActualTotalWorkAmount = new HashMap<Component, Double>();		
		HashMap<Component, Double> projectEstimatedTotalWorkAmount = new HashMap<Component, Double>();
		for(	Component c : projectList) {
			double actualTotalWorkAmount = this.getTaskList(c).stream()
					.mapToDouble(t -> t.getActualWorkAmount()).sum();
			projectActualTotalWorkAmount.put(c, actualTotalWorkAmount);
			System.out.println(c.getName()+" ATWA: "+actualTotalWorkAmount);
			
			double estimatedTotalWorkAmount = this.getTaskList(c).stream()
					.mapToDouble(t -> t.getRemainingWorkAmount()).sum();
			projectEstimatedTotalWorkAmount.put(c, actualTotalWorkAmount);
			System.out.println(c.getName()+" ETWA: "+estimatedTotalWorkAmount);
		}
		
		//Calculate Required Resources
		HashMap<Component, Double> projectRequiredResource = new HashMap<Component, Double>();
		projectList.stream()
			.forEach(c -> projectRequiredResource
				.put(c,projectEstimatedTotalWorkAmount.get(c)/(c.getDueDate()-time)));	
				
		//All Workers
		List<Worker> workerList = this.organization.getWorkerList().stream()
				.map(w -> (Worker)w)
				.collect(Collectors.toList());
		
		//Initial Assignment Plan
		workerList.stream().forEach(w -> 
			projectList.stream().forEach(c -> 
				System.out.println(w.getName() +" : "+c.getName()+" "+w.getExecutableUnfinishedTaskList(c))
				)
		);
		
		//Max Skill-Matching Project
		workerList.stream().forEach(w -> 
			w.setCurrentAssignedProject(
					projectList.stream()
					.max((c1,c2) -> w.getExecutableUnfinishedTaskList(c1).size()-w.getExecutableUnfinishedTaskList(c2).size())
					.orElse(null) //null : ResourcePool
					)
			);
		
		//ReAllocation is necessary.

//		
//		//Update Current Assigned Project 
//		workerList.get(0).setCurrentAssignedProject(projectList.get(0)); //project 1
//		workerList.get(1).setCurrentAssignedProject(projectList.get(0)); //project 1
//		workerList.get(2).setCurrentAssignedProject(projectList.get(1)); //project 2
		
		while(true){
			//0. Check finished or not.
			if(checkAllTasksAreFinished()) return;
			
			/**
			 * ToDo
			 * 	
			 * - ResourcePool Class　not
			 * - Default Allocation
			 * - not completed task
			 * - Request Class time_to_execute = N　 N-- (for each time step)
			 *  Broker Interface
			 * 
			 * 
			 * Issue 1/24
			 * ２．WorkAmountの持ち方について
			 *    Remaining → 各PMが認識している残コスト
			 *    Actual → 実際のコスト
			 *    Default → 今回は実際のコストを入れるものとする．
			 *    
			 * ４．UIにて，正しい入力を入れて，シミュレータの中で見積誤差を乗せた
			 * 　　スケジュールを算出する．
			 * 　　※もし，本シミュレータを実利用する場合は，真の作業量は知らないはずなので，
			 * 　　シミュレータに入れたものが見積作業量となるが，今回の目的は組織設計なので，
			 * 　　シミュレーションを実利用することは考慮しない．
			 * 
			 * ５.current Assign Project（Component）がnullであれば,
			 *  ResourcePoolとみなしたいが他にいい方法はあるか．
			 * 
			 * 
			 */

			//A. Allocation

			//1. Re-Allocation Algorithm.
			
			//Estimate Total Remaining Work Amount for each Project
			
			//Estimated Completion Time
			
			//Necessity of Resources based on comparison between Estimated Completion Time and Project Due Date
			
			

			
			//B. Project Execution
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
				this.performAndUpdateAllWorkflow(time, considerReworkOfErrorTorelance);
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
	 * Perform and update all workflow in this time.
	 * @param time 
	 * @param componentErrorRework 
	 */
	@Override
	public void performAndUpdateAllWorkflow(int time, boolean componentErrorRework){
		workflowList.forEach(w -> w.checkWorking(time));//READY -> WORKING
		organization.getWorkingWorkerList().stream().forEach(w -> w.addLaborCost());//pay labor cost
		organization.getWorkingFacilityList().stream().forEach(f -> f.addLaborCost());//pay labor cost
		workflowList.forEach(w -> ((Workflow)w).perform(time));//update information of WORKING task in each workflow
		workflowList.forEach(w -> w.checkFinished(time));// WORKING -> WORKING_ADDITIONALLY or FINISHED
		workflowList.forEach(w -> w.checkReady(time));// NONE -> READY
		workflowList.forEach(w -> w.updatePERTData());//Update PERT information
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

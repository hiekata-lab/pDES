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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.pdes.simulator.base.PDES_AbstractSimulator;
import org.pdes.simulator.model.Component;
import org.pdes.simulator.model.ProjectInfo;
import org.pdes.simulator.model.base.BaseComponent;
import org.pdes.simulator.model.base.BaseFacility;
import org.pdes.simulator.model.base.BaseProduct;
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
	
	private boolean considerReworkOfErrorTorelance = false;
	
	public PDES_OidaSimulator(BaseProjectInfo project) {
		super(project);
	}

	@Override
	public void execute() {
		this.initialize();
		
		ArrayList<BaseComponent> projectList_sample = new ArrayList<>();
		for (BaseProduct p : this.productList) {
			for(BaseComponent c : p.getComponentList()) {
				projectList_sample.add(c);
				System.out.println(c);
				System.out.println(c.getTargetedTaskList());
			}	
		}
		//Project Portfolio Initialize
		ArrayList<BaseComponent> projectList =this.productList.stream()
				.map(p -> p.getComponentList())
				.collect(
						() -> new ArrayList<>(),
						(l,c) -> l.addAll(c),
						(l1,l2) -> l1.addAll(l2)
						);
		int numOfProject = projectList.size();
		
		System.out.println("Number of project : " + numOfProject);
		System.out.println("Project portfolio : " + projectList);
		projectList.stream().forEach(c -> System.out.println(c.getName()+" : "+c.getTargetedTaskList()));
		
		
		//projectList.get(0).getAssignedWorkerList(); *to implement
		//assignedProjectListをResourceに追加する
		//
		
		while(true){
			
			//0. Check finished or not.
			if(checkAllTasksAreFinished()) return;
			
			/**
			 * ToDo
			 * 1. To confirm how to call the project member based on the component. 
			 * 
			 * 2. To implement Request Class time_to_execute = N
			 *  N-- (for each time step)
			 *  
			 * 3. To implement ResourcePool Class
			 * 4. To implement Broker Interface
			 * 5. Uncertainty for work load. Expected/True 
			 */

			//0. Projectごとへの配分を決める
			/**
			 * プロジェクトごとの以下を決めればあは，動くと思われる．
			 * ・タスクリスト
			 * ・作業者リスト
			 */
			//1. Get ready task and free resources
			List<BaseTask> readyTaskList = this.getReadyTaskList();
			List<BaseWorker> freeWorkerList = organization.getFreeWorkerList();
			List<BaseFacility> freeFacilityList = organization.getFreeFacilityList();
			
			//2. Sort ready task and free resources
			this.sortTasks(readyTaskList);
			this.sortWorkers(freeWorkerList);
			this.sortFacilities(freeFacilityList);
			
			//3. Allocate ready tasks to free resources
			this.allocateReadyTasksToFreeResourcesForSingleTaskWorkerSimulation(readyTaskList, freeWorkerList, freeFacilityList);
			
			//4. Perform WORKING tasks and update the status of each task.
			this.performAndUpdateAllWorkflow(time, considerReworkOfErrorTorelance);
			time++;
		}
	}

}

/*
 * Copyright (c) 2016, Design Engineering Laboratory, The University of Tokyo.
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

import java.util.List;

import org.pdes.simulator.base.PDES_AbstractSimulator;
import org.pdes.simulator.model.Facility;
import org.pdes.simulator.model.ProjectInfo;
import org.pdes.simulator.model.Task;
import org.pdes.simulator.model.Worker;

/**
 * This is the basic discrete event simulator of this application.<br>
 * This simulator is developed based on Hiroya Matsubara's master thesis.<br>
 * Basic Simulator is the simulator which has no uncertainty (Rework of Error tolerance, etc.).<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class PDES_BasicSimulator extends PDES_AbstractSimulator {
	
	private boolean considerReworkOfErrorTorelance = false;;
	
	/**
	 * This is the constructor.
	 * @param workflowList
	 * @param organization
	 * @param productList
	 * @param simultaneousWorkflowLimit
	 */
	public PDES_BasicSimulator(ProjectInfo project) {
		super(project);
	}

	/* (non-Javadoc)
	 * @see org.pdes.simulator.base.PDES_AbstractSimulator#execute()
	 */
	@Override
	public void execute() {
		this.initialize();
		while(true){
			
			//0. Check finished or not.
			if(checkAllTasksAreFinished()) return;
			
			//1. Get ready task and free resources
			List<Task> readyTaskList = this.getReadyTaskList();
			List<Worker> freeWorkerList = organization.getFreeWorkerList();
			List<Facility> freeFacilityList = organization.getFreeFacilityList();
			
			//2. Sort ready task and free resources
			this.sortTasks(readyTaskList);
			this.sortWorkers(freeWorkerList);
			this.sortFacilities(freeFacilityList);
			
			//3. Allocate ready tasks to free resources
			this.allocateReadyTasksToFreeResources(readyTaskList, freeWorkerList, freeFacilityList);
			
			//4. Perform WORKING tasks and update the status of each task.
			this.performAndUpdateAllWorkflow(time, considerReworkOfErrorTorelance);
			time++;
		}
	}

	/**
	 * Check whether this simulator considers rework of error tolerance or not.
	 * @return the considerReworkOfErrorTorelance
	 */
	public boolean isConsiderReworkOfErrorTorelance() {
		return considerReworkOfErrorTorelance;
	}

	/**
	 * Set the simulation condition whether this simulator considers rework of error tolerance or not.
	 * @param considerReworkOfErrorTorelance the considerReworkOfErrorTorelance to set
	 */
	public void setConsiderReworkOfErrorTorelance(boolean considerReworkOfErrorTorelance) {
		this.considerReworkOfErrorTorelance = considerReworkOfErrorTorelance;
	}
}

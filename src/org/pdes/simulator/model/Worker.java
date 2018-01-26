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
package org.pdes.simulator.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.pdes.rcp.model.WorkerElement;
import org.pdes.simulator.PDES_OidaSimulator;
import org.pdes.simulator.model.base.BaseComponent;
import org.pdes.simulator.model.base.BaseTeam;
import org.pdes.simulator.model.base.BaseWorker;

/**
 * @author Yoshiaki Oida <yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class Worker extends BaseWorker {

	// Changeable variable on simulation
	private Component currentAssignedProject = null; // current assigned project
	private Integer[] assignedProjectPlanArray; // list of assigned project history list.
	private Integer[] assignedTaskPlanArray; // list of assigned task history list.
	private Integer[] assignedProjectHistoryArray; // list of assigned project history list.
	private Integer[] assignedTaskHistoryArray; // list of assigned task history list.
	
	/**
	 * @param workerElement
	 * @param team
	 */
	public Worker(WorkerElement workerElement, BaseTeam team) {
		super(workerElement, team);
	}
	
	/**
	 * Initialize
	 */
	
	@Override
	public void initialize() {
		state = ResourceState.FREE;
		totalCost = 0;
		startTimeList.clear();
		finishTimeList.clear();
		assignedTaskList.clear();
		
		//Additional Initialization
		assignedProjectPlanArray 	= new Integer[PDES_OidaSimulator.maxTime];
		assignedProjectHistoryArray	= new Integer[PDES_OidaSimulator.maxTime];
		assignedTaskPlanArray		= new Integer[PDES_OidaSimulator.maxTime];
		assignedTaskHistoryArray		= new Integer[PDES_OidaSimulator.maxTime];//not use?
		
		for (int time = 0; time < PDES_OidaSimulator.maxTime; time++) {
			assignedProjectHistoryArray[time] = -1;//not assigned
		}
		
		setCurrentAssignedProject(null);
	}
	
	/**
	 * Get executable unfinished Task List regarding each Project 
	 * @param c
	 * @return
	 */
	public List<Task> getExecutableUnfinishedTaskList(Component c) {
		return c.getTargetedTaskList().stream()
				.filter(t -> !t.isFinished())
				.filter(t -> this.getWorkAmountSkillPoint(t) > 0)
				.map(t -> (Task)t)
				.collect(Collectors.toList());
	}

	public BaseComponent getCurrentAssignedProject() {
		return currentAssignedProject;
	}

	public void setCurrentAssignedProject(Component currentAssignedProject) {
		this.currentAssignedProject = currentAssignedProject;
	}

	public Integer[] getAssignedProjectPlanArray() {
		return assignedProjectPlanArray;
	}

	public void setAssignedProjectPlanArray(int time, int ProjectIndex) {
		this.assignedProjectPlanArray[time] = ProjectIndex;
	}

	public Integer[] getAssignedTaskPlanArray() {
		return assignedTaskPlanArray;
	}

	public void setAssignedTaskPlanArray(int time, int TaskIndex) {
		this.assignedTaskPlanArray[time] = TaskIndex;
	}

	public Integer[] getAssignedProjectHistoryArray() {
		return assignedProjectHistoryArray;
	}

	public void setAssignedProjectHistoryArray(int time, int ProjectIndex)  {
		this.assignedProjectHistoryArray[time] = ProjectIndex;
	}

	public Integer[] getAssignedTaskHistoryArray() {
		return assignedTaskHistoryArray;
	}

	public void setAssignedTaskHistoryArray(int time, int TaskIndex) {
		this.assignedTaskHistoryArray[time] = TaskIndex;
	}
	
}

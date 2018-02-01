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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.pdes.rcp.model.WorkerElement;
import org.pdes.simulator.PDES_OidaSimulator;
import org.pdes.simulator.model.base.BaseTeam;
import org.pdes.simulator.model.base.BaseWorker;

/**
 * @author Yoshiaki Oida <yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class Worker extends BaseWorker {

	// Changeable variable on simulation
	private Component currentAssignedProject = null; // current assigned project
	private List<Integer[]> assignedProjectPlanArrayList; // list of assigned project history list.
	private Integer[] latestAssignedProjectPlanArray;
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
		assignedProjectPlanArrayList = new ArrayList<Integer[]>();
		assignedProjectPlanArrayList.add(new Integer[1 + PDES_OidaSimulator.maxTime]);//time + plan array.	
		latestAssignedProjectPlanArray = new Integer[PDES_OidaSimulator.maxTime];
		
		assignedProjectHistoryArray	= new Integer[PDES_OidaSimulator.maxTime];
		assignedTaskHistoryArray		= new Integer[PDES_OidaSimulator.maxTime];
		
		Arrays.fill(assignedProjectPlanArrayList.get(0), -1);//time = -1, not assigned
		Arrays.fill(latestAssignedProjectPlanArray, -1);//not assigned
		Arrays.fill(assignedProjectHistoryArray, -1);//not assigned
		Arrays.fill(assignedTaskHistoryArray, -1);//not assigned
		
		setCurrentAssignedProject(null);
	}
	

	/**
	 * Get executable Task List
	 * @param c
	 * @return
	 */
	public List<Task> getExecutableTaskList(Workflow w) {
		return w.getTaskList().stream()
				.filter(t -> this.getWorkAmountSkillPoint(t) > 0)
				.map(t -> (Task)t)
				.collect(Collectors.toList());
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
	
	/**
	 * Get working Task List regarding a project 
	 * @param c
	 * @return
	 */
	public List<Task> getWorkingTaskList(Component c) {
		return c.getTargetedTaskList().stream()
				.filter(t -> t.isWorking() && t.getAllocatedWorkerList().contains(this))
				.map(t -> (Task)t)
				.collect(Collectors.toList());
	}

	public Component getCurrentAssignedProject() {
		return currentAssignedProject;
	}

	public void setCurrentAssignedProject(Component currentAssignedProject) {
		this.currentAssignedProject = currentAssignedProject;
	}

	public Integer[] getLatestAssignedProjectPlanArray() {
		return this.latestAssignedProjectPlanArray;
	}
	
	//Initial Assignment
	public void setAssignedProjectPlanArray(int time, int start, int end, int projectIndex) {		
		//update latest assigned plan
		Arrays.fill(this.latestAssignedProjectPlanArray, start, end, projectIndex);
		
		//update assigned plan history 
		Integer[] timeOneArray = {time};
		Integer[] tmp = new Integer[latestAssignedProjectPlanArray.length + 1];
		System.arraycopy(timeOneArray, 0, tmp, 0, timeOneArray.length);
		System.arraycopy(this.latestAssignedProjectPlanArray, 0, tmp, timeOneArray.length, this.latestAssignedProjectPlanArray.length);
		this.assignedProjectPlanArrayList.add(tmp);
	}
	
	public void setAssignedProjectPlanArray(int time, Integer[] assignedProjectPlanArray) {
		//update latest assigned plan
		if(assignedProjectPlanArray.length != this.latestAssignedProjectPlanArray.length) {
			Exception e = new Exception("Length of assignedProjectPlanArray is different.");
			e.printStackTrace();
		}
		
		//Need Overtime work
		int timeShift = 0;//Current Work make assignment delayed.
		boolean isDifferentProject = (this.latestAssignedProjectPlanArray[time] != assignedProjectPlanArray[time+1]);
		List<Task> workingList = this.latestAssignedProjectPlanArray[time] != -1 ? 
				this.getWorkingTaskList(PDES_OidaSimulator.projectList.get(this.latestAssignedProjectPlanArray[time])):new ArrayList<Task>() ;
		boolean needOvertime = workingList.size() > 0 && isDifferentProject;
		if(needOvertime) {
			timeShift = (int)Math.ceil(workingList.stream().mapToDouble(w -> w.getRemainingWorkAmount()).sum());
		}
	
		if(!needOvertime) {
			System.arraycopy(assignedProjectPlanArray, time+1, this.latestAssignedProjectPlanArray, time+1, assignedProjectPlanArray.length - time -1);
		}else {
			Arrays.fill(this.latestAssignedProjectPlanArray, time+1, time+1 + timeShift, this.latestAssignedProjectPlanArray[time]);
			System.arraycopy(assignedProjectPlanArray, time+1, this.latestAssignedProjectPlanArray, time+1 + timeShift, assignedProjectPlanArray.length - time -1 - timeShift);	
		}
		
		//update assigned plan history 
		Integer[] timeOneArray = {time};
		Integer[] tmp = new Integer[latestAssignedProjectPlanArray.length + 1];
		System.arraycopy(timeOneArray, 0, tmp, 0, timeOneArray.length);
		System.arraycopy(this.latestAssignedProjectPlanArray, 0, tmp, timeOneArray.length, this.latestAssignedProjectPlanArray.length);
		this.assignedProjectPlanArrayList.add(tmp);
	}
	
	public List<Integer[]> getAssignedProjectPlanArrayList(){
		return assignedProjectPlanArrayList;
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

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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.pdes.rcp.model.TaskNode;

/**
 * Task model for discrete event simulation.
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class Task {
	
	private enum TaskState {
		/** Cannot start task (stateInt=0)*/
		NONE,
		/** Can start task but not start (stateInt=1)*/
		READY,
		/** Doing this task (stateInt=2)*/
		WORKING,
		/** Doing Additional or Exceptional work of this task (stateInt=3)*/
		WORKING_ADDITIONALLY,
		/** Finished this task (stateInt=4)*/
		FINISHED,
	}
	
	// Constraint variables on simulation
	private final String id; // ID
	private final String nodeId; // TaskNode ID
	private final String name;
	private final double defaultWorkAmount;
	private final double additionalWorkAmount;
	private final boolean needFacility;
	private final List<Task> inputTaskList = new ArrayList<>();
	private final List<Task> outputTaskList = new ArrayList<>();
	private int dueDate;
	private Team allocatedTeam;
	private final List<Component> targetComponentList = new ArrayList<>();
	
	// Changeable variable on simulation
	private double est = 0; // Earliest start time
	private double eft = 0; // Earliest finish time
	private double lst = 0; // Latest start time
	private double lft = 0; // Latest finish time
	private double remainingWorkAmount; // remaining work amount
	private double actualWorkAmount; // actual work amount
	private TaskState state = TaskState.NONE; // state of this task
	private int stateInt = 0;
	private int readyTime = 0;
	private int startTime = 0;
	private int finishTime = 0;
	private int additionalStartTime = 0;//start time of additional work
	private int additionalFinishTime = 0; //finish time of additional work
	private boolean additionalTaskFlag = false;
	private Worker allocatedWorker = null;
	private Facility allocatedFacility = null;
	
	/**
	 * This is the constructor.
	 * @param taskNode
	 */
	public Task(TaskNode taskNode) {
		this.id = UUID.randomUUID().toString();
		this.nodeId = taskNode.getId();
		this.name = taskNode.getName();
		this.defaultWorkAmount = taskNode.getWorkAmount();
		this.additionalWorkAmount = taskNode.getAdditionalWorkAmount();
		this.needFacility = taskNode.isNeedFacility();
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		est = 0;
		eft = 0;
		lst = 0;
		lft = 0;
		remainingWorkAmount = defaultWorkAmount;
		actualWorkAmount = defaultWorkAmount;
		state = TaskState.NONE;
		stateInt = 0;
		readyTime = 0;
		startTime = 0;
		finishTime = 0;
		additionalStartTime = 0;
		additionalFinishTime = 0;
		additionalTaskFlag = false;
		allocatedWorker = null;
		allocatedFacility = null;
	}
	
	/**
	 * Add input task.
	 * @param task
	 */
	public void addInputTask(Task task) {
		inputTaskList.add(task);
	}
	
	/**
	 * Add output task.
	 * @param task
	 */
	public void addOutputTask(Task task) {
		outputTaskList.add(task);
	}
	
	/**
	 * Add target component of this task.
	 * @param component
	 */
	public void addTargetComponent(Component component) {
		targetComponentList.add(component);
	}
	
	/**
	 * Check whether this task has to be READY or not.<br>
	 * If all input tasks are FINISHED and the state of this task is NONE, change the state of this task to READY.
	 */
	public void checkReady(int time) {
		if (isNone() && inputTaskList.stream().allMatch(t -> t.isFinished())){
			state = TaskState.READY;
			stateInt = 1;
			readyTime = time;
		}
	}
	
	/**
	 * Check whether the state of this task has to be WORKING or not.<br>
	 * If the state of this task is READY and this task is already allocated someone,
	 * change the state of this task and allocated resources to WORKING, and add the information of start time to this task.
	 * @param time
	 */
	public void checkWorking(int time) {
		if (isReady() && allocatedWorker != null) {
			state = TaskState.WORKING;
			stateInt = 2;
			startTime = time;
			allocatedWorker.setStateWorking();
			allocatedWorker.addStartTime(time);
			allocatedWorker.addWorkedTask(this);
			if (needFacility) {
				allocatedFacility.setStateWorking();
				allocatedFacility.addStartTime(time);
				allocatedFacility.addWorkedTask(this);
			}
		}
	}
	
	/**
	 * If remaining work amount of this task is lower than 0, check whether the state of this task has to be FINISHED or not.<br>
	 * Whether additional work is assigned to this task or not is judged by additionalTaskFlag.
	 * If finishing normally, the state of this task is changed to FINISHED and the state of assigned resource is changed to FREE, and record the finish time to this task and assigned resource.
	 * If additional work is assigned, add the additional work amount to remaining work amount and record the start time of additional work.
	 * @param time
	 */
	public void checkFinished(int time) {
		if (remainingWorkAmount <= 0) {
			if (isWorking()) {
				finishTime = time;
				remainingWorkAmount = 0;
				
				if (additionalTaskFlag) {
					//Additional work
					state = TaskState.WORKING_ADDITIONALLY;
					stateInt = 3;
					remainingWorkAmount = additionalWorkAmount;
					actualWorkAmount += additionalWorkAmount;
					additionalStartTime = time + 1;
					additionalTaskFlag = false;
				} else {
					//Finish normally.
					state = TaskState.FINISHED;
					stateInt = 4;
					allocatedWorker.setStateFree();
					allocatedWorker.addFinishTime(time);
					if (needFacility) {
						allocatedFacility.setStateFree();
						allocatedFacility.addFinishTime(time);
					}
				}
			} else if (isWorkingAdditionally()) {
				additionalFinishTime = time;
				remainingWorkAmount = 0;
				state = TaskState.FINISHED;
				stateInt = 4;
				allocatedWorker.setStateFree();
				allocatedWorker.addFinishTime(time);
				if (needFacility) {
					allocatedFacility.setStateFree();
					allocatedFacility.addFinishTime(time);
				}
			}
		}
	}
	
	/**
	 * Performing this task by following steps:<br>
	 * 1. Decreasing remaining work amount and adding cost to resource.<br>
	 * 2. Updating error value considering quality skill point.<br>
	 * 3. Judging additional work is occurred or not by checking the error value.(if componentErrorRework is TRUE)
	 * @param time
	 * @param componentErrorRework
	 */
	public void perform(int time, boolean componentErrorRework) {
		if (isWorking() || isWorkingAdditionally()) {
			double workAmount = allocatedWorker.getWorkAmountSkillPoint(this);
			double noErrorProbability = 1.0 - allocatedWorker.getQualitySkillPoint(this); // Probability of success this task
			allocatedWorker.work();
			if (needFacility) {
				workAmount *= allocatedFacility.getWorkAmountSkillPoint(this);
				noErrorProbability *= 1.0 - allocatedFacility.getQualitySkillPoint(this);
				allocatedFacility.work();
			}
			remainingWorkAmount -= workAmount;
			for (Component c : targetComponentList) {
				c.updateErrorValue(noErrorProbability);
			}

			// Additional work
			if(componentErrorRework){
				if (isWorking() && hasAdditionalTask() && !additionalTaskFlag) { // because additional work of each task is occurred only once in this simulation.
					// If additional work is occurred, all related components is added to additionalTaskFlag.
					if (targetComponentList.stream().anyMatch(c -> c.checkIfErrorIsOverTolerance())) additionalTaskFlag = true;
				}
			}
		}
	}
	
	/**
	 * Check whether the state of this task is NONE.
	 * @return
	 */
	public boolean isNone() {
		return state == TaskState.NONE;
	}
	
	/**
	 * Check whether the state of this task is READY.
	 * @return
	 */
	public boolean isReady() {
		return state == TaskState.READY;
	}
	
	/**
	 * Check whether the state of this task is WORKING.
	 * @return
	 */
	public boolean isWorking() {
		return state == TaskState.WORKING;
	}
	
	/**
	 * Check whether the state of this task is ADDITIONAL WORKING.
	 * @return
	 */
	public boolean isWorkingAdditionally() {
		return state == TaskState.WORKING_ADDITIONALLY;
	}
	
	/**
	 * Check whether the state of this task is FINISHED.
	 * @return
	 */
	public boolean isFinished() {
		return state == TaskState.FINISHED;
	}
	
	/**
	 * Check whether this task has the attributes of additional work.
	 * @return
	 */
	public boolean hasAdditionalTask() {
		return additionalWorkAmount > 0;
	}
	
	/**
	 * Get the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get the node id.
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * Get the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the default or initial work amount.
	 * @return the defaultWorkAmount
	 */
	public double getDefaultWorkAmount() {
		return defaultWorkAmount;
	}
	
	/**
	 * Get the default or initial work amount.
	 * @return the defaultAdditionalWorkAmount
	 */
	public double getDefaultAdditionalWorkAmount() {
		return additionalWorkAmount;
	}

	/**
	 * Need facility or not for performing this task.
	 * @return the needFacility
	 */
	public boolean isNeedFacility() {
		return needFacility;
	}

	/**
	 * Get the list of target component.
	 * @return the targetComponentList
	 */
	public List<Component> getTargetComponentList() {
		return targetComponentList;
	}

	/**
	 * Get allocated team of this task.
	 * @return the allocatedTeam
	 */
	public Team getAllocatedTeam() {
		return allocatedTeam;
	}

	/**
	 * Set allocated team of this task.
	 * @param allocatedTeam the allocatedTeam to set
	 */
	public void setAllocatedTeam(Team allocatedTeam) {
		this.allocatedTeam = allocatedTeam;
	}

	/**
	 * Get the list of input tasks.
	 * @return the inputTaskList
	 */
	public List<Task> getInputTaskList() {
		return inputTaskList;
	}

	/**
	 * GEt the list of output tasks.
	 * @return the outputTaskList
	 */
	public List<Task> getOutputTaskList() {
		return outputTaskList;
	}

	/**
	 * Get the due date.
	 * @return the dueDate
	 */
	public int getDueDate() {
		return dueDate;
	}

	/**
	 * Set the due date.
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * Get the earliest start time.
	 * @return the est
	 */
	public double getEst() {
		return est;
	}

	/**
	 * Set the earliest start time.
	 * @param est the est to set
	 */
	public void setEst(double est) {
		this.est = est;
	}

	/**
	 * Get the earliest finish time.
	 * @return the eft
	 */
	public double getEft() {
		return eft;
	}

	/**
	 * Set the earliest finish time.
	 * @param eft the eft to set
	 */
	public void setEft(double eft) {
		this.eft = eft;
	}

	/**
	 * Get the latest start time.
	 * @return the lst
	 */
	public double getLst() {
		return lst;
	}

	/**
	 * Set the latest start time.
	 * @param lst the lst to set
	 */
	public void setLst(double lst) {
		this.lst = lst;
	}

	/**
	 * Get the latest finish time.
	 * @return the lft
	 */
	public double getLft() {
		return lft;
	}

	/**
	 * Set the latest start time.
	 * @param lft the lft to set
	 */
	public void setLft(double lft) {
		this.lft = lft;
	}
	/**
	 * Get the remaining work amount.
	 * @return the remainingWorkAmount
	 */
	public double getRemainingWorkAmount() {
		return remainingWorkAmount;
	}

	/**
	 * Get the actual work amount.
	 * @return the actualWorkAmount
	 */
	public double getActualWorkAmount() {
		return actualWorkAmount;
	}
	
	

	/**
	 * Get the state(int) of this task.
	 * @return the stateInt
	 */
	public int getStateInt() {
		return stateInt;
	}

	/**
	 * Get the ready time
	 * @return the readyTime
	 */
	public int getReadyTime() {
		return readyTime;
	}

	/**
	 * Get the start time.
	 * @return the startTime
	 */
	public int getStartTime() {
		return startTime;
	}
	/**
	 * Get the finish time.
	 * @return the finishTime
	 */
	public int getFinishTime() {
		return finishTime;
	}
	/**
	 * Get the additional start time.
	 * @return the additionalStartTime
	 */
	public int getAdditionalStartTime() {
		return additionalStartTime;
	}

	/**
	 * Get the additional finish time.
	 * @return the additionalFinishTime
	 */
	public int getAdditionalFinishTime() {
		return additionalFinishTime;
	}

	/**
	 * Get the allocated worker.
	 * @return the allocatedWorker
	 */
	public Worker getAllocatedWorker() {
		return allocatedWorker;
	}

	/**
	 * Set the allocated worker.
	 * @param allocatedWorker the allocatedWorker to set
	 */
	public void setAllocatedWorker(Worker allocatedWorker) {
		this.allocatedWorker = allocatedWorker;
	}

	/**
	 * Get the allocated facility.
	 * @return the workingFacility
	 */
	public Facility getAllocatedFacility() {
		return allocatedFacility;
	}

	/**
	 * Set the allocated facility.
	 * @param allocatedFacility the allocatedFacility to set
	 */
	public void setAllocatedFacility(Facility allocatedFacility) {
		this.allocatedFacility = allocatedFacility;
	}
	
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		String worker = (allocatedWorker != null) ? allocatedWorker.getName() : "";
		String facility = (allocatedFacility != null) ? allocatedFacility.getName() : "";
		String inputTaskNames = String.join(",", inputTaskList.stream().map(t -> t.getName()).collect(Collectors.toList())); // DEBUG
		return String.format("[%s] %s WA=%f team=%s w=%s f=%s in=%s", name, state, remainingWorkAmount, allocatedTeam.getName(), worker, facility, inputTaskNames);
	}
}

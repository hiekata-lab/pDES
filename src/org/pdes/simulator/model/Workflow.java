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

/**
 * Workflow model for discrete event simulation.<br>
 * 
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class Workflow {
	private String id;
	private final int dueDate; // Due or Submission date. (for utilizing the priority of each workflow in this application)
	private final List<Task> taskList;
	private int time = 0;
	
	private double criticalPathLength = 0;
	
	/**
	 * This is the constructor.
	 * @param taskList
	 */
	public Workflow(List<Task> taskList) {
		this(0, taskList);
	}
	
	/**
	 * This is the constructor.
	 * @param dueDate
	 * @param taskList
	 */
	public Workflow(int dueDate, List<Task> taskList) {
		this.id = UUID.randomUUID().toString();
		this.dueDate = dueDate;
		this.taskList = taskList;
		taskList.forEach(t -> t.setDueDate(dueDate));
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		taskList.forEach(t -> t.initialize());
		time = 0;
		criticalPathLength = 0;
		updatePERTData();
		checkReady();
	}
	
	/**
	 * Update the information of PERT attributes at this moment.
	 */
	public void updatePERTData() {
		setEstEftData();
		setLstLftData();
	}
	
	/**
	 * Calculate earliest start / finish time of all tasks by using only remaining work amount.
	 */
	private void setEstEftData(){
		List<Task> inputTaskList = new ArrayList<Task>();
		
		// 1. Set the earliest finish time of head tasks.
		for(Task task : taskList){
			if(task.getInputTaskList().size()==0){
				task.setEft(task.getRemainingWorkAmount());
				inputTaskList.add(task);
			}
		}
		
		// 2. Calculate PERT information of all tasks
		while (true){
			if(inputTaskList.size() == 0) break;
			List<Task> nextTaskList = new ArrayList<Task>();
			for(Task inputTask : inputTaskList){
				for(Task task : taskList){
					List<Task> _inputTaskList = task.getInputTaskList();
					for(Task _inputTask : _inputTaskList){
						if(inputTask.equals(_inputTask)){
							Double preEst = task.getEst();
							Double inputEst = inputTask.getEst();
							Double est = Double.valueOf(inputEst) + inputTask.getRemainingWorkAmount();
							Double eft = Double.valueOf(est) + task.getRemainingWorkAmount();
							if(est >= preEst){
								task.setEst(est);
								task.setEft(eft);
								for (int l = 0; l < nextTaskList.size(); l++) {
									if (nextTaskList.get(l).getId().equals(task.getId())) {
										nextTaskList.remove(l);
									}
								}
								nextTaskList.add(task);
							}
						}
					}
				}
			}
			inputTaskList = nextTaskList;
		}
	}
	
	/**
	 * Calculate latest start / finish time of all tasks by using only remaining work amount.
	 */
	private void setLstLftData(){
		List<Task> lateTaskList = new ArrayList<Task>();
		
		//1. Extract the list of tail tasks.
		List<String> lastTaskIdList = taskList.stream().map(task -> task.getId()).collect(Collectors.toList());
		for(Task task : taskList){
			for(Task inputTask : task.getInputTaskList()){
				String inputTaskId = inputTask.getId();
				for(int k=0; k< lastTaskIdList.size(); k++){
					String id = lastTaskIdList.get(k);
					if(id.equals(inputTaskId)){
						lastTaskIdList.remove(k);
						break;
					}
				}
			}
		}
		
		//2. Update the information of critical path of this workflow.
		for(String lastTaskId : lastTaskIdList){
			for(Task task : taskList){
				if(lastTaskId.equals(task.getId())) {
					lateTaskList.add(task);
					if(criticalPathLength < task.getEft()) criticalPathLength = task.getEft();
				}
			}
		}
		
		
		//3. Calculate the PERT information of all tasks.
		for(Task task : taskList){
			for(Task lateTask : lateTaskList){
				if(task.getId().equals(lateTask.getId())){
					task.setLft(criticalPathLength);
					task.setLst(criticalPathLength - task.getRemainingWorkAmount());
					registerLsLf(task);
				}
			}
		}
		
	}
	
	/**
	 * Calculate latest start / finish time of all tasks by using only remaining work amount.
	 * @param task
	 */
	private void registerLsLf(Task task) {
		double length = task.getLst();
		
		List<Task> inputTaskList = new ArrayList<Task>();
		for(Task inputTask : taskList){
			for(Task it : task.getInputTaskList()){
				String inputId = it.getId();
				if(inputTask.getId().equals(inputId)) inputTaskList.add(inputTask);
			}
		}
		
		for(Task inputTask : inputTaskList){
			if (inputTask.getLft() == 0
					|| inputTask.getLft() > length) { 
				
				inputTask.setLft(length);
				inputTask.setLst(length - (inputTask.getRemainingWorkAmount()));
				
				registerLsLf(inputTask);
			}
		}
	}
	
	/**
	 * Check the Task which id is the same as "id" has existed.
	 * @param id
	 * @return
	 */
	public boolean hasTask(String id){
		return taskList.stream().filter(t -> t.getId().equals(id)).collect(Collectors.toList()).size() > 0;
	}
	
	/**
	 * Get the Task which id is the same as "id".
	 * @param id
	 * @return
	 */
	public Task getTask(String id) {
		for (Task task : taskList) {
			if (task.getId().equals(id)) return task;
		}
		return null;
	}
	
	/**
	 * Get the Task which name is the same as "name".
	 * @param name
	 * @return
	 */
	public Task getTaskByName(String name) {
		for (Task task : taskList) {
			if (task.getName().equals(name)) return task;
		}
		return null;
	}
	
	/**
	 * Check whether this workflow do not start.
	 * @return
	 */
	public boolean isBeforeStart() {
		return taskList.stream().allMatch(t -> t.isNone() || t.isReady());
	}
	
	/**
	 * Check whether this workflow is FINISHED.
	 * @return
	 */
	public boolean isRunning() {
		return !isFinished() && !isBeforeStart();
	}
	
	/**
	 * Check if all task is FINISHED.
	 * @return
	 */
	public boolean isFinished() {
		return taskList.stream().allMatch(t -> t.isFinished());
	}
	
	/**
	 * Change the state of each task to READY if necessary.
	 */
	public void checkReady() {
		taskList.forEach(t -> t.checkReady());
	}
	
	/**
	 * Change the state of each task to WORKING if necessary.
	 */
	public void checkWorking() {
		taskList.forEach(t -> t.checkWorking(time));
	}
	
	/**
	 * Change the state of each task to FINISH if necessary.
	 */
	public void checkFinished() {
		taskList.forEach(t -> t.checkFinished(time));
	}
	
	/**
	 * Perform all tasks and forwarding time.
	 * @param componentErrorRework 
	 * @param time
	 */
	public void perform(boolean componentErrorRework) {
		taskList.forEach(t -> t.perform(time, componentErrorRework));
		time++;
	}
	
	/**
	 * Get the list of READY tasks.
	 * @return
	 */
	public List<Task> getReadyTaskList() {
		return taskList.stream().filter(t -> t.isReady()).collect(Collectors.toList());
	}
	
	/**
	 * Get the total scheduled work amount after simulation.
	 * @return
	 */
	public double getTotalWorkAmount() {
		return taskList.stream().mapToDouble(t -> t.getDefaultWorkAmount()).sum();
	}
	
	/**
	 * Get the total actual work amount after simulation.
	 * @return
	 */
	public double getTotalActualWorkAmount() {
		return taskList.stream().mapToDouble(t -> t.getActualWorkAmount()).sum();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the due date.
	 * @return the dueDate
	 */
	public int getDueDate() {
		return dueDate;
	}

	/**
	 * Get the list of tasks.
	 * @return the taskList
	 */
	public List<Task> getTaskList() {
		return taskList;
	}

	/**
	 * Get the time.
	 * @return the time
	 */
	public int getTime() {
		return time;
	}
	
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		return "t=" + time + "\n" + String.join("\n", taskList.stream().map(t -> t.toString()).collect(Collectors.toList()));
	}
}

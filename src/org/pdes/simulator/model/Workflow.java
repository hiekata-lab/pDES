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
import java.util.stream.Collectors;

import org.pdes.simulator.model.base.BaseComponent;
import org.pdes.simulator.model.base.BaseTask;
import org.pdes.simulator.model.base.BaseWorkflow;

/**
 * @author Yoshiaki Oida <yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class Workflow extends BaseWorkflow {

	/**
	 * @param taskList
	 */
	public Workflow(List<BaseTask> taskList) {
		super(taskList);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param dueDate
	 * @param taskList
	 */
	public Workflow(int dueDate, List<BaseTask> taskList) {
		super(dueDate, taskList);
		// TODO Auto-generated constructor stub
	}
	
	/*--Additional Method--*/
	
	/**
	 * Initialize
	 */
	@Override
	public void initialize() {
		super.getTaskList().forEach(t -> ((Task)t).initialize());
		super.setCriticalPathLength(0);
		super.updatePERTData(0);
		super.checkReady(0);
	}
	
	/**
	 * Get the list of tasks.
	 * @return
	 */
	public List<BaseTask> getTaskList(BaseComponent c) {
		return super.getTaskList().stream()
				.filter(t -> t.getTargetComponentList().contains(c)) //Project 
				.collect(Collectors.toList());
	}
	
	/**
	 * Get the list of READY tasks.
	 * @return
	 */
	public List<BaseTask> getReadyTaskList(BaseComponent c) {
		return super.getTaskList().stream()
				.filter(t -> t.getTargetComponentList().contains(c)) //Project 
				.filter(t -> t.isReady()).collect(Collectors.toList());
	}
	
	/**
	 * Perform all tasks and forwarding time.
	 * @param time
	 */
	public void perform(int time) {
		super.getTaskList().forEach(t -> ((Task)t).perform(time));
	}
	
	/**
	 * Perform project c tasks and forwarding time.
	 * @param time
	 * @param c : stands for a project.
	 */
	public void perform(int time, Component c) {
		super.getTaskList().stream()
		.filter(t -> t.getTargetComponentList().contains(c)) //Project 
		.forEach(t -> ((Task)t).perform(time));
	}
	
	/**
	 * Change the state of each task to READY if necessary.
	 */
	public void checkReady(int time, Component c)  {
		super.getTaskList().stream()
		.filter(t -> t.getTargetComponentList().contains(c)) //Project 
		.forEach(t -> t.checkReady(time));
	}
	
	/**
	 * Change the state of each task to WORKING if necessary.
	 */
	public void checkWorking(int time, Component c)  {
		super.getTaskList().stream()
		.filter(t -> t.getTargetComponentList().contains(c)) //Project 
		.forEach(t -> ((Task)t).checkWorking(time,c));
	}
	
	/**
	 * Change the state of each task to FINISH if necessary.
	 */
	public void checkFinished(int time, Component c)  {
		super.getTaskList().stream()
		.filter(t -> t.getTargetComponentList().contains(c)) //Project 
		.forEach(t -> t.checkFinished(time));
	}
	
	/**
	 * Update the information of PERT attributes at this moment.
	 */
	public void updatePERTData(int time, Component c) {
		setEstEftData(time, c);
		setLstLftData(c);
	}
	
	/**
	 * Calculate earliest start / finish time of all tasks by using only remaining work amount.
	 */
	private void setEstEftData(int time, Component c){
		List<BaseTask> superTaskList = super.getTaskList().stream()
		.filter(t -> t.getTargetComponentList().contains(c))
		.collect(Collectors.toList());
		
		List<BaseTask> inputTaskList = new ArrayList<BaseTask>();
		
		// 1. Set the earliest finish time of head tasks.
		for(BaseTask task : superTaskList){
			task.setEst(time); // for initializing
			if(task.getInputTaskList().size()==0){
				task.setEft(time + task.getRemainingWorkAmount());
				inputTaskList.add(task);
			}
		}
		
		// 2. Calculate PERT information of all tasks
		while (true){
			if(inputTaskList.size() == 0) break;
			List<BaseTask> nextTaskList = new ArrayList<BaseTask>();
			for(BaseTask inputTask : inputTaskList){
				for(BaseTask task : superTaskList){
					List<BaseTask> _inputTaskList = task.getInputTaskList();
					for(BaseTask _inputTask : _inputTaskList){
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
	private void setLstLftData(Component c){
		List<BaseTask> superTaskList = super.getTaskList().stream()
		.filter(t -> t.getTargetComponentList().contains(c))
		.collect(Collectors.toList());
		
		List<BaseTask> lateTaskList = new ArrayList<BaseTask>();
		
		//1. Extract the list of tail tasks.
		List<String> lastTaskIdList = superTaskList.stream().map(task -> task.getId()).collect(Collectors.toList());
		for(BaseTask task : superTaskList){
			for(BaseTask inputTask : task.getInputTaskList()){
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
			for(BaseTask task : superTaskList){
				if(lastTaskId.equals(task.getId())) {
					lateTaskList.add(task);
					if(getCriticalPathLength() < task.getEft()) setCriticalPathLength(task.getEft());
				}
			}
		}
		
		
		//3. Calculate the PERT information of all tasks.
		for(BaseTask task : superTaskList){
			for(BaseTask lateTask : lateTaskList){
				if(task.getId().equals(lateTask.getId())){
					task.setLft(getCriticalPathLength());
					task.setLst(getCriticalPathLength() - task.getRemainingWorkAmount());
					registerLsLf(task, c);
				}
			}
		}
		
	}
	
	/**
	 * Calculate latest start / finish time of all tasks by using only remaining work amount.
	 * @param task
	 */
	private void registerLsLf(BaseTask task, Component c) {
		List<BaseTask> superTaskList = super.getTaskList().stream()
		.filter(t -> t.getTargetComponentList().contains(c))
		.collect(Collectors.toList());
		
		double length = task.getLst();
		
		List<BaseTask> inputTaskList = new ArrayList<BaseTask>();
		for(BaseTask inputTask : superTaskList){
			for(BaseTask it : task.getInputTaskList()){
				String inputId = it.getId();
				if(inputTask.getId().equals(inputId)) inputTaskList.add(inputTask);
			}
		}
		
		for(BaseTask inputTask : inputTaskList){
			
			if (inputTask.getLft() <= length) { 
				
				inputTask.setLft(length);
				inputTask.setLst(length - (inputTask.getRemainingWorkAmount()));	
				registerLsLf(inputTask,c);
			}
		}
	}
	
//	/**
//	 * Calculate earliest start / finish time of all tasks by using only remaining work amount.
//	 */
//	private void setEstEftData(int time, Component c){
//		List<BaseTask> superTaskList = super.getTaskList().stream()
//		.filter(t -> t.getTargetComponentList().contains(c))
//		.collect(Collectors.toList());
//		
//		List<BaseTask> inputTaskList = new ArrayList<BaseTask>();
//		
//		// 1. Set the earliest finish time of head tasks.
//		for(BaseTask task : superTaskList){
//			if(task.getInputTaskList().size()==0){
//				task.setEst(time);
//				task.setEft(time + task.getRemainingWorkAmount());
//				inputTaskList.add(task);
//			}
//		}
//		
//		// 2. Calculate PERT information of all tasks
//		while (true){
//			if(inputTaskList.size() == 0) break;
//			List<BaseTask> nextTaskList = new ArrayList<BaseTask>();
//			for(BaseTask inputTask : inputTaskList){
//				for(BaseTask task : superTaskList){
//					List<BaseTask> _inputTaskList = task.getInputTaskList();
//					for(BaseTask _inputTask : _inputTaskList){
//						if(inputTask.equals(_inputTask)){
//							Double preEst = task.getEst();
//							Double inputEst = inputTask.getEst();
//							Double est = Double.valueOf(inputEst) + inputTask.getRemainingWorkAmount();
//							Double eft = Double.valueOf(est) + task.getRemainingWorkAmount();
//							if(est > preEst){
//								task.setEst(est);
//								task.setEft(eft);
//								for (int l = 0; l < nextTaskList.size(); l++) {
//									if (nextTaskList.get(l).getId().equals(task.getId())) {
//										nextTaskList.remove(l);
//									}
//								}
//								nextTaskList.add(task);
//							}
//						}
//					}
//				}
//			}
//			inputTaskList = nextTaskList;
//		}
//	}
//	
//	/**
//	 * Calculate latest start / finish time of all tasks by using only remaining work amount.
//	 */
//	private void setLstLftData(Component c){
//		List<BaseTask> superTaskList = super.getTaskList().stream()
//		.filter(t -> t.getTargetComponentList().contains(c))
//		.collect(Collectors.toList());
//		
//		List<BaseTask> lateTaskList = new ArrayList<BaseTask>();
//		
//		//1. Extract the list of tail tasks.
//		List<String> lastTaskIdList = superTaskList.stream().map(task -> task.getId()).collect(Collectors.toList());
//		for(BaseTask task : superTaskList){
//			for(BaseTask inputTask : task.getInputTaskList()){
//				String inputTaskId = inputTask.getId();
//				for(int k=0; k< lastTaskIdList.size(); k++){
//					String id = lastTaskIdList.get(k);
//					if(id.equals(inputTaskId)){
//						lastTaskIdList.remove(k);
//						break;
//					}
//				}
//			}
//		}
//		
//		//2. Update the information of critical path of this workflow.
//		for(String lastTaskId : lastTaskIdList){
//			for(BaseTask task : superTaskList){
//				if(lastTaskId.equals(task.getId())) {
//					lateTaskList.add(task);
//					if(getCriticalPathLength() < task.getEft()) setCriticalPathLength(task.getEft());
//				}
//			}
//		}
//		
//		
//		//3. Calculate the PERT information of all tasks.
//		for(BaseTask task : superTaskList){
//			for(BaseTask lateTask : lateTaskList){
//				if(task.getId().equals(lateTask.getId())){
//					task.setLft(getCriticalPathLength());
//					task.setLst(getCriticalPathLength() - task.getRemainingWorkAmount());
//					registerLsLf(task,c);
//				}
//			}
//		}
//		
//	}
//	
//	/**
//	 * Calculate latest start / finish time of all tasks by using only remaining work amount.
//	 * @param task
//	 */
//	private void registerLsLf(BaseTask task, Component c) {
//		List<BaseTask> superTaskList = super.getTaskList().stream()
//		.filter(t -> t.getTargetComponentList().contains(c))
//		.collect(Collectors.toList());
//		
//		double length = task.getLst();
//		
//		List<BaseTask> inputTaskList = new ArrayList<BaseTask>();
//		for(BaseTask inputTask : superTaskList){
//			for(BaseTask it : task.getInputTaskList()){
//				String inputId = it.getId();
//				if(inputTask.getId().equals(inputId)) inputTaskList.add(inputTask);
//			}
//		}
//		
//		for(BaseTask inputTask : inputTaskList){
//			if (inputTask.getLft() == 0
//					|| inputTask.getLft() > length) { 
//				
//				inputTask.setLft(length);
//				inputTask.setLst(length - (inputTask.getRemainingWorkAmount()));
//				
//				registerLsLf(inputTask,c);
//			}
//		}
//	}


}

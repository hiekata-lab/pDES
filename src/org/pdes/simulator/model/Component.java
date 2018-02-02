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

import org.pdes.rcp.model.ComponentNode;
import org.pdes.simulator.PDES_OidaSimulator;
import org.pdes.simulator.model.base.BaseComponent;

/**
 * @author Yoshiaki Oida <yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class Component extends BaseComponent {
	private final double sigma;
	private final int dueDate;//[0,time = dueDate-1] or [0,dueDate)
	private double actualTotalWorkAmount;

	//Changeable variables
	private List<Worker> workerList = new ArrayList<Worker>();
	private double estimatedCompletionTime;
	private double estimatedTotalWorkAmount;
	private double estimatedRequiredResource;
	private double estimatedDelay;
	private int start;
	private int finish;

	/**
	 * @param componentNode
	 */
	public Component(ComponentNode componentNode) {
		super(componentNode);
		this.sigma = componentNode.getSigma();
		this.dueDate = componentNode.getDueDate(); 
		
		this.start = -1;
		this.finish = -1;
	}
	
	/**
	 * Initialize
	 */
	@Override
	public void initialize() {
		this.actualTotalWorkAmount = this.getTargetedTaskList().stream()
				.mapToDouble(t -> t.getActualWorkAmount()).sum();
		
		this.workerList.clear();
		this.estimatedTotalWorkAmount = Double.POSITIVE_INFINITY;
		this.estimatedCompletionTime = Double.POSITIVE_INFINITY;
		this.estimatedRequiredResource = Double.POSITIVE_INFINITY;
	}

	public double getSigma() {
		return sigma;
	}
	public int getDueDate() {
		return dueDate;
	}

	public double getActualTotalWorkAmount() {
		return this.actualTotalWorkAmount;
	}
	
	/**
	 * Estimate Total Work Amount, Completion Time, Required Resources
	 * @param time
	 */
	public void estimeate(int time) {
		this.estimatedTotalWorkAmount = this.getTargetedTaskList().stream()
				.mapToDouble(t -> t.getRemainingWorkAmount()).sum();
		this.estimatedCompletionTime = time + this.getEstimatedTotalWorkAmount()/this.getWorkerList().size();	
		this.estimatedRequiredResource = this.getEstimatedTotalWorkAmount()/(this.getDueDate() - time);
		this.estimatedDelay = this.estimatedCompletionTime - this.getDueDate();
	}
	
	public double estimateReleasableWorkAmount(int time) {
		double estimatedReleasableWorkAmount = 0;
		for (int t = time+1; t < this.getDueDate()+1; t++){//t+1 ~ dd
			double numOfResourceAtTime = 0;
			for (Worker w : PDES_OidaSimulator.allWorkerList) {
				if(PDES_OidaSimulator.projectList.indexOf(this) == (w.getLatestAssignedProjectPlanArray()[t])) numOfResourceAtTime++;
			}
			estimatedReleasableWorkAmount += numOfResourceAtTime;
		}
		estimatedReleasableWorkAmount -= this.getEstimatedTotalWorkAmount();	
		return estimatedReleasableWorkAmount;
	}
	
	//PM->WR
	public List<Request> sendRelease(int time) {
		List<Request> releaseList  = new ArrayList<Request>();
		
		//(PM) Estimate Releasable Work Amount
		double estimatedReleasableWorkAmount = this.estimateReleasableWorkAmount(time);
		
		//No Release-able Resource -> next Project
		if(estimatedReleasableWorkAmount < 0) return releaseList;
		
		//(PM) Priority of Workers to be released. Which worker should be released? "Minimum" Matching Skill.
		List<Worker> workerListToBeReleased = PDES_OidaSimulator.allWorkerList.stream()
			.sorted((w1,w2) -> w1.getExecutableUnfinishedTaskList(this).size() - w2.getExecutableUnfinishedTaskList(this).size())//How About Skill Point 
			.collect(Collectors.toList());
		
		//(PM) Select time slots to be released. Priority : 1.Worker -> 2.Time(Backward:from due date to current time).
		//Initialize		
		int projectIndex = PDES_OidaSimulator.projectList.indexOf(this);
		double workAmountToBeReleased = 0;
		for (Worker w : workerListToBeReleased) {
			//Initialize
			Integer[] targetTimeSlotArray = new Integer[PDES_OidaSimulator.maxTime];
			System.arraycopy(w.getLatestAssignedProjectPlanArray(), 0,
					targetTimeSlotArray, 0, w.getLatestAssignedProjectPlanArray().length);
			
			//Target Time Slots for Request
			for(int t = this.getDueDate(); time < t ; t--) {
				if(estimatedReleasableWorkAmount <= workAmountToBeReleased) break;
				if(w.getLatestAssignedProjectPlanArray()[t] == projectIndex) {
					targetTimeSlotArray[t] = -1; //Release
					workAmountToBeReleased += 1;
				}
			}
			
			//Add request list
			releaseList.add(new Request(time, Request.indexOf(this), Request.indexOf(w), targetTimeSlotArray));
		}
		return releaseList;
	}
	
	public double estimateLackOfWorkAmount(int time) {
		double estimatedLackOfWorkAmount = 0;
		if(time <= this.getDueDate()) {//time <= due date

			//Work Amount to be supply requested
			estimatedLackOfWorkAmount = this.getEstimatedTotalWorkAmount();					
			for (int t = time+1; t < this.getDueDate()+1; t++){//t+1 ~ dd
				double numOfResourceAtTime = 0;
				for (Worker w : PDES_OidaSimulator.allWorkerList) {
					if(PDES_OidaSimulator.projectList.indexOf(this) == (w.getLatestAssignedProjectPlanArray()[t])) numOfResourceAtTime++;
				}
				estimatedLackOfWorkAmount -= numOfResourceAtTime;
			}
		}else {//time > due date			
			//Work Amount as much as possible to be supply requested 
			estimatedLackOfWorkAmount = this.getEstimatedTotalWorkAmount();											
		}
		return estimatedLackOfWorkAmount;
	}
	
	//PM->BR
	public List<Request> sendSupplyRequest(int time) {
		List<Request> supplyRequestList  = new ArrayList<Request>();
		
		//(PM) Estimate work amount to supply request
		double estimatedLackOfWorkAmount = estimateLackOfWorkAmount(time);
		
		//No lack of Resource -> next Project
		if(estimatedLackOfWorkAmount < 0) return supplyRequestList;
		
		//Add request list
		supplyRequestList.add(new Request(time, Request.indexOf(this), PDES_OidaSimulator.brokerId, this.getUnfinishedTaskList(), estimatedLackOfWorkAmount));
		
		return supplyRequestList;
	}
	
	public double getEstimatedTotalWorkAmount() {
		return this.estimatedTotalWorkAmount;
	}

	public double getEstimatedRequiredResource(int time) {
		return this.estimatedRequiredResource;
	}

	public List<Worker> getWorkerList() {
		return workerList;
	}

	public void setWorkerList(List<Worker> workerList) {
		this.workerList = workerList;
	}
	
	public double getEstimatedCompletionTime() {
		return this.estimatedCompletionTime;
	}
	
	public double getEstimatedDelay() {
		return estimatedDelay;
	}

	/**
	 * Transfer to text data.
	 */
	@Override
	public String toString() {
		String assignedWorkerNames = String.join(",", workerList.stream().map(w -> w.getName()).collect(Collectors.toList()));
		return String.format("[%s] DD=%d ECT=%f || ATWA=%f ETWA=%f || ERR=%f NoAW=%d AW=[%s]", 
				this.getName(), this.getDueDate(), this.estimatedCompletionTime, //Time
				this.actualTotalWorkAmount,this.estimatedTotalWorkAmount, //Work Amount
				this.estimatedRequiredResource, workerList.size(), assignedWorkerNames); //Resource
	}

	public List<Task> getUnfinishedTaskList() {
		return super.getTargetedTaskList().stream()
				.filter(t -> !t.isFinished())
				.map(t -> (Task)t)
				.collect(Collectors.toList());
	}

	/**
	 * @return the start
	 */
	public int getStartTime() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStartTime(int time) {
		if(this.start == -1) this.start = time;
	}

	/**
	 * @return the finish
	 */
	public int getFinishTime() {
		return finish;
	}

	/**
	 * @param finish the finish to set
	 */
	public void setFinishTime(int time) {
		this.finish = time;
	}


}

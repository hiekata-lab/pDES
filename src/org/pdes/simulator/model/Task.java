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

import java.util.Random;
import java.util.stream.Collectors;

import org.pdes.rcp.model.TaskNode;
import org.pdes.simulator.model.base.BaseTask;
import org.pdes.simulator.model.base.BaseTeam;
import org.pdes.simulator.model.base.BaseWorker;

/**
 * @author Yoshiaki Oida <yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class Task extends BaseTask {
	private boolean firstTimeFlag = false;
	private double expectedWorkAmount; // expected work amount
	private Random r = new Random(0); // seed = 0

	/**
	 * @param taskNode
	 */
	public Task(TaskNode taskNode) {
		super(taskNode);
		firstTimeFlag = true;
	}
	
	@Override
	public void initializeWorkAmount() {
		//super.setRemainingWorkAmount(super.getDefaultWorkAmount());
		super.setActualWorkAmount(super.getDefaultWorkAmount());
		super.setRemainingWorkAmount(estimateWorkAmount());
		//setExpectedWorkAmount(estimateWorkAmount());
	}
	
	private double estimateWorkAmount() {
		//Assume one component for each task
		double sigma = ((Component)super.getTargetComponentList().get(0)).getSigma();
		//μ = Actual Work Amount，sigma = Estimation Skill for Project 
		return getActualWorkAmount() + sigma*r.nextGaussian();
	}
		
	/**
	 * @param time
	 */
	public void perform(int time) {
		if (super.isWorking()) {
			double producedWorkAmount = 0;
			for(BaseWorker allocatedWorker : super.getAllocatedWorkerList()) {
				producedWorkAmount += allocatedWorker.getWorkAmountSkillPoint(this);
			}
			if(firstTimeFlag) {
				/**
				 * For the first time to do this work,
				 * workers know the actual work amount of this task.
				 */
				//RemainingWorkAmountをProjectMangaerが認識している値として利用すべきか，
				//Expectedを使うべきか．
				super.setRemainingWorkAmount(super.getActualWorkAmount());
				firstTimeFlag = false;
			}
			//Reduce work amount by produced work amount of workers.
			super.setRemainingWorkAmount(super.getRemainingWorkAmount() - producedWorkAmount);
		}
	}

	//Use or Not
	public double getExpectedWorkAmount() {
		return expectedWorkAmount;
	}
	
	//Use or Not
	public void setExpectedWorkAmount(double expectedWorkAmount) {
		this.expectedWorkAmount = expectedWorkAmount;
	}

	/**
	 * Transfer to text data.
	 */
	@Override
	public String toString() {
		String worker = (super.getAllocatedWorkerList().size() > 0) ? String.join(",", super.getAllocatedWorkerList().stream().map(w -> w.getName()).collect(Collectors.toList())) : "";
		String inputTaskNames = String.join(",", this.getInputTaskList().stream().map(t -> t.getName()).collect(Collectors.toList())); // DEBUG
		return String.format("[%s] %s EWA=%f AWA=%f AW=[%s] IN=[%s]", 
				getName(), 
				getState(),
				getRemainingWorkAmount(), 
				getActualWorkAmount(),
				worker, 
				inputTaskNames);
	}

}

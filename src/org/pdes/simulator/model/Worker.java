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

import org.pdes.rcp.model.WorkerElement;
import org.pdes.simulator.model.base.BaseComponent;
import org.pdes.simulator.model.base.BaseTask;
import org.pdes.simulator.model.base.BaseTeam;
import org.pdes.simulator.model.base.BaseWorker;

/**
 * @author Yoshiaki Oida <yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class Worker extends BaseWorker {

	// Changeable variable on simulation
	private BaseComponent currentAssignedProject = null; // current assigned project
	protected final List<BaseComponent> assignedProjectHistoryList = new ArrayList<BaseComponent>(); // list of assigned project history list.
	protected final List<BaseTask> assignedTaskHistoryList = new ArrayList<BaseTask>(); // list of assigned task history list.
			
	/**
	 * @param workerElement
	 * @param team
	 */
	public Worker(WorkerElement workerElement, BaseTeam team) {
		super(workerElement, team);
		// TODO Auto-generated constructor stub
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
		assignedProjectHistoryList.clear();
		assignedTaskHistoryList.clear();
		setCurrentAssignedProject(null);
	}

	public BaseComponent getCurrentAssignedProject() {
		return currentAssignedProject;
	}

	public void setCurrentAssignedProject(BaseComponent currentAssignedProject) {
		this.currentAssignedProject = currentAssignedProject;
	}
}

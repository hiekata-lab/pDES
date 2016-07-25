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
package org.pdes.rcp.actions;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.pdes.rcp.actions.base.AbstractOneRunSimulationAction;
import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.simulator.PDES_BasicSimulator;
import org.pdes.simulator.model.ProjectInfo;

/**
 * This is the Action class for running PDES_BasicSimulator considering rework of error tolerance at once.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class OneRunPDES_SimulatorConsideringReworkOfErrorToleranceAction extends AbstractOneRunSimulationAction {
	
	private final String text = "DES considering rework of error tolerance";
	
	public OneRunPDES_SimulatorConsideringReworkOfErrorToleranceAction(){
		this.setToolTipText(text);
		this.setText(text);
	}
	
	/* (non-Javadoc)
	 * @see org.pdes.rcp.actions.base.AbstractOneRunSimulationAction#doSimulation()
	 */
	@Override
	protected Future<String> doSimulation(ProjectDiagram diagram, int workflowCount) {
		long start = System.currentTimeMillis();
		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Future<String> result = service.submit(new BasicSimulationTask(0, diagram, workflowCount));
		service.shutdown();
		long end = System.currentTimeMillis();
		msgStream.println("Processing time: " + ((end - start) / 1000) + " [sec]");
		return result;
	}
	
	/**
	 * This is the concurrent callable class for doing simulation by another thread.<br>
	 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
	 */
	private class BasicSimulationTask implements Callable<String>{
		
		private final int no;
		private final ProjectDiagram diagram;
		private final int numOfWorkflow;
		
		/**
		 * This is the constructor.
		 * @param no
		 * @param diagram
		 * @param numOfWorkflow
		 */
		public BasicSimulationTask(int no, ProjectDiagram diagram, int numOfWorkflow) {
			this.no = no;
			this.diagram = diagram;
			this.numOfWorkflow = numOfWorkflow;
		}

		/* (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public String call() throws Exception {
			ProjectInfo project = new ProjectInfo(diagram, numOfWorkflow);
			PDES_BasicSimulator sim = new PDES_BasicSimulator(project);
			sim.setConsiderReworkOfErrorTorelance(true);
			sim.execute();			
			return String.format("%d,%d,%f,%f", no, sim.getTime(), project.getTotalCost(), project.getTotalActualWorkAmount());
		}
	}

}

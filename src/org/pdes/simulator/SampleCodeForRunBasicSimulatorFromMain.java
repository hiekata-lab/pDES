/*
 * Copyright (c) 2018, Design Engineering Laboratory, The University of Tokyo.
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

import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.simulator.model.base.BaseProjectInfo;

/**
 * This is the sample code for running simulator from main class, not GUI.
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class SampleCodeForRunBasicSimulatorFromMain {
	
	private static String pdmFilePath = "XXXXXXXXXXXXXXXXXXXXXXXXXX";
	private static String outputFolderPath = "XXXXXXXXXXXXXXXXXXXXX";
	
	//Example
	//private static String pdmFilePath = "models/simple_case1.pdm";
	//private static String outputFolderPath = "C:\\Users\\nakl\\Desktop\\test";
	
	private static String outputFileName = "1.csv";
	
	public static void main(String[] args) {
		ProjectDiagram diagram = new ProjectDiagram();
		if(diagram.readProjectFile(pdmFilePath)) {
			BaseProjectInfo project = new BaseProjectInfo(diagram, 1);
			PDES_BasicSimulator_TaskPerformedBySingleTaskWorker sim = new PDES_BasicSimulator_TaskPerformedBySingleTaskWorker(project);
			sim.execute();
			sim.saveResultFileByCsv(outputFolderPath, outputFileName);
		}else {
			System.out.println("File has not exists..");
		}

	}

}

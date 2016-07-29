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
package org.pdes.simulator.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.io.FilenameUtils;
import org.pdes.simulator.model.Facility;
import org.pdes.simulator.model.Organization;
import org.pdes.simulator.model.Product;
import org.pdes.simulator.model.ProjectInfo;
import org.pdes.simulator.model.Task;
import org.pdes.simulator.model.Worker;
import org.pdes.simulator.model.Workflow;

/**
 * This is the abstract simulator for pDES application.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class PDES_AbstractSimulator {
	protected final List<Workflow> workflowList;
	protected final Organization organization;
	protected final List<Product> productList;
	protected final int concurrencyWorkflowLimit;
	
	protected int time = 0;
	
	/**
	 * This is the constructor.
	 * @param workflowList
	 * @param organization
	 * @param productList
	 * @param simultaneousWorkflowLimit
	 */
	public PDES_AbstractSimulator(ProjectInfo project){
		this.workflowList = project.getWorkflowList();
		this.organization = project.getOrganization();
		this.productList = project.getProductList();
		this.concurrencyWorkflowLimit = project.getConcurrencyWorkflowLimit();
	}
	
	/**
	 * Execute simulator.
	 */
	abstract public void execute();

	/**
	 * Get the time.
	 * @return the time
	 */
	public int getTime() {
		return time;
	}
	
	/**
	 * Initialize before starting simulation
	 */
	public void initialize(){
		this.time = 0;
		workflowList.forEach(w -> w.initialize());
		organization.initialize();
		productList.forEach(p -> p.initialize());
	}
	
	/**
	 * Check whether all tasks are finished or not.
	 * @return
	 */
	public boolean checkAllTasksAreFinished(){
		return workflowList.stream().allMatch(w -> w.isFinished());
	}
	
	/**
	 * Get the list of READY tasks.
	 * @return
	 */
	public List<Task> getReadyTaskList(){
		return workflowList.stream()
				.map(w -> w.getReadyTaskList())
				.collect(
						() -> new ArrayList<>(),
						(l, t) -> l.addAll(t),
						(l1, l2) -> l1.addAll(l2)
						);
	}
	
	/**
	 * Sort Tasks as followings:<br>
	 * 1. Due date<br>
	 * 2. TSLACK (a task which Slack time(LS-ES) is lower has high priority)
	 * @param resourceList
	 */
	public void sortTasks(List<Task> taskList){
		taskList.sort((t1, t2) -> {
			int dd1 = t1.getDueDate();
			int dd2 = t2.getDueDate();
			if (dd1 < dd2) return -1;
			if (dd1 > dd2) return 1;
			double slack1 = t1.getLst() - t1.getEst();
			double slack2 = t2.getLst() - t2.getEst();
			return Double.compare(slack1, slack2);
		});
	}
	
	/**
	 * Sort Worker as followings:<br>
	 * 1. SSP (a resource which amount of skill point is lower has high priority)
	 * @param resourceList
	 */
	public void sortWorkers(List<Worker> resourceList){
		resourceList.sort((w1, w2) -> {
			double sp1 = w1.getTotalWorkAmountSkillPoint();
			double sp2 = w2.getTotalWorkAmountSkillPoint();
			return Double.compare(sp1, sp2);
		});
	}
	
	/**
	 * Sort Facilities as followings:<br>
	 * 1. SSP (a resource which amount of skill point is lower has high priority)
	 * @param resourceList
	 */
	public void sortFacilities(List<Facility> resourceList){
		resourceList.sort((w1, w2) -> {
			double sp1 = w1.getTotalWorkAmountSkillPoint();
			double sp2 = w2.getTotalWorkAmountSkillPoint();
			return Double.compare(sp1, sp2);
		});
	}
	
	/**
	 * Allocate ready tasks to free workers and facilities if necessary.
	 * @param time 
	 * @param readyTaskList
	 * @param freeWorkerList
	 * @param freeFacilityList
	 */
	public void allocateReadyTasksToFreeResources(List<Task> readyTaskList, List<Worker> freeWorkerList, List<Facility> freeFacilityList){
		this.sortTasks(readyTaskList);
		readyTaskList.stream().forEachOrdered(task -> {
			if(this.checkSatisfyingWorkflowLimitForStartingTask(task)){
				Optional<Worker> availableWorker = freeWorkerList.stream().filter(w -> w.hasSkill(task)).findFirst();
				availableWorker.ifPresent(worker ->{
					if (task.isNeedFacility()) {
						Optional<Facility> availableFacility = freeFacilityList.stream().filter(w -> w.hasSkill(task)).findFirst();
						availableFacility.ifPresent(facility -> {
							task.setAllocatedWorker(worker);
							task.setAllocatedFacility(facility);
							freeWorkerList.remove(worker);
							freeFacilityList.remove(facility);
						});
					}else{
						task.setAllocatedWorker(worker);;
						freeWorkerList.remove(worker);
					}
				});
			}
		});
	}
	
	/**
	 * Perform and update all workflow in this time.
	 * @param time 
	 * @param componentErrorRework 
	 */
	public void performAndUpdateAllWorkflow(int time, boolean componentErrorRework){
		workflowList.forEach(w -> {
			w.checkWorking(time);//READY -> WORKING
			w.perform(time, componentErrorRework);//update information of WORKING task in each workflow
			w.checkFinished(time);// WORKING -> WORKING_ADDITIONALLY or FINISHED
			w.checkReady(time);// NONE -> READY
			w.updatePERTData();//Update PERT information
		});
	}
	
	/**
	 * Check if this task can start by considering workflow limit.
	 * @param task
	 * @return
	 */
	public boolean checkSatisfyingWorkflowLimitForStartingTask(Task task){
		long numOfRunningWorkflow = workflowList.stream()
				.filter(w -> w.isRunning())
				.map(w -> w.getId())
				.count();
		Optional<String> runningWorkflow = workflowList.stream()
				.filter(w -> w.isRunning())
				.filter(w -> w.hasTask(task.getId()))
				.map(w -> w.getId()).findFirst();
		return runningWorkflow.isPresent() || numOfRunningWorkflow < concurrencyWorkflowLimit;
	}
	
	/**
	 * Save the simulation result to the given directory.
	 * @param outputDir
	 */
	public void saveResultFilesInDirectory(String outputDir, String no){
		String fileName = this.getResultFileName(no);//the list of file name
		this.saveResultFileByCsv(outputDir, fileName+".csv");//1. Gantt chart data by csv format.
	}
	
	/**
	 * Get the file name considering 
	 * @param type
	 * @param extension
	 */
	private String getResultFileName(String no){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = String.join("_", sdf.format(date));
		if(no.length()>0) fileName += "_"+no;
		return fileName;
	}
	
	/**
	 * Save result file by csv format.
	 * @param outputDirName
	 * @param resultFileName
	 */
	private void saveResultFileByCsv(String outputDirName, String resultFileName){
		File resultFile = new File(outputDirName, resultFileName);
		String separator = ",";
		try {
			// BOM
			FileOutputStream os = new FileOutputStream(resultFile);
			os.write(0xef);
			os.write(0xbb);
			os.write(0xbf);
			
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
			
			// header
			pw.println(FilenameUtils.getBaseName(resultFile.toString()));
			
			// workflow
			pw.println();
			pw.println("Gantt chart of each Task");
			pw.println(String.join(separator , new String[]{"Workflow", "Task", "Assigned Team", "Ready Time", "Start Time", "Finish Time", "Additinoal Start Time", "Additional Finish Time"}));
			this.workflowList.forEach(w -> {
				String workflowName = "Workflow ("+w.getDueDate()+")";
				w.getTaskList().forEach(t ->{
					pw.println(String.join(separator ,
							new String[]{workflowName, t.getName(), t.getAllocatedTeam().getName(), String.valueOf(t.getReadyTime()), String.valueOf(t.getStartTime()), String.valueOf(t.getFinishTime()), String.valueOf(t.getAdditionalStartTime()), String.valueOf(t.getAdditionalFinishTime())}));
				});
			});
			
			// product
			pw.println();
			pw.println("Gantt chart of each Component");
			pw.println(String.join(separator , new String[]{"Product", "Component", "Error", "Start Time", "Finish Time", "Additinoal Start Time", "Additional Finish Time"}));
			this.productList.forEach(p -> {
				String productName = "Product ("+p.getDueDate()+")";
				p.getComponentList().forEach(c -> {
					pw.println(String.join(separator ,
							new String[]{productName, c.getName(), String.valueOf(c.getErrorTolerance()), String.valueOf(c.getStartTime()), String.valueOf(c.getFinishTime()), String.valueOf(c.getAdditionalStartTime()), String.valueOf(c.getAdditionalFinishTime())}));
				});
			});
			// Organization
			pw.println();
			pw.println("Gantt chart of each Resource");
			pw.println(String.join(separator , new String[]{"Team", "Type", "Name", "Start Time", "Finish Time", "Assigned Task", "Start Time", "Finish Time", "Assigned Task", "Start Time", "Finish Time", "Assigned Task", "Start Time", "Finish Time"}));
			this.organization.getTeamList().forEach(t -> {
				String teamName = t.getName();
				
				//Workers
				t.getWorkerList().forEach(w -> {;
					List<String> baseInfo = new ArrayList<String>();
					baseInfo.add(teamName);
					baseInfo.add("Worker");
					baseInfo.add(w.getName());
					IntStream.range(0, w.getFinishTimeList().size()).forEach(i -> {
						baseInfo.add(String.valueOf(w.getStartTimeList().get(i)));
						baseInfo.add(String.valueOf(w.getFinishTimeList().get(i)));
						baseInfo.add(w.getWorkedTaskList().get(i).getName());
					});
					pw.println(String.join(separator, baseInfo.stream().toArray(String[]::new)));;
				});
				
				//Facilities
				t.getFacilityList().forEach(w -> {
					List<String> baseInfo = new ArrayList<String>();
					baseInfo.add(teamName);
					baseInfo.add("Facility");
					baseInfo.add(w.getName());
					IntStream.range(0, w.getFinishTimeList().size()).forEach(i -> {
						baseInfo.add(String.valueOf(w.getStartTimeList().get(i)));
						baseInfo.add(String.valueOf(w.getFinishTimeList().get(i)));
						baseInfo.add(w.getWorkedTaskList().get(i).getName());
					});
					pw.println(String.join(separator, baseInfo.stream().toArray(String[]::new)));;
				});
			});
			
			pw.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

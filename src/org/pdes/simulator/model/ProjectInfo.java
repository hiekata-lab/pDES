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
import java.util.stream.IntStream;

import org.pdes.rcp.model.ProjectDiagram;

/**
 * This is the class for collecting Project information.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class ProjectInfo {
	
	private ProjectDiagram diagram;
	
	public Organization organization;
	public List<Workflow> workflowList;
	public List<Product> productList;
	public int concurrencyWorkflowLimit;
	
	/**
	 * This is the constructor.
	 * If you put the diagram information, you can create the model for simulation automatically.
	 * @param diagram
	 */
	public ProjectInfo(ProjectDiagram diagram, int workflowCount){
		this.diagram = diagram;
		this.organization = this.getOrganizationFromProjectDiagram();
		
		this.workflowList = new ArrayList<Workflow>();
		this.productList = new ArrayList<Product>();
		IntStream.range(0,workflowCount).forEach(i ->{
			List<Task> taskList = this.getTaskListConsideringOnlyTaskDependency();
			List<Component> componentList = this.getComponentListConsideringOnlyComponentDependency();
			this.addTargetComponentLinkInformation(componentList, taskList);
			this.addAllocationLinkInformation(organization, taskList);
			Workflow workflow = new Workflow(i,taskList);
			Product product = new Product(componentList);
			this.workflowList.add(workflow);
			this.productList.add(product);
		});
		this.concurrencyWorkflowLimit = this.diagram.getConcurrencyLimitOfWorkflow();
	}
	
	/**
	 * Get Organization from ProjectDiagram.
	 * @return
	 */
	private Organization getOrganizationFromProjectDiagram(){
		List<Team> teamList = diagram.getTeamNodeList().stream()
				.map(node -> new Team(node))
				.collect(Collectors.toList());
		return new Organization(teamList);
	}
	
	
	/**
	 * Get the list of Task considering only task dependency.
	 * @return
	 */
	private List<Task> getTaskListConsideringOnlyTaskDependency(){
		List<Task> taskList = this.diagram.getTaskNodeList().stream()
				.map(node -> new Task(node))
				.collect(Collectors.toList());
		this.diagram.getTaskLinkList().forEach(link -> {
			Task destinationTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			Task originTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst()
					.get();
			destinationTask.addInputTask(originTask);
			originTask.addOutputTask(destinationTask);
		});
		return taskList;
	}
	
	/**
	 * Get the list of Component considering only component dependency.
	 * @return
	 */
	private List<Component> getComponentListConsideringOnlyComponentDependency(){
		List<Component> componentList = this.diagram.getComponentNodeList().stream()
				.map(node -> new Component(node))
				.collect(Collectors.toList());
		this.diagram.getComponentLinkList().forEach(link -> {
			Component destinationComponent = componentList.stream()
					.filter(component -> component.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			Component originComponent = componentList.stream()
					.filter(component -> component.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst()
					.get();
			destinationComponent.addDependedComponent(originComponent);
			originComponent.addDependingComponent(destinationComponent);
		});
		return componentList;
	}
	
	/**
	 * Add TargetComponentLink information to Component and Task.
	 * @param taskList
	 * @param componentList
	 */
	private void addTargetComponentLinkInformation(List<Component> componentList, List<Task> taskList){
		this.diagram.getTargetComponentLinkList().forEach(link -> {
			Task destinationTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			Component originComponent = componentList.stream()
					.filter(component -> component.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst()
					.get();
			destinationTask.addTargetComponent(originComponent);
		});
	}
	
	/**
	 * Add TargetComponentLink information to Team and Task.
	 * @param organization
	 * @param taskList
	 */
	private void addAllocationLinkInformation(Organization organization, List<Task> taskList){
		this.diagram.getAllocationLinkList().forEach(link -> {
			Task destinationTask = taskList.stream()
					.filter(task -> task.getNodeId().equals(link.getDestinationNode().getId()))
					.findFirst()
					.get();
			Team originTeam = organization.getTeamList().stream()
					.filter(team -> team.getNodeId().equals(link.getOriginNode().getId()))
					.findFirst()
					.get();
			destinationTask.setAllocatedTeam(originTeam);
		});
	}

	/**
	 * Get the Organization.
	 * @return the organization
	 */
	public Organization getOrganization() {
		return organization;
	}

	/**
	 * Get the list of Workflow.
	 * @return the workflowList
	 */
	public List<Workflow> getWorkflowList() {
		return workflowList;
	}

	/**
	 * Get the list of Product
	 * @return the productList
	 */
	public List<Product> getProductList() {
		return productList;
	}

	/**
	 * Get the limit of concurrency workflow.
	 * @return the concurrencyWorkflowLimit
	 */
	public int getConcurrencyWorkflowLimit() {
		return concurrencyWorkflowLimit;
	}
	
	/**
	 * Get the total cost of this Project after simulation.
	 * @return
	 */
	public double getTotalCost(){
		return organization.getTotalCost();
	}
	
	/**
	 * Get the total actual work amount of this project after simulation.
	 * @return
	 */
	public double getTotalActualWorkAmount(){
		return workflowList.stream()
				.mapToDouble(w -> w.getTotalActualWorkAmount())
				.sum();
	}
}

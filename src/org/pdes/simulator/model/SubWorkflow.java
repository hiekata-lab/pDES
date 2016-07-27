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

import java.util.List;
import java.util.stream.Collectors;

import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.rcp.model.SubWorkflowNode;

/**
 * SubWorkflow model for discrete event simulation.
 * TODO However, this model cannot be used in our simulation now.
 * @author Hiroya Matsubara <matsubara@is.k.u-tokyo.ac.jp>
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
public class SubWorkflow {
	private final String nodeId; // SubWorkflowNodeID
	private final String name;
	private final ProjectDiagram diagram;
	
	private List<Task> taskList;
	private List<Component> componentList;
	
	/**
	 * This is the constructor.
	 * @param node
	 * @param diagram
	 */
	public SubWorkflow(SubWorkflowNode node, ProjectDiagram diagram) {
		this.nodeId = node.getId();
		this.name = node.getName();
		this.diagram = diagram;
		buildSimulationModel();
	}
	
	/**
	 * Set the information of TaskLink, ComponentLink and TargetComponentLink to Task and Component.
	 */
	private void buildSimulationModel() {
		taskList = diagram.getTaskNodeList().stream().map(node -> new Task(node)).collect(Collectors.toList());
		componentList = diagram.getComponentNodeList().stream().map(node -> new Component(node)).collect(Collectors.toList());
		
		//Set the information of TaskLink to Task
		diagram.getTaskLinkList().forEach(link -> {
			Task destTask = findTaskByNodeId(link.getDestinationNode().getId());
			Task origTask = findTaskByNodeId(link.getOriginNode().getId());
			destTask.addInputTask(origTask);
			origTask.addOutputTask(destTask);
		});
		
		//Set the information of ComponentLink to Component
		diagram.getComponentLinkList().forEach(link -> {
			Component destComponent = findComponentByNodeId(link.getDestinationNode().getId());
			Component origComponent = findComponentByNodeId(link.getOriginNode().getId());
			destComponent.addDependedComponent(origComponent);
			origComponent.addDependingComponent(destComponent);
		});
		
		//Set the information of TargetComponentLink to Task
		diagram.getTargetComponentLinkList().forEach(link -> {
			Task task = findTaskByNodeId(link.getDestinationNode().getId());
			Component component = findComponentByNodeId(link.getOriginNode().getId());
			task.addTargetComponent(component);
		});
	}
	
	/**
	 * Get Task which has the same id as nodeId
	 * @param nodeId
	 * @return
	 */
	private Task findTaskByNodeId(String nodeId) {
		return taskList.stream().filter(task -> task.getNodeId().equals(nodeId)).findFirst().get();
	}
	
	/**
	 * Get Component which has the same id as nodeId
	 * @param nodeId
	 * @return
	 */
	private Component findComponentByNodeId(String nodeId) {
		return componentList.stream().filter(component -> component.getNodeId().equals(nodeId)).findFirst().get();
	}
	
	/**
	 * Get head task list. These tasks do not have depended task.
	 * @return
	 */
	public List<Task> getHeadTaskList() {
		return taskList.stream().filter(task -> task.getInputTaskList().size() == 0).collect(Collectors.toList());
	}
	
	/**
	 * Get tail task list. These tasks do not have depending task.
	 * @return
	 */
	public List<Task> getTailTaskList() {
		return taskList.stream().filter(task -> task.getOutputTaskList().size() == 0).collect(Collectors.toList());
	}
	
	/**
	 * Get top component in the list of component.
	 * @return
	 */
	public Component getTopComponent() {
		return componentList.stream().filter(component -> component.getDirectlyDependedComponentList().size() == 0).findFirst().get();
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
	 * Get the diagram.
	 * @return the diagram
	 */
	public ProjectDiagram getDiagram() {
		return diagram;
	}

	/**
	 * Get the list of tasks.
	 * @return the taskList
	 */
	public List<Task> getTaskList() {
		return taskList;
	}

	/**
	 * Get the list of components.
	 * @return the componentList
	 */
	public List<Component> getComponentList() {
		return componentList;
	}

}

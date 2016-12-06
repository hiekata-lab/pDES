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
package org.pdes.simulator.model.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.pdes.rcp.model.ComponentNode;

/**
 * Component model for discrete event simulation.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class BaseComponent {
	
	// Constraint variables on simulation
	private final String id; // ID
	private final String nodeId; // ComponentNode ID
	private final String name;
	private final double errorTolerance;
	private final List<BaseComponent> dependingComponentList = new ArrayList<>();
	private final List<BaseComponent> dependedComponentList = new ArrayList<>();
	private final List<BaseTask> targetedTaskList = new ArrayList<>();
	
	// Changeable variable on simulation
	private double error;
	
	//Other
	private final Random random = new Random();
	
	/**
	 * This is the constructor.
	 * @param componentNode
	 */
	public BaseComponent(ComponentNode componentNode) {
		this.id = UUID.randomUUID().toString();
		this.nodeId = componentNode.getId();
		this.name = componentNode.getName();
		this.errorTolerance = componentNode.getErrorTolerance();
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		error = 0;
	}
	
	/**
	 * Add the attribute of depending component to this.
	 * @param component
	 */
	public void addDependingComponent(BaseComponent component) {
		dependingComponentList.add(component);
	}
	
	/**
	 * Add the attribute of depended component to this,
	 * @param component
	 */
	public void addDependedComponent(BaseComponent component) {
		dependedComponentList.add(component);
	}
	
	/**
	 * Add targeted Task of this component.
	 * @param task
	 */
	public void addTargetedTask(BaseTask task) {
		targetedTaskList.add(task);
	}
	
	/**
	 * Update error value randomly.
	 * @param noErrorProbability
	 */
	public void updateErrorValue(double noErrorProbability) {
		if (random.nextDouble() >= noErrorProbability) error++;
	}

	/**
	 * Add error.
	 * @param generatedError
	 */
	public void addError(double generatedError) {
		error += generatedError;
	}

	/**
	 * Remove error.
	 * @param detectedError
	 */
	public void removeError(double detectedError) {
		error -= detectedError;
	}
	
	/**
	 * Get total error value including depending components.
	 */
	public double getTotalErrorValue() {
		if (dependingComponentList.size() == 0) return error;
		return error + dependingComponentList.stream().mapToDouble(c -> c.getTotalErrorValue()).sum();
	}
	
	/**
	 * Check if the value of error is over tolerance.
	 * @return
	 */
	public boolean checkIfErrorIsOverTolerance() {
		if (getTotalErrorValue() > errorTolerance) {
			return true;
		}
		return false;
	}
	
	/**
	 * Reset error value.
	 */
	public void resetErrorValue() {
		error = 0;
		dependingComponentList.forEach(c -> c.resetErrorValue());
	}

	/**
	 * Get the id.
	 * @return the id
	 */
	public String getId() {
		return id;
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
	 * Get error tolerance.
	 * @return the errorTolerance
	 */
	public double getErrorTolerance() {
		return errorTolerance;
	}

	/**
	 * Get the list of directly depending components.
	 * @return the dependingComponentList
	 */
	public List<BaseComponent> getDirectlyDependingComponentList() {
		return dependingComponentList;
	}

	/**
	 * Get the list of directly depended components.
	 * @return the dependedComponentList
	 */
	public List<BaseComponent> getDirectlyDependedComponentList() {
		return dependedComponentList;
	}
	
	/**
	 * Get the list of targeted task.
	 * @return the targetedTaskList
	 */
	public List<BaseTask> getTargetedTaskList() {
		return targetedTaskList;
	}
	
	/**
	 * Get the list of all depending components.
	 * @return
	 */
	public List<BaseComponent> getAllDependingComponentList(){
		List<BaseComponent> allDependingComponentList = new ArrayList<BaseComponent>();
		this.getAllDependingComponentListForRecursion(this, allDependingComponentList);
		return allDependingComponentList;
	}
	
	/**
	 * Get the list of all depending components.
	 * @param c
	 * @param allDependingComponentList
	 */
	private void getAllDependingComponentListForRecursion(BaseComponent c, List<BaseComponent> allDependingComponentList){
		allDependingComponentList.add(c);
		c.getDirectlyDependingComponentList().forEach(cc ->{
			 this.getAllDependingComponentListForRecursion(cc, allDependingComponentList);
		});
	}
	
	/**
	 * Get the start time from all depending components and tasks.
	 * @return
	 */
	public int getStartTime(){
		return this.getAllDependingComponentList().stream()
				.flatMap(c -> c.getTargetedTaskList().stream())
				.mapToInt(t -> t.getStartTimeList().stream()
						.filter(s -> s >=0)
						.min(Comparator.naturalOrder())
						.orElse(-1))
				.filter(s -> s >= 0)
				.min()
				.orElse(-1);
	}
	
	/**
	 * Get the start time list from all depending components and tasks.
	 * @return
	 */
	public List<Integer> getStartTimeList(){
		List<Integer> startTimeList = new ArrayList<Integer>();
		this.getAllDependingComponentList().forEach(c -> {
			c.getTargetedTaskList().forEach(t -> {
				startTimeList.addAll(t.getStartTimeList());
			});
		});
		Collections.sort(startTimeList);
		return startTimeList;
	}
	
	/**
	 * Get the finish time list from all depending components and tasks.
	 * @return
	 */
	public List<Integer> getFinishTimeList(){
		List<Integer> finishTimeList = new ArrayList<Integer>();
		this.getAllDependingComponentList().forEach(c -> {
			c.getTargetedTaskList().forEach(t -> {
				finishTimeList.addAll(t.getFinishTimeList());
			});
		});
		Collections.sort(finishTimeList);
		return finishTimeList;
	}
	
	/**
	 * Get the finish time from all depending components and tasks.
	 * @return
	 */
	public int getFinishTime(){
		return this.getAllDependingComponentList().stream()
				.flatMap(c -> c.getTargetedTaskList().stream())
				.mapToInt(t -> t.getFinishTimeList().stream()
						.filter(f -> f >=0)
						.max(Comparator.naturalOrder())
						.orElse(-1))
				.filter(f -> f >= 0)
				.max()
				.orElse(-1);
	}
	
	/**
	 * Get error value.
	 * @return the error
	 */
	public double getError() {
		return error;
	}

	/**
	 * Set error value.
	 * @param error the error to set
	 */
	public void setError(double error) {
		this.error = error;
	}

	/**
	 * Set error value.
	 * @param error the error to set
	 */
	public boolean isFinished() {
		return this.getTargetedTaskList().stream()
										.allMatch(t -> t.isFinished());
	}
	
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		String dependingComponentNames = String.join(",", dependingComponentList.stream().map(c -> c.getName()).collect(Collectors.toList()));
		return String.format("[%s] E=%f ETotal=%f dp=%s", name, error, getTotalErrorValue(), dependingComponentNames);
	}
}

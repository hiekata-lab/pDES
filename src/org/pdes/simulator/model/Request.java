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
package org.pdes.simulator.model;

import java.util.List;
import java.util.Objects;

import org.pdes.simulator.PDES_OidaSimulator;

/**
 * @author Yoshiaki Oida <yoida@s.h.k.u-tokyo.ac.jp>
 *
 */
public class Request {

	public static int indexOf(Worker w) {
		return 1 + PDES_OidaSimulator.projectList.size() + PDES_OidaSimulator.allWorkerList.indexOf(w);
	}
	public static int indexOf(Component c) {
		return 1 + PDES_OidaSimulator.projectList.indexOf(c); 
	}
	public static Object getObject(int index) {
		if(index == 0) {
			System.out.println("Portfolio Manager is selected.");
			return null;
		}
		if(0 < index && index < 1+PDES_OidaSimulator.projectList.size()) {
			return PDES_OidaSimulator.projectList.get(index);
		}else{
			return PDES_OidaSimulator.allWorkerList.get(index -(1+PDES_OidaSimulator.projectList.size()));
		}
		
	}
	public static void showCommunicationMatrix() {
		//Communication Matrix
		Integer[][] CommunicationMatrix = 
				new Integer[1+PDES_OidaSimulator.projectList.size()+PDES_OidaSimulator.allWorkerList.size()]
						[1+PDES_OidaSimulator.projectList.size()+PDES_OidaSimulator.allWorkerList.size()];
		System.out.println("<Communication Matrix>");
		System.out.print("PoM,");
		PDES_OidaSimulator.projectList.stream().forEach(c -> System.out.print(c.getName()+","));
		PDES_OidaSimulator.allWorkerList.stream().forEach(w -> System.out.print(w.getName()+","));
		System.out.println();
		for (int i = 0; i < CommunicationMatrix.length; i++) {
			for (int j = 0; j < CommunicationMatrix.length; j++) {
				CommunicationMatrix[i][j] = Request.getCommunicationDistance(i, j);
				System.out.print(CommunicationMatrix[i][j]+",");
			}
			System.out.println();
		}
	}
	
	private static int getCommunicationDistance(int fromIndex, int toindex) {
		if(fromIndex == toindex) return 0;
		
		int minIndex = Math.min(fromIndex, toindex);//keep generalization
		int maxIndex = Math.max(fromIndex, toindex);//keep generalization
		if(minIndex == 0) {//PoM
			if(0 < maxIndex && maxIndex < 1+PDES_OidaSimulator.projectList.size()) {
				return 1;//PM
			}else {
				if(PDES_OidaSimulator.allWorkerList
				.get(maxIndex -(1+PDES_OidaSimulator.projectList.size()))
				.getCurrentAssignedProject()!=null) {
					return 2; //Worker Assigned to project
				}else {
					return 2; //Worker in Resource Pool
				}
			}
		}else if(0 < minIndex && minIndex < 1+PDES_OidaSimulator.projectList.size()) {//PM
			if(0 < maxIndex && maxIndex < 1+PDES_OidaSimulator.projectList.size()) {
				return 2;//Other PM
			}else {
				if(PDES_OidaSimulator.projectList.get(minIndex-1).equals(
						PDES_OidaSimulator.allWorkerList
						.get(maxIndex -(1+PDES_OidaSimulator.projectList.size()))
						.getCurrentAssignedProject())
						) {
					return 1; //Worker Assigned to same project
				}else if(PDES_OidaSimulator.allWorkerList
						.get(maxIndex -(1+PDES_OidaSimulator.projectList.size()))
						.getCurrentAssignedProject()!=null)
				{
					return 3; //Worker Assigned to other project
				}else {
					return 2; //Worker in Resource Pool
				}
			}
		}else {//Worker
			if(PDES_OidaSimulator.allWorkerList
					.get(minIndex -(1+PDES_OidaSimulator.projectList.size()))
					.getCurrentAssignedProject() == null 
					||
					PDES_OidaSimulator.allWorkerList
					.get(maxIndex -(1+PDES_OidaSimulator.projectList.size()))
					.getCurrentAssignedProject() == null) {
				return 2; //Worker in Resource Pool
			}else if(Objects.equals(PDES_OidaSimulator.allWorkerList
					.get(minIndex -(1+PDES_OidaSimulator.projectList.size()))
					.getCurrentAssignedProject()
					, PDES_OidaSimulator.allWorkerList
					.get(maxIndex -(1+PDES_OidaSimulator.projectList.size()))
					.getCurrentAssignedProject())
					){
				return 2; //Worker Assigned to same project
			}else {
				return 4; //Worker Assigned to other project
			}
		}
	}	

	private Integer[] targetTimeSlotArray;
	private List<Task> taskList;
	private double workAmount;
	private int remainingTime;
	private int arrivalTime;
	private final int departureTime;
	private final int fromIndex;
	private final int toIndex;
	
//	public Request(int time, int fromIndex, int toindex) {
//		this.departureTime = time;
//		this.fromIndex = fromIndex;
//		this.toIndex = toindex;
//	}
	
	//Release(PM->WR), Final Confirm & Reply (BR->WR)
	public Request(int time, int fromIndex, int toindex, Integer[] targetTimeSlotArray) {
		this.departureTime = time;
		this.arrivalTime = -1;
		this.targetTimeSlotArray = targetTimeSlotArray;
		this.fromIndex = fromIndex;
		this.toIndex = toindex;
		this.remainingTime = calcDuration(fromIndex, toindex);
	}
	
	//Supply Request PM->BR
	public Request(int time, int fromIndex, int toindex, List<Task> taskList, double workAmount) {
		this.departureTime = time;
		this.arrivalTime = -1;
		this.taskList = taskList;
		this.workAmount = workAmount;
		this.fromIndex = fromIndex;
		this.toIndex = toindex;
		this.remainingTime = calcDuration(fromIndex, toindex);
	}
	
	//Confirm & Reply (BR->WR)
	public Request(int time, int fromIndex, int toindex, Integer[] targetTimeSlotArray, List<Task> taskList) {
		this.departureTime = time;
		this.arrivalTime = -1;
		this.taskList = taskList;
		this.targetTimeSlotArray = targetTimeSlotArray;
		this.fromIndex = fromIndex;
		this.toIndex = toindex;
		this.remainingTime = calcDuration(fromIndex, toindex);
	}

	public void updateRemainlingTime() {
		if(this.remainingTime > 0) {
			this.remainingTime-- ;
		}
	}
	
	public boolean checkArrival(int time) {
		if (this.remainingTime == 0 && arrivalTime == -1){
			arrivalTime = time;
			return true; //arrival
		}else {
			return false; //not yet
		}
	}
	
	private int calcDuration(int fromIndex, int toindex) {
		//return getCommunicationDistance(fromIndex, toindex);//0　
		return 1;
	}

	/**
	 * @return the targetTimeSpanArray
	 */
	public Integer[] getTargetTimeSlotArray() {
		return targetTimeSlotArray;
	}
	/**
	 * @return the fromIndex
	 */
	public int getFromIndex() {
		return fromIndex;
	}
	/**
	 * @return the toIndex
	 */
	public int getToIndex() {
		return toIndex;
	}
	/**
	 * @return the workAmount
	 */
	public double getWorkAmount() {
		return workAmount;
	}
	/**
	 * @return the departureTime
	 */
	public int getDepartureTime() {
		return departureTime;
	}
	/**
	 * @return the arrivalTime
	 */
	public int getArrivalTime() {
		return arrivalTime;
	}
}

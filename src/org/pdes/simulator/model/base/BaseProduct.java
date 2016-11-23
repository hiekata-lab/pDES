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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Product model for discrete event simulation.<br>
 * This model has the list of Components.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class BaseProduct {
	private String id;
	private int dueDate;
	private List<BaseComponent> componentList;
	
	/**
	 * This is the constructor.
	 * @param componentList
	 */
	public BaseProduct(int dueDate, List<BaseComponent> componentList) {
		this.id = UUID.randomUUID().toString();
		this.dueDate = dueDate;;
		this.setComponentList(componentList);
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		componentList.forEach(c -> c.initialize());
	}
	
	/**
	 * Get the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the dueDate
	 */
	public int getDueDate() {
		return dueDate;
	}

	/**
	 * Get the component which has same id.
	 * @param id
	 * @return
	 */
	public BaseComponent getComponent(String id) {
		for (BaseComponent c : componentList) {
			if (c.getId().equals(id)) return c;
		}
		return null;
	}
	
	/**
	 * Get the list of components.
	 * @return the componentList
	 */
	public List<BaseComponent> getComponentList() {
		return componentList;
	}
	
	/**
	 * Set the list of components.
	 * @param componentList the componentList to set
	 */
	public void setComponentList(List<BaseComponent> componentList) {
		this.componentList = componentList;
	}
	
	/**
	 * Transfer to text data.
	 */
	public String toString() {
		return String.join("\n", componentList.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}

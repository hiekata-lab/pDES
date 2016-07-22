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
package org.pdes.rcp.model.base;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * This is AbstractModel class including following restriction:<br>
 *   1. Serializable. This means that the context of this model can be saved as the file format or others.<br>
 *   2. EventListener for MVC architecture. Model cannot access View directory.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class AbstractModel implements Serializable {

	private static final long serialVersionUID = -483102722843574908L;
	private PropertyChangeSupport support;
	
	/**
	 * This is the constructor.
	 */
	public AbstractModel(){
		this.support = new PropertyChangeSupport(this);
	}
	
	/**
	 * Operation after firing property change.
	 * @param name
	 * @param oldvalue
	 * @param newvalue
	 */
	protected void firePropertyChange(String name, Object oldvalue, Object newvalue){
		this.support.firePropertyChange(name, oldvalue, newvalue);
	}
	
	/**
	 * Operation after firing property change.
	 * @param name
	 * @param oldvalue
	 * @param newvalue
	 */
	protected void firePropertyChange(String name, int oldvalue, int newvalue){
		this.support.firePropertyChange(name, oldvalue, newvalue);
	}
	
	/**
	 * Operation after firing property change.
	 * @param name
	 * @param oldvalue
	 * @param newvalue
	 */
	protected void firePropertyChange(String name, boolean oldvalue, boolean newvalue){
		this.support.firePropertyChange(name, oldvalue, newvalue);
	}
	
	/**
	 * Add property change listener.
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener){
		this.support.addPropertyChangeListener(listener);
	}
	
	/**
	 * Remove property change listener.
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener){
		this.support.removePropertyChangeListener(listener);
	}
}

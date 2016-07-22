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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;


/**
 * This is NodeElement class which has the attributes of x,y,width and height.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class NodeElement extends AbstractModel {

	private static final long serialVersionUID = -514739918278977968L;
	
	
	private String id;
	private Diagram parentDiagram;
	private IFigure figure;
	
	//Position and Size
	private int x;
	private int y;
	private int height;
	private int width;
	
	private List<Link> incomingLinkList;//List of incoming Link
	private List<Link> outgoingLinkList;//List of outgoing Link
	//////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This is the constructor.
	 */
	public NodeElement(){
		super();
		this.incomingLinkList = new ArrayList<Link>();
		this.outgoingLinkList = new ArrayList<Link>();
	}
	
	/**
	 * Get the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Set the id.
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Get the Diagram which has this NodeElement.
	 * @return the parentDiagram
	 */
	public Diagram getParentDiagram() {
		return parentDiagram;
	}
	
	/**
	 * Set the Diagram which has this NodeElement.
	 * @param diagram the diagram to set
	 */
	public void setParentDiagram(Diagram parentDiagram) {
		this.parentDiagram = parentDiagram;
	}
	
	/**
	 * Get the IFigure.
	 * @return the figure
	 */
	public IFigure getFigure() {
		return figure;
	}

	/**
	 * Set the IFigure.
	 * @param figure the figure to set
	 */
	public void setFigure(IFigure figure) {
		this.figure = figure;
	}
	
	/**
	 * Set the information whether this NodeElement is visible or not.
	 * @param visible
	 */
	public void setVisibleFigure(boolean visible){
		this.figure.setVisible(visible);
	}
	
	/**
	 * Get the information whether this NodeElement is visible or not.
	 * @return
	 */
	public boolean isVisibleFigure(){
		return this.figure.isVisible();
	}
	
	/**
	 * Get the x position.
	 * @return the x
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Set the x position.
	 * @param x the x to set
	 */
	public void setX(int x) {
		int old = this.x;
		this.x = x;
		firePropertyChange("x", old, x);
	}
	
	/**
	 * Get the y position.
	 * @return the y
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Set the y position.
	 * @param y the y to set
	 */
	public void setY(int y) {
		int old = this.y;
		this.y = y;
		firePropertyChange("y", old, y);
	}
	
	/**
	 * Get the height of this NodeElement.
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Set the height of this NodeElement.
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Get the width of this NodeElement.
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Set the width of this NodeElement.
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * Get the list of incoming link.
	 * @return the incomingLinkList
	 */
	public List<Link> getIncomingLinkList() {
		return incomingLinkList;
	}
	
	/**
	 * Set the list of incoming link.
	 * @param incomingLinkList the incomingLinkList to set
	 */
	public void setIncomingLinkList(List<Link> incomingLinkList) {
		this.incomingLinkList = incomingLinkList;
	}
	
	/**
	 * Add a incoming link to the list of incoming link.
	 * @param link
	 */
	public void addIncomingLink(Link link){
		this.incomingLinkList.add(link);
		firePropertyChange("incoming", null, link);
	}
	
	/**
	 * Remove a incoming link from the list of incoming link.
	 * @param link
	 */
	public void removeIncomingLink(Link link){
		this.incomingLinkList.remove(link);
		firePropertyChange("incoming", link, null);
	}
	
	/**
	 * Get the list of outgoing link.
	 * @return the outgoingLinkList
	 */
	public List<Link> getOutgoingLinkList() {
		return outgoingLinkList;
	}
	
	/**
	 * Set the list of outgoing link.
	 * @param outgoingLinkList the outgoingLinkList to set
	 */
	public void setOutgoingLinkList(List<Link> outgoingLinkList) {
		this.outgoingLinkList = outgoingLinkList;
	}
	
	/**
	 * Add an outgoing link to the list of outgoing link.
	 * @param link
	 */
	public void addOutgoingLink(Link link){
		this.outgoingLinkList.add(link);
		firePropertyChange("outgoing", null, link);
	}
	
	/**
	 * Remove an outgoing link from the list of outgoing link.
	 * @param link
	 */
	public void removeOutgoingLink(Link link){
		this.outgoingLinkList.remove(link);
		firePropertyChange("outgoing", link, null);
	}
	
}

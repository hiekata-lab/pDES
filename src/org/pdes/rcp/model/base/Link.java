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
import org.eclipse.draw2d.geometry.Point;

/**
 * This is Link abstract class.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public abstract class Link extends AbstractModel {

	private static final long serialVersionUID = 7726828248725194529L;
	public static final String P_BEND_POINT = "_bend_point";
	
	////////////////Variables//////////////////////////////////////////////////////////
	public String linkTypeName;
	private NodeElement originNode;
	private NodeElement destinationNode;
	private List<Point> bendPoints = new ArrayList<Point>();
	private IFigure figure;
	//////////////////////////////////////////////////////////////////////////////////
	
	public Link(){
		super();
	}
	
	/**
	 * Get the type of Link.
	 * @return the linkTypeName
	 */
	public String getLinkTypeName() {
		return linkTypeName;
	}
	
	

	/**
	 * Set the type of Link.
	 * @param linkTypeName the linkTypeName to set
	 */
	public void setLinkTypeName(String linkTypeName) {
		this.linkTypeName = linkTypeName;
	}

	/**
	 * Get the origin NodeElement of this Link.
	 * @return the originNode
	 */
	public NodeElement getOriginNode() {
		return originNode;
	}

	/**
	 * Set the origin NodeElement of this Link.
	 * @param originNode the originNode to set
	 */
	public void setOriginNode(NodeElement originNode) {
		NodeElement old = this.originNode;
		this.originNode = originNode;
		firePropertyChange("origin", old, originNode);
	}

	/**
	 * Get the destination NodeElement of this Link.
	 * @return the destinationNode
	 */
	public NodeElement getDestinationNode() {
		return destinationNode;
	}

	/**
	 * Set the origin NodeElement of this Link.
	 * @param destinationNode the destinationNode to set
	 */
	public void setDestinationNode(NodeElement destinationNode) {
		NodeElement old = this.destinationNode;
		this.destinationNode = destinationNode;
		firePropertyChange("destination", old, destinationNode);
	}

	/**
	 * Get the list of bend points of this Link.
	 * @return the bendPoints
	 */
	public List<Point> getBendPoints() {
		return bendPoints;
	}
	
	/**
	 * Set the list of bent points of this Link.
	 * @param bendpoints the bendPoints to set
	 */
	public void setBendPoints(List<Point> bendPoints) {
		this.bendPoints = bendPoints;
	}
	
	/**
	 * Add a bend point.
	 * @param index
	 * @param point
	 */
	public void addBendPoint(int index, Point point) {
		bendPoints.add(index, point);
		firePropertyChange(P_BEND_POINT, null, null);
	}
	
	/**
	 * Remove a bend point.
	 * @param index
	 */
	public void removeBendPoint(int index) {
		bendPoints.remove(index);
		firePropertyChange(P_BEND_POINT, null, null);
	}
	
	/**
	 * Replace a bend point.
	 * @param index
	 * @param point
	 */
	public void replaceBendPoint(int index, Point point) {
		bendPoints.set(index, point);
		firePropertyChange(P_BEND_POINT, null, null);
	}
	
	/**
	 * Get IFigure of this Link.
	 * @return the figure
	 */
	public IFigure getFigure() {
		return figure;
	}

	/**
	 * Set IFigure of this Link.
	 * @param figure the figure to set
	 */
	public void setFigure(IFigure figure) {
		this.figure = figure;
	}
	
	/**
	 * Get the information whether this Link is visible or not.
	 * @return
	 */
	public boolean isVisible(){
		return this.figure.isVisible();
	}
	
	/**
	 * Get the information whether this Link is visible or not.
	 * @param visible
	 */
	public void setVisible(boolean visible){
		this.figure.setVisible(visible);
	}
	
}

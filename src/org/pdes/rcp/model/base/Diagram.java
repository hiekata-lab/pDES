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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;


/**
 * This is Diagram abstract class.<br>
 * Diagram manage all NodeElements. Each Link is managed by each NodeElement.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class Diagram extends AbstractModel {

	private static final long serialVersionUID = 7440176854616915473L;
	
	private List<NodeElement> nodeElementList;
	
	/**
	 * This is the constructor.
	 */
	public Diagram(){
		super();
		this.nodeElementList = new ArrayList<NodeElement>();
	}

	/**
	 * Get the NodeElement list in this ProjectDiagram.
	 * @return the nodeElementList
	 */
	public List<NodeElement> getNodeElementList() {
		return nodeElementList;
	}

	/**
	 * Set the list of NodeElement in this ProjectDiagram<br>
	 * @param nodeElementList the nodeElementList to set
	 */
	public void setNodeElementList(List<NodeElement> nodeElementList) {
		this.nodeElementList = nodeElementList;
	}
	
	/**
	 * Add a NodeElement in the list of NodeElement.
	 * @param element
	 */
	public void addNodeElement(NodeElement element){
		this.nodeElementList.add(element);
		element.setParentDiagram(this);
		firePropertyChange("contents",null,null);
	}
	
	/**
	 * Remove "element" from the list of NodeElement.
	 * @param element
	 */
	public void removeNodeElement(NodeElement element){
		this.nodeElementList.remove(element);
		element.setParentDiagram(null);
		firePropertyChange("contents",null,null);
	}
	
	/**
	 * Get the NodeElement which ID is the same as "id".<br>
	 * @param id
	 * @return
	 */
	public NodeElement getNodeElement(String id){
		for(NodeElement node: nodeElementList){
			if(node.getId().equals(id)) return node;
		}
		return null;
	}
	
	/**
	 * Get the list of Link.
	 * @return
	 */
	public List<Link> getLinkList(){
		List<Link> linkList = new ArrayList<Link>();
		for(NodeElement node : nodeElementList){
			linkList.addAll(node.getIncomingLinkList());
		}
		return linkList;
	}
	
	/**
	 * Transform string data for XML escape.
	 * @param str
	 * @return
	 */
	protected String xmlEscape(String str) {
		if(str == null) return "";
		
		String ret = str;
		// just make sure this is valid.
		// are there any good validator?
		try {
			// special characters.
			ret = ret.replaceAll("&amp;", "&");
			ret = ret.replaceAll("&lt;", "<");
			ret = ret.replaceAll("&gt;", ">");
			ret = ret.replaceAll("&apos;", "'");
			ret = ret.replaceAll("&quot;", "\"");
			ret = ret.replaceAll("&", "&amp;");
			ret = ret.replaceAll("<", "&lt;");
			ret = ret.replaceAll(">", "&gt;");
			ret = ret.replaceAll("'", "&apos;");
			ret = ret.replaceAll("\"", "&quot;");

			// other invalid characters.
			ret = ret.replaceAll("\\u000b", "");
			ret = ret.replaceAll("\\u000c", "");
			ret = ret.replaceAll("\\u000f", "");
			ret = ret.replaceAll("\\u0010", "");
			ret = ret.replaceAll("\\u0011", "");
			ret = ret.replaceAll("\\u0012", "");
			ret = ret.replaceAll("\\u0013", "");
			ret = ret.replaceAll("\\u0014", "");
			ret = ret.replaceAll("\\u0016", "");
			ret = ret.replaceAll("\\u001a", "");
			ret = ret.replaceAll("\\u001d", "");
			ret = ret.replaceAll("\\u201e", "");

			StringReader sr = new StringReader("<xml><tag>" + ret + "</tag></xml>");
			InputSource is = new InputSource(sr);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmldoc = builder.parse(is);
			if( xmldoc != null ) {
				// dummy read for checking this xml document.
				xmldoc = null;
			}
			builder = null;
			is = null;
			sr.close();
			// current string is good for XML
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
}

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
package org.pdes.rcp.controller.command;

import java.util.Random;

import org.eclipse.gef.commands.Command;
import org.pdes.rcp.model.base.Diagram;
import org.pdes.rcp.model.base.NodeElement;

/**
 * This class is the command class for creating NodeElement.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class CreateNodeElementCommand extends Command {
	
	////////////////Variables////////////////////////////
	private Diagram diagram;
	private NodeElement element;
	private int x;
	private int y;
	////////////////////////////////////////////////
	
	/**
	 * This is the constructor.<br>
	 * @param diagram
	 * @param element
	 * @param x
	 * @param y
	 */
	public CreateNodeElementCommand(Diagram diagram, NodeElement element, int x, int y){
		this.diagram = diagram;
		this.element = element;
		this.x = x;
		this.y = y;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		element.setId(getRandomId(12));
		element.setX(x);
		element.setY(y);
		diagram.addNodeElement(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		diagram.removeNodeElement(element);
	}
	
	/**
	 * Get random ID for creating NodeElement.<br>
	 * @return
	 */
	private String getRandomId(int length){
		String id = "";
		for(int i=0; i<length; i++) id += String.valueOf(new Random().nextInt(10));
		return id;
	}
	
}

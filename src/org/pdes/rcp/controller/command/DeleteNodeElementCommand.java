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

import java.util.ArrayList;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.pdes.rcp.model.base.Diagram;
import org.pdes.rcp.model.base.Link;
import org.pdes.rcp.model.base.NodeElement;

/**
 * This class is the command class for deleting NodeElement.</br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class DeleteNodeElementCommand extends Command {
	
	////////////////Variables////////////////////////////
	private Diagram diagram;
	private NodeElement element;
	//////////////////////////////////////////////
	
	/**
	 * This is the constructor.<br>
	 * @param diagram
	 * @param element
	 */
	public DeleteNodeElementCommand(Diagram diagram, NodeElement element){
		this.diagram = diagram;
		this.element = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		
		if(element.getIncomingLinkList().size()>0 | element.getOutgoingLinkList().size()>0){
			if(!MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Confirmation"
					, "Related links will be deleted and cannot be undo. OK?")){
				return;
			}
		}
		
		//Delete related links.
		element.getIncomingLinkList().forEach(link -> link.getOriginNode().removeOutgoingLink(link));
		element.setIncomingLinkList(new ArrayList<Link>());
		element.getOutgoingLinkList().forEach(link -> link.getDestinationNode().removeIncomingLink(link));
		element.setOutgoingLinkList(new ArrayList<Link>());
		
		diagram.removeNodeElement(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		diagram.addNodeElement(element);
	}
}

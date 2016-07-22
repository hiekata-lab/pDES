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

import org.pdes.rcp.model.base.Link;
import org.pdes.rcp.model.base.NodeElement;

/**
 * This class is the command class for reconnecting link.</br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 *
 */
public class ReconnectLinkCommand extends CreateLinkCommand {
	
	private NodeElement oldSource,oldTarget;
	
	/**
	 * This is the constructor.<br>
	 * @param link
	 * @param source
	 * @param target
	 */
	public ReconnectLinkCommand(Link link, NodeElement source,NodeElement target){
		super(link);
		this.link = link;
		this.source = source;
		this.target = target;
	}

	/*
	 * (non-Javadoc)
	 * @see org.pdes.rcp.controller.command.CreateLinkCommand#execute()
	 */
	@Override
	public void execute() {
		if(source!=null && target!=null){
			oldSource = link.getOriginNode();
			oldTarget = link.getDestinationNode();
			if(!(oldSource.equals(source) && oldTarget.equals(target))){
				if(oldSource.equals(source)){
					//move Target information
					link.setDestinationNode(target);
					oldTarget.removeIncomingLink(link);
					target.addIncomingLink(link);
				}else if(oldTarget.equals(target)){
					link.setOriginNode(source);
					//move Source information
					oldSource.removeOutgoingLink(link);
					source.addOutgoingLink(link);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.pdes.rcp.controller.command.CreateLinkCommand#undo()
	 */
	@Override
	public void undo() {
		source.removeOutgoingLink(link);
		oldSource.addOutgoingLink(link);
		link.setOriginNode(oldSource);
		
		target.removeIncomingLink(link);
		oldTarget.addIncomingLink(link);
		link.setDestinationNode(oldTarget);
	}
	
}

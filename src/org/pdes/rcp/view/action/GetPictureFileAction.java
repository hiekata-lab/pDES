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
package org.pdes.rcp.view.action;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.pdes.rcp.model.base.Diagram;
import org.pdes.rcp.model.base.NodeElement;
import org.pdes.rcp.view.editor.base.DiagramEditor;

/**
 * This is Action class for saving picture on DiagramEditor.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class GetPictureFileAction extends Action{
	
	public GetPictureFileAction() {
		//setImageDescriptor(ImageDescriptor.createFromFile(GetPictureFileAction.class, "getpicture.png"));
		setToolTipText("Get picture");
		this.setText("Get picture");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),SWT.SAVE);
		dialog.setText("Save_File");
		dialog.setFilterExtensions(new String[]{"*.bmp","*.jpg","*.png"});
		String saveFilePath = dialog.open();
		if(saveFilePath == null) return;
		
		String exf = saveFilePath.substring(saveFilePath.length()-3, saveFilePath.length());
		if((!saveFilePath.equals("")&&!saveFilePath.equals(null))){
			try{
				DiagramEditor de = (DiagramEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				GraphicalViewer gV = de.getGraphicalViewerForAny();
				ScalableRootEditPart rootEditPart = (ScalableRootEditPart) gV.getRootEditPart();
				double zoom = (rootEditPart.getZoomManager().getZoom());
				IFigure rootFigure = ((LayerManager) rootEditPart).getLayer(LayerConstants.PRINTABLE_LAYERS);
				Rectangle rootFigureBounds = rootFigure.getBounds();
				Rectangle drawSpace = getDrawSpace(de.getDiagram());
				
				int W = (int)(zoom*(drawSpace.width));
				int H = (int)(zoom*(drawSpace.height));
				
				while(true){
					double wW = (double)((double)rootFigureBounds.width/(double)W);
					double hH = (double)((double)rootFigureBounds.height/(double)H);
					if(wW>1&&hH>1){break;}
					rootEditPart.getZoomManager().zoomOut();
					rootFigureBounds = ((LayerManager) rootEditPart).getLayer(LayerConstants.PRINTABLE_LAYERS).getBounds();
					W=(int)(W*rootEditPart.getZoomManager().getZoom());
					H=(int)(H*rootEditPart.getZoomManager().getZoom());	
				}
				
				Image img = new Image(PlatformUI.getWorkbench().getDisplay(), rootFigureBounds.width, rootFigureBounds.height);
				GC imageGC = new GC(img);
				Graphics g = new SWTGraphics(imageGC);
				
				rootFigure.paint(g);
				ImageLoader imgLoader = new ImageLoader();
				imgLoader.data = new ImageData[] { img.getImageData() };
				int format = 0;
				if(exf.equals("bmp")||exf.equals("BMP")){
					format = SWT.IMAGE_BMP;
				}else if(exf.equals("jpg")||exf.equals("JPG")){
					format = SWT.IMAGE_JPEG;
				}else if(exf.equals("gif")||exf.equals("GIF")){
					format = SWT.IMAGE_GIF;
				}else if(exf.equals("png")||exf.equals("PNG")){
					format = SWT.IMAGE_PNG;
				}
				imgLoader.save(saveFilePath,format);
				imageGC.dispose();
				img.dispose();
				rootEditPart.getZoomManager().setZoom(zoom);
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Save", "Picture was saved to [" +saveFilePath+"].");
			}catch(Exception e ){
				e.printStackTrace();
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", "Picture cannot be saved safely.");
			}
			
		}
	}
	
	/**
	 * Get drawing space automatically.<br>
	 * @param diagram
	 * @return
	 */
	private Rectangle getDrawSpace(Diagram diagram) {
		if(diagram.getNodeElementList().size()==0) return new Rectangle(0,0,0,0);
		int minX = diagram.getNodeElementList().get(0).getX();
		int minY = diagram.getNodeElementList().get(0).getY();
		int maxX = diagram.getNodeElementList().get(0).getX();
		int maxY = diagram.getNodeElementList().get(0).getY();
		int maxWidth = diagram.getNodeElementList().get(0).getWidth();
		int maxHeight = diagram.getNodeElementList().get(0).getHeight();
		for(NodeElement node : diagram.getNodeElementList()){
			int X = node.getX();
			int Y = node.getY();
			int Width = node.getWidth();
			int Height = node.getHeight();
			if(X-maxWidth < minX-maxWidth) minX=X;
			if(Y-maxHeight < minY - maxHeight) minY=Y;
			if(X+Width > maxX+maxWidth) maxX=X;maxWidth=Width;
			if(Y+Height > maxY+maxHeight) maxY=Y;maxHeight=Height;
		}
		return new Rectangle(Math.max(0, minX-maxWidth),Math.max(0, minY-maxHeight),maxX+maxWidth,maxY+maxHeight);
	}
	
	
}

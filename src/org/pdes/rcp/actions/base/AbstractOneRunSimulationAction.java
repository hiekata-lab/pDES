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
package org.pdes.rcp.actions.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.pdes.rcp.core.Activator;
import org.pdes.rcp.dialog.InputSimpleTextDialog;
import org.pdes.rcp.model.ProjectDiagram;
import org.pdes.rcp.view.editor.ProjectEditor;

/**
 * This is the abstract One Run Simulation Action.<br>
 * Simulation should do on other UI thread, so concurrent callable class should be developed in upper class.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public abstract class AbstractOneRunSimulationAction extends Action {
	
	protected final MessageConsoleStream msgStream = Activator.getDefault().getMsgStream();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		
		//1. Check whether Project is opened or not.
		IWorkbench ib = PlatformUI.getWorkbench();
		ProjectEditor pe = (ProjectEditor) ib.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(pe == null){
			MessageDialog.openError(ib.getActiveWorkbenchWindow().getShell(), "Error", "Project is not opened.");
			return;
		}
		
		//2. Set directory for save result.
		DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		directoryDialog.setText("Select a directory to save results.");
		String outputDirName = directoryDialog.open();
		if (outputDirName == null) {
			return;
		}
		
		//3. Set the number of workflow and product
		int workflowCount = 0;
		InputSimpleTextDialog workflowCountTextDialog = new InputSimpleTextDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		workflowCountTextDialog.setTitleAndMessage("The number of workflows running", "Enter the number of workflows running.");
		if (workflowCountTextDialog.open() == Window.OK) {
			String text = workflowCountTextDialog.getTextString();
			try {
				workflowCount = Integer.valueOf(text);
			} catch (NumberFormatException e) {
				msgStream.println(String.format("\"%s\" is not integer value. Exit.", text));
				return;
			}
		}
		if (workflowCount <= 0) {
			msgStream.println("Enter a positive integer value. Exit.");
			return;
		}
		
		//4. Run simulation
		Future<String> result = this.doSimulation((ProjectDiagram)pe.getDiagram(), workflowCount);
		
		//5. Save the result of simulation
		this.saveResult(outputDirName, result);
	}
	
	
	/**
	 * Run simulation.
	 * @param workflowCount 
	 */
	protected abstract Future<String> doSimulation(ProjectDiagram diagram, int workflowCount);
	
	/**
	 * Save result of simulation.
	 * @param outputDirName
	 * @param result
	 */
	public void saveResult(String outputDirName, Future<String> result){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String resultFileName = String.join("_", sdf.format(date)) + ".csv";
		File resultFile = new File(outputDirName, resultFileName);
		try {
			// BOMをつける
			FileOutputStream os = new FileOutputStream(resultFile);
			os.write(0xef);
			os.write(0xbb);
			os.write(0xbf);
			
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
			
			// header
			pw.println(FilenameUtils.getBaseName(resultFile.toString()));
			pw.println(String.join(",", "No", "Time", "Cost", "Work amount"));
			
			try {
				pw.println(result.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
				msgStream.println("Error was occurred.");
				pw.close();
				os.close();
				return;
			} catch (ExecutionException e) {
				e.printStackTrace();
				msgStream.println("Error was occurred.");
				pw.close();
				os.close();
				return;
			}
			pw.close();
			os.close();
			
			msgStream.println("A result was saved to " + resultFile.toString());
		} catch (IOException e) {
			msgStream.println(e.getMessage());
		}
	}
}

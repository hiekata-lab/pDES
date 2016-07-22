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
package org.pdes.rcp.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Dialog for selecting a couple of items by check box.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class SelectDialog extends Dialog {

	private String title="";
	private List<String> dataList;
	private List<Boolean> isCheckedList;
	
	private List<String> checkedDataList;
	
	private Table table;
	
	/**
	 * This is the constructor.
	 * @param parentShell
	 */
	public SelectDialog(Shell parentShell) {
		super(parentShell);
	}
	
	
	/**
	 * Set the title and candidate items of this dialog.
	 * @param title
	 * @param message
	 * @param data
	 */
	public void setTitleAndData(String title,List<String> dataList){
		this.title = title;
		this.dataList = dataList;
	}
	
	/**
	 * Set the title and candidate items including initial conditions (checked or not) of this dialog.
	 * @param title
	 * @param dataList
	 * @param isCheckedList
	 */
	public void setTitleAndData(String title, List<String> dataList, List<Boolean> isCheckedList){
		this.title = title;
		this.dataList = dataList;
		this.isCheckedList = isCheckedList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		Composite composite = (Composite)super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2,false));
		table = new Table(parent, SWT.CHECK | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableColumn(table, SWT.RIGHT, "Skill name", 200);
		addTableContents(dataList, isCheckedList);
		table.setSize(table.computeSize(SWT.DEFAULT, 200));
		GridData tableGD = new GridData();
		tableGD.horizontalAlignment = GridData.FILL;
		tableGD.grabExcessHorizontalSpace=true;
		table.setLayoutData(tableGD);
		return composite;
	}
	
	/**
	 * Add a contents in a table.
	 * @param dataList
	 */
	private void addTableContents(List<String> dataList, List<Boolean> isCheckedList) {
		for(int i=0;i<dataList.size();i++){
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(dataList.get(i));
			if(isCheckedList != null) ti.setChecked(isCheckedList.get(i));
		}
	}

	/**
	 * Create a column in table.
	 * @param table
	 * @param style
	 * @param title
	 * @param width
	 * @return
	 */
	private TableColumn createTableColumn(Table table, int style, String title, int width){
		TableColumn tc = new TableColumn(table, style);
		tc.setText(title);
		tc.setResizable(false);
		tc.setWidth(width);
		return tc;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId){
		if(buttonId == IDialogConstants.OK_ID){
			setCheckedDataList();
			setReturnCode(buttonId);
			close();
			super.buttonPressed(buttonId);
		}else if(buttonId == IDialogConstants.CLOSE_ID){
			setReturnCode(buttonId);
		}
	}
	
	/**
	 * Set the information of checked item to global variable "checkedDataList".
	 */
	private void setCheckedDataList(){
		this.checkedDataList = new ArrayList<String>();
		for(TableItem item : this.table.getItems()){
			if(item.getChecked()) checkedDataList.add(item.getText());
		}
	}
	
	/**
	 * Get the string list of checked items.
	 * @return
	 */
	public List<String> getCheckedDataList(){
		return checkedDataList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent,IDialogConstants.OK_ID,"OK", false);
	}


}

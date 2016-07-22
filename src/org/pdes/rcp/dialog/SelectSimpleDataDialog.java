/*
 * Copyright (c) 2016 , Industrial Information Systems, 
 * Department of Human and Engineered Environmental Studies,
 * Graduate School of Frontier Sciences, The University of Tokyo
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Dialog for editing data by selecting.<br>
 * @author Taiga Mitsuyuki <mitsuyuki@sys.t.u-tokyo.ac.jp>
 */
public class SelectSimpleDataDialog extends Dialog {
	private String title="";
	private String message="";

	List<TreeItem> itemTreeList = new ArrayList<TreeItem>();
	List<String> selectedItemList= new ArrayList<String>();
	List<Integer> selectedItemNumber = new ArrayList<Integer>();
	
	private List<String> itemList;
	
	/**
	 * This is the constructor.
	 * @param parent
	 */
	public SelectSimpleDataDialog(Shell parent) {
		super(parent);
	}
	
	/**
	 * Set title and message of this dialog.
	 * @param title
	 * @param message
	 */
	public void setTitleAndMessage(String title,String message){
		this.title = title;
		this.message = message;
	}
	
	/**
	 * Set item list for selecting in this dialog.
	 * @param memberNameList
	 */
	public void setItemList(List<String> memberNameList){
		this.itemList = memberNameList;
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
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent){		
		Composite composite = (Composite)super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));
		
		Label numberLabel = new Label(parent, SWT.SINGLE|SWT.BORDER);
		numberLabel.setText(message);
		numberLabel.setFont(new Font(null, "", 10, 0));
		GridData numberLabelGD = new GridData();
		numberLabelGD.horizontalAlignment = GridData.FILL;
		numberLabelGD.grabExcessHorizontalSpace=true;
		numberLabel.setLayoutData(numberLabelGD);
		
		Tree tree = new Tree(composite.getShell(),SWT.BORDER|SWT.MULTI|SWT.CHECK);
		for(int i=0;i<itemList.size();i++){
			String skillName = itemList.get(i);
			TreeItem item = new TreeItem(tree,SWT.NULL);
			item.setText(skillName);
			for(int j=0;j<selectedItemList.size();j++){
				if(selectedItemList.get(j).equals(skillName)){
					item.setChecked(true);break;
				}
			}
			itemTreeList.add(item);
		}
		return composite;
	}
	
	/**
	 * Get selected item list for local. Information of TreeItem cannot be get from main view.
	 * @return
	 */
	private List<String> getSelectedItemListForLocal(){
		List<String> selectedItemList = new ArrayList<String>();
		selectedItemNumber = new ArrayList<Integer>();
		for(int i=0;i<itemTreeList.size();i++){
			boolean isChecked = itemTreeList.get(i).getChecked();
			if(isChecked){
				selectedItemList.add(itemList.get(i));
				selectedItemNumber.add(i);
			}
		}
		return selectedItemList;
	}
	
	/**
	 * Get selected item list.
	 * @return
	 */
	public List<String> getSelectedItemList(){
		return selectedItemList;
	}
	
	/**
	 * Get selected numbers list.
	 * @return
	 */
	public List<Integer> getSelectedItemNumber(){
		return selectedItemNumber;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent,IDialogConstants.OK_ID,"Register", false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId){
		if(buttonId == IDialogConstants.OK_ID){
			selectedItemList = getSelectedItemListForLocal();
			setReturnCode(buttonId);
			close();
			super.buttonPressed(buttonId);
		}else if(buttonId == IDialogConstants.CLOSE_ID){
			setReturnCode(buttonId);
		}
	}
}

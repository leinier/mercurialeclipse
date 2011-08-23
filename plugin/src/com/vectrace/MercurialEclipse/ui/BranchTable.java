/*******************************************************************************
 * Copyright (c) 2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jerome Negre              - implementation
 *     Brian Wallis              - adaptation to branches
 *     Andrei Loskutov - bug fixes
 *     Zsolt Kopany (Intland)    - bug fixes
 *******************************************************************************/
package com.vectrace.MercurialEclipse.ui;

import java.util.Comparator;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.vectrace.MercurialEclipse.HgRevision;
import com.vectrace.MercurialEclipse.model.Branch;

/**
 * @author Jerome Negre <jerome+hg@jnegre.org>
 * @author <a href="mailto:zsolt.koppany@intland.com">Zsolt Koppany</a>
 * @version $Id$
 */
public class BranchTable extends Composite {
	private static final Font PARENT_FONT = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);

	private final Table table;
	private int[] parents;
	private boolean showTip = true;

	private String highlightBranch;

	private Branch[] branches;

	@SuppressWarnings("unchecked")
	public BranchTable(Composite parent) {
		super(parent, SWT.NONE);

		this.setLayout(new GridLayout());
		this.setLayoutData(new GridData());

		table = new Table(this, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.VIRTUAL);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(data);

		String[] titles = {
				Messages.getString("BranchTable.column.rev"), Messages.getString("BranchTable.column.global"), Messages.getString("BranchTable.column.branch"), Messages.getString("BranchTable.column.active") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Comparator[] comparators = { new Comparator<Branch>() {
			public int compare(Branch o1, Branch o2) {
				return TableSortListener.sort(o1.getRevision(), o2.getRevision());
			}
		}, new Comparator<Branch>() {
			public int compare(Branch o1, Branch o2) {
				return o1.getGlobalId().compareTo(o2.getGlobalId());
			}
		}, new Comparator<Branch>() {
			public int compare(Branch o1, Branch o2) {
				return o1.getName().compareTo(o2.getName());
			}
		}, new Comparator<Branch>() {
			public int compare(Branch o1, Branch o2) {
				return TableSortListener.sort(o1.isActive() ? 1 : 0, o2.isActive() ? 1 : 0);
			}
		} };
		int[] widths = { 60, 150, 300, 70 };

		Listener sortListener = new TableSortListener(table, comparators) {
			@Override
			protected Object[] getData() {
				return branches;
			}
		};

		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(widths[i]);
			column.addListener(SWT.Selection, sortListener);
		}

		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				TableItem row = (TableItem) event.item;
				int index = table.indexOf(row);

				if (branches != null && 0 <= index && index < branches.length) {
					Branch branch = branches[index];
					if (showTip || !HgRevision.TIP.getChangeset().equals(branch.getName())) {
						if ((parents != null && isParent(branch.getRevision()))
								|| Branch.same(highlightBranch, branch.getName())) {
							row.setFont(PARENT_FONT);
						}
						row.setText(0, Integer.toString(branch.getRevision()));
						row.setText(1, branch.getGlobalId());
						row.setText(2, branch.getName());
						row.setText(3, branch.isActive() ? Messages
								.getString("BranchTable.stateActive") //$NON-NLS-1$
								: Messages.getString("BranchTable.stateInactive")); //$NON-NLS-1$
						row.setData(branch);
					}
				}
			}
		});
	}

	public void hideTip() {
		this.showTip = false;
	}

	public void highlightParents(int[] newParents) {
		this.parents = newParents;
	}

	public void setBranches(Branch[] branches) {
		table.clearAll();
		table.setItemCount(branches.length);
		this.branches = branches;
	}

	public Branch getSelection() {
		TableItem[] selection = table.getSelection();
		if (selection.length == 0) {
			return null;
		}
		return (Branch) selection[0].getData();
	}

	public void addSelectionListener(SelectionListener listener) {
		table.addSelectionListener(listener);
	}

	private boolean isParent(int r) {
		switch (parents.length) {
			case 2:
				if (r == parents[1]) {
					return true;
				}
				//$FALL-THROUGH$
			case 1:
				if (r == parents[0]) {
					return true;
				}
				//$FALL-THROUGH$
			default:
				return false;
		}
	}

	/**
	 * @param branch non null branch to highlight
	 */
	public void highlightBranch(String branch) {
		this.highlightBranch = branch;
	}
}
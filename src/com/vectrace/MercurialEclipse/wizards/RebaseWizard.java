/*******************************************************************************
 * Copyright (c) 2005-2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * bastian	implementation
 *     Andrei Loskutov (Intland) - bug fixes
 *******************************************************************************/
package com.vectrace.MercurialEclipse.wizards;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;
import com.vectrace.MercurialEclipse.commands.HgClients;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.operations.RebaseOperation;

/**
 * @author bastian
 */
public class RebaseWizard extends HgWizard {

	private final HgRoot hgRoot;
	private RebasePage rebasePage;

	public RebaseWizard(HgRoot hgRoot) {
		super(Messages.getString("RebaseWizard.title")); //$NON-NLS-1$
		this.hgRoot = hgRoot;
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		int srcRev = -1;
		int baseRev = -1;
		int destRev = -1;
		if (rebasePage.isSourceRevSelected()) {
			srcRev = rebasePage.getSelectedSrcIndex();
		}
		if (rebasePage.isBaseRevSelected()) {
			baseRev = rebasePage.getSelectedSrcIndex();
		}
		if (rebasePage.isDestRevSelected()) {
			destRev = rebasePage.getSelectedDestIndex();
		}
		boolean collapse = rebasePage.isCollapseRevSelected();
		boolean keepBranches = rebasePage.isKeepBranchesSelected();

		RebaseOperation op = new RebaseOperation(getContainer(), hgRoot,
				srcRev, destRev, baseRev, collapse,
				false, false, keepBranches);
		try {
			getContainer().run(true, false, op);
			if (op.getResult().length() != 0) {
				HgClients.getConsole().printMessage(op.getResult(), null);
			}
			return true;
		} catch (Exception e) {
			MercurialEclipsePlugin.logError(e.getCause());
			rebasePage.setErrorMessage(e.getLocalizedMessage());
			return false;
		}

	}

	@Override
	public void addPages() {
		rebasePage = new RebasePage("RebasePage", Messages.getString("RebaseWizard.rebasePage.title"), //$NON-NLS-1$ //$NON-NLS-2$
				MercurialEclipsePlugin
						.getImageDescriptor("wizards/droplets-50.png"), //$NON-NLS-1$
				Messages.getString("RebaseWizard.rebasePage.description"), //$NON-NLS-1$
				hgRoot);

		initPage(rebasePage.getDescription(), rebasePage);
		addPage(rebasePage);
	}

}

/*******************************************************************************
 * Copyright (c) 2006-2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jerome Negre - implementation
 *******************************************************************************/
package com.vectrace.MercurialEclipse.menu;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.team.core.TeamException;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;
import com.vectrace.MercurialEclipse.commands.HgIgnoreClient;
import com.vectrace.MercurialEclipse.dialogs.IgnoreDialog;
import com.vectrace.MercurialEclipse.team.cache.MercurialStatusCache;

public class HgIgnoreHandler extends SingleResourceHandler {

	@Override
	protected void run(IResource resource) throws Exception {
		IgnoreDialog dialog;
		switch(resource.getType()) {
			case IResource.FILE:
				dialog = new IgnoreDialog(getShell(), (IFile)resource);
				break;
			case IResource.FOLDER:
				dialog = new IgnoreDialog(getShell(), (IFolder)resource);
				break;
			default:
				dialog = new IgnoreDialog(getShell());
		}
		
		if(dialog.open() == IDialogConstants.OK_ID) {
			switch(dialog.getResultType()) {
				case FILE:
					HgIgnoreClient.addFile(dialog.getFile());
					break;
				case EXTENSION:
					HgIgnoreClient.addExtension(dialog.getFile());
					break;
				case FOLDER:
					HgIgnoreClient.addFolder(dialog.getFolder());
					break;
				case GLOB:
					HgIgnoreClient.addGlob(resource.getProject(), dialog.getPattern());
					break;
				case REGEXP:
					HgIgnoreClient.addRegexp(resource.getProject(), dialog.getPattern());
					break;
			}
			try {
			    IProject project = resource.getProject();
			    // if there is a .hgignore at project level, get it via a
                // refresh.
                IResource hgIgnoreFile = project.getFile(".hgignore"); //$NON-NLS-1$
                hgIgnoreFile.refreshLocal(IResource.DEPTH_ZERO, null);			        			    
		
                // refresh status of newly ignored resource
                MercurialStatusCache.getInstance().refreshStatus(resource,
                        new NullProgressMonitor());
			} catch (TeamException e) {
				MercurialEclipsePlugin.logError(Messages.getString("HgIgnoreHandler.unableToRefreshProject"), //$NON-NLS-1$
						e);
			}
		}
	}

}

/*******************************************************************************
 * Copyright (c) 2006-2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Software Balm Consulting Inc (Peter Hunnisett <peter_hge at softwarebalm dot com>) - implementation
 *     VecTrace (Zingo Andersen) - some updates
 *     Stefan Groschupf          - logError
 *     Stefan C                  - Code cleanup
 *******************************************************************************/
package com.vectrace.MercurialEclipse.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.operation.IRunnableContext;

import com.vectrace.MercurialEclipse.commands.AbstractClient;
import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.team.MercurialUtilities;

/**
 * @author Peter
 *
 *         Mercurial status operation.
 *
 */
public class StatusContainerAction extends HgOperation {

    private final IResource[] resources;

    public StatusContainerAction(IRunnableContext context, IResource[] resources) {
        super(context);

        this.resources = resources;
    }

    @Override
    protected String[] getHgCommand() {
        ArrayList<String> launchCmd = new ArrayList<String>(resources.length + 4);
        launchCmd.add(MercurialUtilities.getHGExecutable());
        launchCmd.add("status"); //$NON-NLS-1$
        launchCmd.add("--"); //$NON-NLS-1$
        if (resources.length == 0) {
            // System.out.println("StatusContainerAction::getHgCommand() resources.length == 0");
        }
        for (int res = 0; res < resources.length; res++) {
            // Mercurial doesn't control directories or projects and so will just return that they're
            // untracked.
            launchCmd.add(resources[res].getLocation().toOSString());
        }
        launchCmd.trimToSize();

        return launchCmd.toArray(new String[0]);
    }

    @Override
    public HgRoot getHgWorkingDir() throws HgException {
        return AbstractClient.getHgRoot(resources[0]);
    }

    @Override
    protected String getActionDescription() {
        return Messages.getString("StatusContainerAction.job.description1") + resources[0].getLocation() + Messages.getString("StatusContainerAction.job.description.2"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}

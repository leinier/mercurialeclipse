/*******************************************************************************
 * Copyright (c) 2005-2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * bastian	implementation
 *******************************************************************************/
package com.vectrace.MercurialEclipse.operations;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableContext;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;
import com.vectrace.MercurialEclipse.actions.HgOperation;
import com.vectrace.MercurialEclipse.commands.HgCloneClient;
import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.storage.HgRepositoryLocation;
import com.vectrace.MercurialEclipse.wizards.Messages;

public class CloneOperation extends HgOperation {

    private String parentDirectory;
    private HgRepositoryLocation repo;
    private boolean noUpdate;
    private boolean pull;
    private boolean uncompressed;
    private boolean timeout;
    private String rev;
    private String cloneName;
    private List<File> projectFiles;
    private Path projectLocation;

    /**
     * @param name
     */
    public CloneOperation(IRunnableContext context, String parentDirectory,
            HgRepositoryLocation repo, boolean noUpdate, boolean pull,
            boolean uncompressed, boolean timeout, String rev,
            String cloneName) {
        super(context);
        this.parentDirectory = parentDirectory;
        this.repo = repo;
        this.noUpdate = noUpdate;
        this.pull = pull;
        this.uncompressed = uncompressed;
        this.timeout = timeout;
        this.rev = rev;
        this.cloneName = cloneName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @SuppressWarnings("restriction")//$NON-NLS-1$
    @Override
    public void run(IProgressMonitor m) throws InvocationTargetException,
            InterruptedException {
        
        m.beginTask(Messages.getString("CloneRepoWizard.operation.name"), 50); //$NON-NLS-1$        

        m
                .subTask(Messages
                        .getString("CloneRepoWizard.subTaskParentDirectory.name") + parentDirectory); //$NON-NLS-1$
        m.worked(1);        

        m
                .subTask(Messages
                        .getString("CloneRepoWizard.subTaskCloneDirectory.name") + cloneName); //$NON-NLS-1$
        m.worked(1);

        try {

            m.subTask(Messages
                    .getString("CloneRepoWizard.subTask.invokingMercurial")); //$NON-NLS-1$
            HgCloneClient.clone(parentDirectory, repo, noUpdate, pull,
                    uncompressed, timeout, rev, cloneName);
            m.worked(1);
        } catch (HgException e) {
            MercurialEclipsePlugin.logError(e);
            throw new InvocationTargetException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vectrace.MercurialEclipse.actions.HgOperation#getActionDescription()
     */
    @Override
    protected String getActionDescription() {
        return Messages.getString("CloneRepoWizard.actionDescription.1") + repo + Messages.getString("CloneRepoWizard.actionDescription.2") + cloneName; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return the projectFiles
     */
    public List<File> getProjectFiles() {
        return projectFiles;
    }

    /**
     * @return the projectLocation
     */
    public Path getProjectLocation() {
        return projectLocation;
    }

    /**
     * @return the parentDirectory
     */
    public String getParentDirectory() {
        return parentDirectory;
    }

}
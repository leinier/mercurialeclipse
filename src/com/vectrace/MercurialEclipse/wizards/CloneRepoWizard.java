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
 *******************************************************************************/

package com.vectrace.MercurialEclipse.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;
import com.vectrace.MercurialEclipse.actions.RepositoryCloneAction;
import com.vectrace.MercurialEclipse.storage.HgRepositoryLocation;
import com.vectrace.MercurialEclipse.team.MercurialTeamProvider;

/*
 * 
 * This class implements the import wizard extension and the new wizard
 * extension.
 * 
 */

public class CloneRepoWizard extends SyncRepoWizard
{
	
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) 
  {
    setWindowTitle(Messages.getString("ImportWizard.WizardTitle")); //$NON-NLS-1$
    setNeedsProgressMonitor(true);
    syncRepoLocationPage = new SyncRepoPage(true,"CreateRepoPage","Create Repository Location","Create a repository location to clone","no repo name",null);
  }


  /* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish()
  {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IProject project = workspace.getRoot().getProject(projectName);
    final HgRepositoryLocation repo = new HgRepositoryLocation(locationUrl);
    
    // Check that this project doesn't exist.
    if( project.getLocation() != null )
    {
      // TODO: Ask if user wants to torch everything there before the clone, otherwise fail.
      System.out.println( "Project " + projectName + " already exists");
      return false;
    }
    
    RepositoryCloneAction cloneRepoAction = new RepositoryCloneAction(null, workspace, repo, parameters, projectName,null);

    try
    {
      cloneRepoAction.run();
    }
    catch (Exception e)
    {
    	MercurialEclipsePlugin.logError("Clone operation failed", e);
//      System.out.println("Clone operation failed");
//      System.out.println(e.getMessage());
    }

    // FIXME: Project creation must be done after the clone otherwise the
    // clone command will barf. Not quite sure why a destination directory isn't
    // really allowed to exist with the hg clone command...
    // At any rate we have a potential race condition on project creation if
    // anything to do with project is done before the clone operation.
    try
    {
      project.create(null);
      project.open(null);
    }
    catch(CoreException e)
    {
      // TODO: Should kill the project if we could map everything
      return false;      
    }

    try
    {
      // Register the project with Team. This will bring all the files that
      // we cloned into the project.
      RepositoryProvider.map(project, MercurialTeamProvider.class.getName());
      RepositoryProvider.getProvider(project, MercurialTeamProvider.class.getName());
    }
    catch(TeamException e)
    {
      // TODO: Should kill the project if we could map everything
      return false;
    }

    // It appears good. Stash the repo location.
    MercurialEclipsePlugin.getRepoManager().addRepoLocation(repo);

    return true;
  }	
  
}

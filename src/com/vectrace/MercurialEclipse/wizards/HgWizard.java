/*******************************************************************************
 * Copyright (c) 2005-2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bastian Doetsch	implementation
 *******************************************************************************/
package com.vectrace.MercurialEclipse.wizards;

import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;

/**
 * @author bastian
 * 
 */
public abstract class HgWizard extends Wizard {

    protected HgWizardPage page;
    protected Properties properties = null;

    public HgWizard(String windowTitle) {
        super();
        init(windowTitle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        HgWizardPage[] pages = (HgWizardPage[]) getPages();
        boolean retValue = true;
        NullProgressMonitor monitor = new NullProgressMonitor();
        for (HgWizardPage hgWizardPage : pages) {
            retValue &= hgWizardPage.finish(monitor);
        }
        return retValue;
    }

    /**
     * Initializes the wizard with data.
     */
    private void init(String windowTitle) {
        IDialogSettings workbenchSettings = MercurialEclipsePlugin.getDefault()
                .getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(getClass()
                .getCanonicalName());
        if (section == null) {
            section = workbenchSettings.addNewSection(getClass()
                    .getCanonicalName());
        }
        setDialogSettings(section);
        setWindowTitle(windowTitle);
    }

    /**
     * @param description
     * @param page
     */
    protected void initPage(String description, HgWizardPage wizardPage) {
        if (properties != null) {
            wizardPage.setProperties(properties);
        }
        wizardPage.setDescription(description);
        wizardPage.setDialogSettings(getDialogSettings());
    }

}

/*******************************************************************************
 * Copyright (c) 2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bastian Doetsch              - implementation
 *******************************************************************************/
package com.vectrace.MercurialEclipse.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.model.FlaggedAdaptable;
import com.vectrace.MercurialEclipse.preferences.MercurialPreferenceConstants;

public class HgResolveClient extends AbstractClient {

    /**
     * List merge state of files after merge
     * 
     * @param res
     * @return
     * @throws HgException
     */
    public static List<FlaggedAdaptable> list(IResource res) throws HgException {
        HgCommand command = new HgCommand("resolve", getWorkingDirectory(res),
                false);
        command
                .setUsePreferenceTimeout(MercurialPreferenceConstants.IMERGE_TIMEOUT);
        command.addOptions("-l");
        String[] lines = command.executeToString().split("\n");
        List<FlaggedAdaptable> result = new ArrayList<FlaggedAdaptable>();
        if (lines.length != 1 || !"".equals(lines[0])) {
            for (String line : lines) {
                IFile iFile = res.getProject().getFile(line.substring(2));
                FlaggedAdaptable fa = new FlaggedAdaptable(iFile, line
                        .charAt(0));
                result.add(fa);
            }
        }
        return result;
    }

    /**
     * Mark a resource as resolved ("R")
     * 
     * @param res
     * @return
     * @throws HgException
     */
    public static String markResolved(IResource res, FlaggedAdaptable adaptable)
            throws HgException {
        HgCommand command = new HgCommand("resolve", getWorkingDirectory(res),
                false);
        command
                .setUsePreferenceTimeout(MercurialPreferenceConstants.IMERGE_TIMEOUT);
        command.addOptions("-m", ((IFile) adaptable.getAdapter(IFile.class))
                .getProjectRelativePath().toOSString());
        return command.executeToString();
    }

    /**
     * Mark a resource as unresolved ("U")
     * 
     * @param res
     * @return
     * @throws HgException
     */
    public static String markUnresolved(IResource res,
            FlaggedAdaptable adaptable) throws HgException {
        HgCommand command = new HgCommand("resolve", getWorkingDirectory(res),
                false);
        command
                .setUsePreferenceTimeout(MercurialPreferenceConstants.IMERGE_TIMEOUT);
        command.addOptions("-u", ((IFile) adaptable.getAdapter(IFile.class))
                .getProjectRelativePath().toOSString());
        return command.executeToString();
    }

    /**
     * @return
     * @throws HgException
     */
    public static boolean checkAvailable() {
        try {
            HgCommand command = new HgCommand("help", ResourcesPlugin
                    .getWorkspace().getRoot(), false);
            command.addOptions("resolve");
            String result = new String(command.executeToBytes(5000, true));
            if (result.startsWith("hg: unknown command 'resolve'")) {
                return false;
            }
            return true;
        } catch (HgException e) {
            //MercurialEclipsePlugin.logError(e);
            return false;
        }
    }

}
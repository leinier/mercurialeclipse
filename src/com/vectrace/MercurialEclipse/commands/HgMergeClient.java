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

import org.eclipse.core.resources.IResource;

import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.preferences.MercurialPreferenceConstants;

public class HgMergeClient extends AbstractClient {

    public static String merge(IResource res, String revision)
            throws HgException {
        HgCommand command = new HgCommand("merge", getWorkingDirectory(res),
                false);
        command
                .setUsePreferenceTimeout(MercurialPreferenceConstants.IMERGE_TIMEOUT);
        
        command.addOptions("--config","ui.merge=internal:fail");
        if (revision != null) {
            command.addOptions("-r", revision);
        }
        
        try {
            String result = command.executeToString();
            return result;
        } catch (HgException e) {
            // if conflicts aren't resolved and no merge tool is started, hg
            // exits with 1
            if (!e.getMessage().startsWith("Process error, return code: 1")) {
                throw new HgException(e);
            }
            return e.getMessage();
        }
    }
}
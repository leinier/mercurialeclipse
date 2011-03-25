/*******************************************************************************
 * Copyright (c) 2006-2008 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bastian Doetsch
 *******************************************************************************/
package com.vectrace.MercurialEclipse.menu;

import org.eclipse.core.resources.IResource;

import com.vectrace.MercurialEclipse.commands.extensions.lock.HgUnLock;

public class UnLockHandler extends SingleResourceHandler {

	@Override
	public void run(IResource resource) throws Exception {
		HgUnLock lock = new HgUnLock(resource);
	}

}

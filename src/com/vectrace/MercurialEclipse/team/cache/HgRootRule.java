/*******************************************************************************
 * Copyright (c) 2010 Andrei Loskutov (Intland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Andrei Loskutov (Intland) - implementation
 *******************************************************************************/
package com.vectrace.MercurialEclipse.team.cache;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.team.MercurialTeamProvider;

/**
 * @author Andrei
 */
public class HgRootRule implements ISchedulingRule {

	private final HgRoot hgRoot;

	public HgRootRule(HgRoot hgRoot) {
		this.hgRoot = hgRoot;
	}

	public boolean contains(ISchedulingRule rule) {
		return isConflicting(rule);
	}

	public boolean isConflicting(ISchedulingRule rule) {
		if(!(rule instanceof HgRootRule)){
			if(rule instanceof IWorkspaceRoot){
				return false;
			}
			if(rule instanceof IResource){
				IResource resource = (IResource) rule;
				return getHgRoot().equals(MercurialTeamProvider.getHgRoot(resource.getProject()));
			}
			return false;
		}
		HgRootRule rootRule = (HgRootRule) rule;
		return getHgRoot().equals(rootRule.getHgRoot());
	}

	public HgRoot getHgRoot() {
		return hgRoot;
	}

}

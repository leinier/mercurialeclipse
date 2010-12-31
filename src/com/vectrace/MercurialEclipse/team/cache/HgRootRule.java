/*******************************************************************************
 * Copyright (c) 2010 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Andrei Loskutov 		- implementation
 *******************************************************************************/
package com.vectrace.MercurialEclipse.team.cache;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.RepositoryProvider;

import com.vectrace.MercurialEclipse.model.HgRoot;

/**
 * Exclusive rule for locking access to the resources related to same hg root.
 *
 * @author Andrei
 */
public class HgRootRule implements ISchedulingRule {

	private final HgRoot hgRoot;

	/**
	 * @param hgRoot non null
	 */
	public HgRootRule(HgRoot hgRoot) {
		Assert.isNotNull(hgRoot, "Trying to create HgRootRule without a hgRoot object");
		this.hgRoot = hgRoot;
	}

	public boolean contains(ISchedulingRule rule) {
		return isConflicting(rule);
	}

	/**
	 * Note: this method (used by Job API to detect deadlocks) should avoid made locking calls to
	 * avoid deadlocks, see issue 13474
	 */
	public boolean isConflicting(ISchedulingRule rule) {
		if(!(rule instanceof HgRootRule)){
			if(rule instanceof IWorkspaceRoot){
				return false;
			}
			if (rule instanceof IResource) {
				IProject project = ((IResource) rule).getProject();
				// isShared() call does not lock. In case project is not shared, we don't care
				if (project != null && RepositoryProvider.isShared(project)) {
					// hasHgRoot() locks but only in case the project was unshared before the call (issue 13474)
					// but now it's safe to use it as we know the project was already shared
					HgRoot resourceRoot = MercurialRootCache.getInstance().hasHgRoot((IResource) rule);
					if(resourceRoot == null) {
						return false;
					}
					return getHgRoot().equals(resourceRoot);
				}
			}
			return false;
		}
		HgRootRule rootRule = (HgRootRule) rule;
		return getHgRoot().equals(rootRule.getHgRoot());
	}

	public HgRoot getHgRoot() {
		return hgRoot;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HgRootRule [");
		builder.append("hgRoot=");
		builder.append(hgRoot);
		builder.append("]");
		return builder.toString();
	}

}

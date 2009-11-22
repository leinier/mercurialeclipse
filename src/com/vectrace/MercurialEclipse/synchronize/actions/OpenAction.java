/*******************************************************************************
 * Copyright (c) 2005-2009 VecTrace (Zingo Andersen) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrei Loskutov (Intland) - implementation
 *******************************************************************************/
package com.vectrace.MercurialEclipse.synchronize.actions;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentService;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;
import com.vectrace.MercurialEclipse.commands.HgParentClient;
import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.WorkingChangeSet;
import com.vectrace.MercurialEclipse.model.ChangeSet.Direction;
import com.vectrace.MercurialEclipse.model.ChangeSet.ParentChangeSet;
import com.vectrace.MercurialEclipse.synchronize.cs.ChangesetGroup;
import com.vectrace.MercurialEclipse.synchronize.cs.HgChangeSetContentProvider;
import com.vectrace.MercurialEclipse.synchronize.cs.HgChangeSetModelProvider;
import com.vectrace.MercurialEclipse.team.CompareAction;
import com.vectrace.MercurialEclipse.team.MercurialRevisionStorage;
import com.vectrace.MercurialEclipse.team.NullRevision;
import com.vectrace.MercurialEclipse.utils.CompareUtils;

/**
 * @author Andrei
 */
public class OpenAction extends Action {

	private final Action defaultOpen;
	private final ISynchronizePageConfiguration configuration;

	public OpenAction(Action defaultOpen, ISynchronizePageConfiguration configuration) {
		super();
		this.defaultOpen = defaultOpen;
		this.configuration = configuration;
	}

	@Override
	public void run() {
		Object input = configuration.getPage().getViewer().getInput();
		if(!(input instanceof HgChangeSetModelProvider)) {
			// this is the "default" workspace resources mode
			if(defaultOpen != null){
				defaultOpen.run();
			}
			return;
		}
		ISelection selection = configuration.getSite().getSelectionProvider().getSelection();
		if(!(selection instanceof IStructuredSelection)){
			return;
		}
		IStructuredSelection selection2 = (IStructuredSelection) selection;
		if(selection2.size() != 1){
			return;
		}
		Object object = selection2.getFirstElement();
		if(!(object instanceof IFile)){
			return;
		}
		IFile file = (IFile) object;
		Viewer viewer = configuration.getPage().getViewer();
		if(!(viewer instanceof ContentViewer)){
			return;
		}
		CommonViewer commonViewer = (CommonViewer) viewer;
		HgChangeSetContentProvider csProvider = getProvider(commonViewer.getNavigatorContentService());
		ChangeSet cs = csProvider.getParentOfSelection(file);
		if(cs == null){
			return;
		}

		if(cs instanceof WorkingChangeSet){
			// default: compare local file against parent changeset
			new CompareAction(file).run(this);
			return;
		}

		// XXX Andrei: the code below is copied from Incoming/Outgoing Page classes.
		// it has to be moved to a common class/method and verified if it works at all in all cases


		if(cs.getDirection() == Direction.OUTGOING){
			String[] parents = cs.getParents();
			MercurialRevisionStorage thisRev = new MercurialRevisionStorage(file, cs.getChangeset());
			MercurialRevisionStorage parentRev ;
			if(parents.length == 0){
				// TODO for some reason, we do not always have right parent info in the changesets
				// if we are on the different branch then the changeset. So simply enforce the parents resolving
				try {
					parents = HgParentClient.getParentNodeIds(file, cs);
				} catch (HgException e) {
					MercurialEclipsePlugin.logError(e);
				}
			}
			if(cs.getRevision().getRevision() == 0 || parents.length == 0){
				parentRev = new NullRevision(file, cs);
			} else {
				parentRev = new MercurialRevisionStorage(file, parents[0]);
			}
			CompareUtils.openEditor(thisRev, parentRev, false, false);
		} else {
			// incoming
			MercurialRevisionStorage remoteRev = new MercurialRevisionStorage(
					file, cs.getChangesetIndex(), cs.getChangeset(), cs);
			MercurialRevisionStorage parentRev ;
			String[] parents = cs.getParents();
			if(cs.getRevision().getRevision() == 0 || parents.length == 0){
				parentRev = new NullRevision(file, cs);
			} else {
				String parentId = parents[0];
				ChangeSet parentCs = null;
				ChangesetGroup group = csProvider.getParentGroup(cs);

				Set<ChangeSet> changesets = new TreeSet<ChangeSet>();
				if(group != null){
					changesets.addAll(group.getChangesets());
				}
				for (ChangeSet cset : changesets) {
					if(parentId.endsWith(cset.getChangeset())
							&& parentId.startsWith("" + cset.getChangesetIndex())){
						parentCs = cset;
						break;
					}
				}
				if(parentCs == null) {
					parentCs = new ParentChangeSet(parentId, cs);
				}
				parentRev = new MercurialRevisionStorage(
						file, parentCs.getChangesetIndex(), parentCs.getChangeset(), parentCs);
			}
			CompareUtils.openEditor(remoteRev, parentRev, false, false);
			// the line below compares the remote changeset with the local copy.
			// it was replaced with the code above to fix the issue 10364
			// CompareUtils.openEditor(file, cs, true, true);

		}
	}

	public static HgChangeSetContentProvider getProvider(INavigatorContentService service) {
		INavigatorContentExtension extensionById = service
				.getContentExtensionById(HgChangeSetContentProvider.ID);
		IContentProvider provider = extensionById.getContentProvider();
		if (provider instanceof HgChangeSetContentProvider) {
			return (HgChangeSetContentProvider) provider;
		}
		return null;
	}

}

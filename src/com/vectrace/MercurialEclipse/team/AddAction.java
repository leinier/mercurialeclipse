package com.vectrace.MercurialEclipse.team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.views.navigator.ResourceComparator;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;
import com.vectrace.MercurialEclipse.commands.HgAddClient;
import com.vectrace.MercurialEclipse.commands.HgStatusClient;
import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.ui.ResourceLabelProvider;
import com.vectrace.MercurialEclipse.ui.ResourcesTreeContentProvider;
import com.vectrace.MercurialEclipse.ui.UntrackedResourcesFilter;

public class AddAction extends MultipleResourcesAction {

	@Override
	protected void run(final List<IResource> resources) throws HgException {
		
		Set<IProject> roots = getRoots(resources);

		Map<IProject, Set<IPath>> untrackedFiles = new HashMap<IProject, Set<IPath>>();
		Map<IProject, Set<IPath>> untrackedFolders = new HashMap<IProject, Set<IPath>>();
		
		for(IProject project : roots) {
			String[] rawFiles = HgStatusClient.getUntrackedFiles(project);
			Set<IPath> files = new HashSet<IPath>();
			Set<IPath> folders = new HashSet<IPath>();
			
			for(String raw: rawFiles) {
				IPath path = new Path(raw);
				files.add(path);
				int count = path.segmentCount();
				for(int i=1; i< count; i++) {
					folders.add(path.removeLastSegments(i));
				}
			}
			
			untrackedFiles.put(project, files);
			untrackedFolders.put(project, folders);
		}

		ViewerFilter untrackedFilter = new UntrackedResourcesFilter(
				untrackedFiles,
				untrackedFolders);
		
		CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(
				getShell(),
				new ResourceLabelProvider(),
				new ResourcesTreeContentProvider(roots));
		
		dialog.setInput(ResourcesTreeContentProvider.ROOT);
		dialog.setTitle("Add to Version Control...");
		dialog.setMessage("Select the files to add to Mercurial");
		dialog.setContainerMode(true);
		dialog.setInitialElementSelections(resources);
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.addFilter(untrackedFilter);
		if(dialog.open() ==  IDialogConstants.OK_ID) {
			HgAddClient.addResources(filter(dialog.getResult()), null);
			 try {
					MercurialStatusCache.getInstance().refresh();
				} catch (TeamException e) {
					MercurialEclipsePlugin.logError(e);
			 }
		}
	}
	
	/**
	 * Only keep IFiles
	 * @param objects
	 * @return
	 */
	private List<IResource> filter(Object[] objects) {
		List<IResource> files = new ArrayList<IResource>();
		for(Object object : objects) {
			if(object instanceof IFile) {
				files.add((IFile)object);
			}
		}
		return files;
	}
	
	private Set<IProject> getRoots(List<IResource> resources) {
		Set<IProject> roots = new TreeSet<IProject>(new Comparator<IProject>() {
			public int compare(IProject p1, IProject p2) {
			return p1.getName().compareTo(p2.getName());
		}});
		for(IResource resource : resources) {
			roots.add(resource.getProject());
		}
		return roots;
	}
	
	
}

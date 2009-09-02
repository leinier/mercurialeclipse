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
package com.vectrace.MercurialEclipse.team.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.core.RepositoryProvider;

import com.vectrace.MercurialEclipse.MercurialEclipsePlugin;
import com.vectrace.MercurialEclipse.commands.AbstractClient;
import com.vectrace.MercurialEclipse.commands.HgClients;
import com.vectrace.MercurialEclipse.commands.HgIdentClient;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.preferences.MercurialPreferenceConstants;
import com.vectrace.MercurialEclipse.team.MercurialTeamProvider;

/**
 * The cache does NOT keeps the state automatically. Clients have explicitely request and manage
 * cache updates.
 * <p>
 * There is no guarantee that the data in the cache is up-to-date with the server. To get the latest
 * data, clients have explicitely refresh the cache before using it.
 * <p>
 * The cache does not maintain any states. If client "clear" this cache, it must make sure that they
 * request an explicit cache update. After "clear" and "refresh", a notification is sent to the
 * observing clients.
 * <p>
 * <b>Implementation note 1</b> the cache does not send any notifications...
 *
 * @author bastian
 * @author Andrei Loskutov
 */
public class LocalChangesetCache extends AbstractCache {

    private static final MercurialStatusCache STATUS_CACHE = MercurialStatusCache.getInstance();

    private static LocalChangesetCache instance;

    private final Map<IPath, SortedSet<ChangeSet>> localChangeSets;

    private int logBatchSize;

    private boolean isGetFileInformationForChangesets;

    private LocalChangesetCache() {
        super();
        localChangeSets = new HashMap<IPath, SortedSet<ChangeSet>>();
    }

    private boolean isGetFileInformationForChangesets() {
        return isGetFileInformationForChangesets;
    }

    public static synchronized LocalChangesetCache getInstance() {
        if (instance == null) {
            instance = new LocalChangesetCache();
        }
        return instance;
    }

    public void clear(IResource resource, boolean notify) {
        Set<IResource> members = getMembers(resource);
        members.add(resource);
        synchronized(localChangeSets){
            for (IResource member : members) {
                localChangeSets.remove(member.getLocation());
            }
        }
        if(resource instanceof IProject){
            clearChangesets(resource.getProject());
        }
    }

    @Override
    protected void clearProjectCache(IProject project) {
        super.clearProjectCache(project);
        clear(project, false);
    }

    public SortedSet<ChangeSet> getLocalChangeSets(IResource resource) throws HgException {
        IPath location = resource.getLocation();

        SortedSet<ChangeSet> revisions;
        synchronized(localChangeSets){
            revisions = localChangeSets.get(location);
            if (revisions == null) {
                if (resource.getType() == IResource.FILE
                        || resource.getType() == IResource.PROJECT
                        && STATUS_CACHE.isSupervised(resource)
                        && !STATUS_CACHE.isAdded(resource.getProject(), location)) {
                    refreshAllLocalRevisions(resource, true);
                    revisions = localChangeSets.get(location);
                }
            }
        }
        if (revisions != null) {
            return Collections.unmodifiableSortedSet(revisions);
        }
        return null;
    }

    /**
     * Gets changeset for given resource.
     *
     * @param resource
     *            the resource to get status for.
     * @return may return null
     * @throws HgException
     */
    public ChangeSet getNewestLocalChangeSet(IResource resource) throws HgException {
        SortedSet<ChangeSet> revisions = getLocalChangeSets(resource);
        if (revisions != null && revisions.size() > 0) {
            return revisions.last();
        }
        return null;
    }

    /**
     * Checks whether version is known.
     *
     * @param resource
     *            the resource to be checked.
     * @return true if known, false if not.
     * @throws HgException
     */
    public boolean isLocallyKnown(IResource resource) throws HgException {
        synchronized(localChangeSets){
            return localChangeSets.containsKey(resource.getLocation());
        }
    }

    /**
     * Refreshes all local revisions. If limit is set, it looks up the default
     * number of revisions to get and fetches the topmost till limit is reached.
     *
     * If preference is set to display changeset information on label decorator,
     * and a resource version can't be found in the topmost revisions,
     * the last revision of this file is obtained via additional
     * calls.
     *
     * @param res non null
     * @param limit whether to limit or to have full project log
     * @throws HgException
     *
     * @see #refreshAllLocalRevisions(IResource, boolean, boolean)
     */
    public void refreshAllLocalRevisions(IResource res, boolean limit) throws HgException {
        refreshAllLocalRevisions(res, limit, isGetFileInformationForChangesets());
    }

    /**
     * Refreshes all local revisions. If limit is set, it looks up the default
     * number of revisions to get and fetches the topmost till limit is reached.
     * <p>
     * A clear of all existing data for the given resource is triggered.
     * <p>
     * If withFiles is true and a resource version can't be found in the topmost
     * revisions, the last revision of this file is obtained via additional
     * calls.
     *
     * @param res non null
     * @param limit
     *            whether to limit or to have full project log
     * @param withFiles
     *            true = include file in changeset
     * @throws HgException
     */
    public void refreshAllLocalRevisions(IResource res, boolean limit,
            boolean withFiles) throws HgException {
        Assert.isNotNull(res);
        IProject project = res.getProject();
        if (project.isOpen() && null != RepositoryProvider.getProvider(project, MercurialTeamProvider.ID)) {
            clear(res, false);
            fetchLocalRevisions(res, limit, getLogBatchSize(), -1, withFiles);
        }
    }

    @Override
    protected void configureFromPreferences(IPreferenceStore store){
        logBatchSize = store.getInt(MercurialPreferenceConstants.LOG_BATCH_SIZE);
        if (logBatchSize < 0) {
            logBatchSize = 2000;
            MercurialEclipsePlugin.logWarning(Messages.localChangesetCache_LogLimitNotCorrectlyConfigured, null);
        }
        isGetFileInformationForChangesets = store.getBoolean(
                MercurialPreferenceConstants.RESOURCE_DECORATOR_SHOW_CHANGESET);
    }


    /**
     * Gets the configured log batch size.
     *
     * @return
     */
    public int getLogBatchSize() {
        return logBatchSize;
    }

    public ChangeSet getLocalChangeSet(IResource res, String nodeId) throws HgException {
        Assert.isNotNull(res);
        Assert.isNotNull(nodeId);
        ChangeSet changeSet = getChangeset(res.getProject(), nodeId);
        if (changeSet != null) {
            return changeSet;
        }
        synchronized (localChangeSets){
            changeSet = HgLogClient.getChangeset(res, nodeId, isGetFileInformationForChangesets());
            if (changeSet == null) {
                return changeSet;
            }
            // ok, the map has to  be updated with the new info
            if(!res.exists() || STATUS_CACHE.isSupervised(res)){
                // !res.exists() is the case for renamed (moved) or copied files which does not exist anymore
                HashSet<ChangeSet> set = new HashSet<ChangeSet>();
                set.add(changeSet);
                addChangesets(res.getProject(), set);
            }
        }
        return changeSet;
    }

    /**
     * @return may return null
     */
    public ChangeSet getCurrentWorkDirChangeset(IResource res) throws HgException {
        HgRoot root = HgClients.getHgRoot(res);
        String nodeId = HgIdentClient.getCurrentChangesetId(root);
        if (!"0000000000000000000000000000000000000000".equals(nodeId)) { //$NON-NLS-1$
            return getLocalChangeSet(res, nodeId);
        }
        return null;
    }


    /**
     * Fetches local revisions. If limit is set, it looks up the default
     * number of revisions to get and fetches the topmost till limit is reached.
     *
     * If a resource version can't be found in the topmost revisions, the last
     * revisions of this file (10% of limit number) are obtained via additional
     * calls.
     *
     * @param res non null
     * @param limit
     *            whether to limit or to have full project log
     * @param limitNumber
     *            if limit is set, how many revisions should be fetched
     * @param startRev
     *            the revision to start with
     * @throws HgException
     */
    public void fetchLocalRevisions(IResource res, boolean limit,
            int limitNumber, int startRev, boolean withFiles) throws HgException {
        Assert.isNotNull(res);
        IProject project = res.getProject();
        if (!project.isOpen() || !STATUS_CACHE.isSupervised(res)) {
            return;
        }
        HgRoot root = AbstractClient.getHgRoot(res);

        Map<IPath, SortedSet<ChangeSet>> revisions;
        // now we may change cache state, so lock
        synchronized(localChangeSets){
            if (limit) {
                revisions = HgLogClient.getProjectLog(res, limitNumber, startRev, withFiles);
            } else {
                revisions = HgLogClient.getCompleteProjectLog(res, withFiles);
            }
            if (revisions == null || revisions.size() <= 0) {
                return;
            }

            // every changeset is at least stored for the repository root
            if (res.getType() != IResource.PROJECT) {
                IPath rootPath = new Path(root.getAbsolutePath());
                localChangeSets.put(res.getLocation(), revisions.get(rootPath));
            }
            for (Map.Entry<IPath, SortedSet<ChangeSet>> mapEntry : revisions.entrySet()) {
                IPath path = mapEntry.getKey();
                SortedSet<ChangeSet> changes = mapEntry.getValue();
                // if changes for resource not in cache, get at least 1 revision
                if (changes == null && limit && withFiles
                        && STATUS_CACHE.isSupervised(project, path)
                        && !STATUS_CACHE.isAdded(project, path)) {

                    IResource myResource = convertRepoRelPath(root, project, root.toRelative(path.toFile()));
                    if (myResource != null) {
                        changes = HgLogClient.getRecentProjectLog(myResource, 1, withFiles).get(path);
                    }
                }
                // add changes to cache
                addChangesToLocalCache(project, path, changes);
            }
        }
    }

    @Override
    public synchronized void addObserver(Observer o) {
        // TODO current implementation was very inefficient: the only listener was
        // the decorator, and this one has generated NEW cache updates each time 
        // he was notified about changes, so it is an endless loop.
        // So temporary do not allow to observe this cache, until the code is improved
        // has no effect
        MercurialEclipsePlugin.logError(new UnsupportedOperationException("Observer not supported: " + o));
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        // has no effect
    }

    /**
     * Spawns an update job to notify all the clients about given resource changes
     * @param resource non null
     */
    @Override
    protected void notifyChanged(final IResource resource, boolean expandMembers) {
        // has no effect
        MercurialEclipsePlugin.logError(new UnsupportedOperationException("notifyChanged not supported"));
    }

    /**
     * Spawns an update job to notify all the clients about given resource changes
     * @param resources non null
     */
    @Override
    protected void notifyChanged(final Set<IResource> resources, final boolean expandMembers) {
        // has no effect
        MercurialEclipsePlugin.logError(new UnsupportedOperationException("notifyChanged not supported"));
    }

    /**
     * @param path absolute file path
     * @param changes may be null
     */
    private void addChangesToLocalCache(IProject project, IPath path, SortedSet<ChangeSet> changes) {
        if (changes != null && changes.size() > 0) {
            SortedSet<ChangeSet> existing = localChangeSets.get(path);
            if (existing == null) {
                existing = new TreeSet<ChangeSet>();
                localChangeSets.put(path, existing);
            }
            existing.addAll(changes);
            addChangesets(project, changes);
        }
    }

    public SortedSet<ChangeSet> getLocalChangeSetsByBranch(IProject project,
            String branchName) throws HgException {

        SortedSet<ChangeSet> changes = getLocalChangeSets(project);
        SortedSet<ChangeSet> branchChangeSets = new TreeSet<ChangeSet>();
        for (ChangeSet changeSet : changes) {
            String branch = changeSet.getBranch();
            if (branch.equals(branchName) || (branchName.equals("default") && branch.equals(""))) { //$NON-NLS-1$
                branchChangeSets.add(changeSet);
            }
        }
        return branchChangeSets;

    }
}

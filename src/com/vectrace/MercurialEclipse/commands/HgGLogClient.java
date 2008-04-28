package com.vectrace.MercurialEclipse.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import com.vectrace.MercurialEclipse.exception.HgException;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.GChangeSet;
import com.vectrace.MercurialEclipse.model.GChangeSet.RowCount;

public class HgGLogClient extends HgCommand
{
  private List<GChangeSet> sets = new ArrayList<GChangeSet>();
  
  public HgGLogClient(IResource resource) throws HgException
  {
    super("glog", (resource instanceof IFile) ? resource.getParent() : (IContainer)resource, false);
    addOptions("--config", "extensions.graphlog=");
    addOptions("--template", "{}"); // Removes everything
    if(resource instanceof IFile) {
      addFiles(resource.getName());
    }
    load(executeToString());
  }

  protected HgGLogClient(String text)
  {
    super("", true);
    load(text);
  }

  public void load(String s)
  {
    String[] split = s.split("\n");
    int length = split.length / 2;
    int lengthp1 = length + 1;
    RowCount rowCount = new RowCount();
    GChangeSet last = null;
    for(int i=0;i<lengthp1;i++)
    {
      int j = i*2;
      sets.add(last = new GChangeSet(rowCount, i, split[j], i != length ? split[j+1] : "")
      	.clean(last));
    }
  }

  public List<GChangeSet> getChangeSets()
  {
    return sets;
  }

  // TODO This is a (very) temporary fix to ensure we have enough elements
  public HgGLogClient update(Collection<ChangeSet> changeSets) {
	  int diff = changeSets.size() - sets.size();
	  if(diff > 0)
	  {
		  sets.addAll(Collections.nCopies(diff, (GChangeSet)null));
	  }
	  return this;
  }
}

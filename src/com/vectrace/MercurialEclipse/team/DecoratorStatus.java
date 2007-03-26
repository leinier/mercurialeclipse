/**
 * com.vectrace.MercurialEclipse (c) Vectrace 2006-jun-08
 * Created by zingo
 */
package com.vectrace.MercurialEclipse.team;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.vectrace.MercurialEclipse.exception.HgException;

/**
 * @author zingo
 *
 */
public class DecoratorStatus extends LabelProvider implements ILightweightLabelDecorator
{

  /**
   * 
   */
  public DecoratorStatus()
  {
    super();
//    System.out.println("MercurialEclipsePlugin:DecoratorStatus.DecoratorStatus()");
  }
   
  
  public static void refresh() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    String decoratorId = DecoratorStatus.class.getName();
    workbench.getDecoratorManager().update( decoratorId );
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
   */
  
  public void decorate(Object element, IDecoration decoration)
  {
//    System.out.println("MercurialEclipsePlugin:DecoratorStatus.decorate()");
    IResource objectResource;
    IProject objectProject;
    RepositoryProvider RepoProvider;
    
    objectResource = (IResource) element;
    if (objectResource == null)
    {
      return ;
    }
//    System.out.println("1 MercurialEclipsePlugin:DecoratorStatus.decorate()");
    
    objectProject = objectResource.getProject();
    if( RepositoryProvider.isShared( objectProject ) ) 
    {
      //A Shared Project
//      System.out.println("2 MercurialEclipsePlugin:DecoratorStatus.decorate()");
      RepoProvider = RepositoryProvider.getProvider( objectProject );
      if( ! (RepoProvider instanceof MercurialTeamProvider))
      {
        //Resource not unsing this plugin
        return;
      }
    }
    else
    { 
      //Not a Shared Project
      return;
    }
//    System.out.println("3 MercurialEclipsePlugin:DecoratorStatus.decorate():" + objectResource.toString() );

    if(MercurialUtilities.isResourceInReposetory(objectResource, true) != true)
    {
      //Resource could be inside a link or something do nothing
      // in the future this could check is this is another repository
//      System.out.println("4 MercurialEclipsePlugin:DecoratorStatus.decorate()");
      return;
    }

//    System.out.println("5 MercurialEclipsePlugin:DecoratorStatus.decorate()");

    
    // Decorating a Project   
    if (objectResource.getType() == IResource.PROJECT)
    {
//        decoration.addSuffix( "{PROJECT}" );
    }

    // Decorating a Folder
    else if (objectResource.getType() == IResource.FOLDER)
    {
      // Folders should not be decorated..
//      decoration.addSuffix( "{FOLDER}" );
    }
    
    else if (objectResource.getType() == IResource.FILE)
    {
      // Only files are decorated
//      decoration.addPrefix("{nofile->}" );
      
      IProject proj;
      String Repository;
      String FullPath;
      proj=objectResource.getProject();
      Repository=MercurialUtilities.getRepositoryPath(proj);
      if(Repository==null)
      {
        Repository="."; //never leave this empty add a . to point to current path
      }
      //Setup and run command
//     System.out.println("hg --cwd " + Repository + " status");
      
     
      FullPath=( objectResource.getLocation() ).toOSString();
      File workingDir=MercurialUtilities.getWorkingDir((IResource) objectResource);
      String fileName = MercurialUtilities.getResourceName((IResource) objectResource);

      System.out.println("hg status?");
      
      
      if(FullPath.indexOf(".hg") == -1)  //Do not decorate the stuff inder .hg
      {      
        System.out.println("hg status :)");

        String launchCmd[] = { MercurialUtilities.getHGExecutable(),"status", fileName };
        try
        {
          String output=MercurialUtilities.ExecuteCommand(launchCmd,workingDir,false);
          if(output!=null)
          {
            if(output.length()!=0)
            {
              //        decoration.addSuffix( "{" + output.substring(0,1)  + "}" );
              //          System.out.println("MercurialEclipsePlugin:DecoratorStatus.decorate(" + element.toString() + ", "+ output.substring(0,1) + ")");
              System.out.println("hg status output=" + output);
              decoration.addOverlay(DecoratorImages.getImageDescriptor(output));
            }
            else
            {
              //Managed and unchanged (No output from status)
              //          System.out.println("MercurialEclipsePlugin:DecoratorStatus.decorate(" + element.toString() + ", No output (managed?))");
              System.out.println("hg status managedDescriptor");
              decoration.addOverlay(DecoratorImages.managedDescriptor);      
            }
          }
        } catch (HgException e)
        {
          System.out.println(e.getMessage());
        }
      }
    }
    
//    System.out.println("MercurialEclipsePlugin:DecoratorStatus.decorate(" + element.toString() + ", "+ decoration.toString() + ")");
  }
  
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener)
  {
//    System.out.println("MercurialEclipsePlugin:DecoratorStatus.addListener()");

  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose()
  {
//    System.out.println("MercurialEclipsePlugin:DecoratorStatus.dispose()");

  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
   */
  public boolean isLabelProperty(Object element, String property)
  {
//    System.out.println("MercurialEclipsePlugin:DecoratorStatus.isLabelProperty()");
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener)
  {
//    System.out.println("MercurialEclipsePlugin:DecoratorStatus.removeListener()");

  }

}

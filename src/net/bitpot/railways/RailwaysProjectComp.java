package net.bitpot.railways;

import com.intellij.ProjectTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import net.bitpot.railways.api.Railways;
import org.jdesktop.swingx.action.ActionManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Plugin project component. Contains minimal functionality just to provide
 * required initialization to Railways.
 */
public class RailwaysProjectComp implements ProjectComponent
{
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(RailwaysProjectComp.class.getName());
    
    private Project myProject = null;

    private Railways railwaysAPI;


    public RailwaysProjectComp(Project project)
    {
        this.myProject = project;
    }

    
    public Project getProject()
    {
        return myProject;
    }

    public void initComponent()
    {
        railwaysAPI = new Railways(this);
    }




    public void disposeComponent()
    {
        // Do nothing now
    }

    @NotNull
    public String getComponentName()
    {
        return "Railways.ProjectComponent";
    }

    public void projectOpened()
    {
        // Init routes should be run when project is initialized.
        StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
            public void run() {
                railwaysAPI.getRoutesManager().initRouteList();
            }
        });
    }

    public void projectClosed()
    {
        // called when project is being closed
    }


    public Railways getRailwaysAPI()
    {
        return railwaysAPI;
    }
}
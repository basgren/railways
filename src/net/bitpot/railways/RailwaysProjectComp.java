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
 *
 */
public class RailwaysProjectComp implements ProjectComponent, Disposable
{
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(RailwaysProjectComp.class.getName());
    
    private Project myProject = null;

    private Module railsModule = null;
    private Railways railwaysAPI;

    private boolean isRails = false;


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
        //ModuleManager modMgr = ModuleManager.getInstance(myProject);

        //log.debug(">>> Init project component.");

        // At this moment there's no modules added to the project. They are added somewhere later by OpenAPI.

        myProject.getMessageBus().connect(this).subscribe(ProjectTopics.MODULES,
                new ProjectModulesListener());
        
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
            public void run() { railwaysAPI.initRouteList(); }
        });
    }

    public void projectClosed()
    {
        // called when project is being closed
    }

    @Override
    public void dispose()
    {
    }

    public Railways getRailwaysAPI()
    {
        return railwaysAPI;
    }

    public boolean isRailsProject()
    {
        return isRails;
    }

    private class ProjectModulesListener extends ModuleAdapter
    {
        @Override
        public void moduleAdded(Project project, Module module)
        {
            // Ignore event not addressed to this project.
            if (myProject != project)
                return;

            isRails = Railways.isRailsApp(module);

            if (isRails && (railsModule == null))
            {
                railsModule = module;

                // It seems that pSdk.getSdkType() was absent in Ruby Mine 4.0.3.
            }
        }

        @Override
        public void moduleRemoved(Project project, Module module)
        {
            // Ignore event not addressed to this project.
            if (myProject != project)
                return;
            
            if (railsModule == module)
            {
                railsModule = null;
            }
        }

    }
}

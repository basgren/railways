package net.bitpot.railways;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import net.bitpot.railways.api.Railways;
import net.bitpot.railways.api.RoutesManager;
import org.jetbrains.annotations.NotNull;

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
                RoutesManager rm = railwaysAPI.getActiveRoutesManager();
                if (rm != null)
                    rm.initRouteList();
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
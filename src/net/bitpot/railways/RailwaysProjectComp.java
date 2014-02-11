package net.bitpot.railways;

import com.intellij.ProjectTopics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import net.bitpot.railways.utils.RailwaysUtils;
import net.bitpot.railways.routesView.RoutesManager;
import net.bitpot.railways.routesView.RoutesView;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin project component. Contains minimal functionality just to provide
 * required initialization to Railways.
 */
public class RailwaysProjectComp implements ProjectComponent {
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(RailwaysProjectComp.class.getName());

    private Project myProject;
    private RailwaysUtils railwaysAPI;


    public RailwaysProjectComp(Project project) {
        myProject = project;
    }


    public Project getProject() {
        return myProject;
    }


    public void initComponent() {
        railwaysAPI = new RailwaysUtils(this);
    }


    public void disposeComponent() {
        // Do nothing now
    }


    @NotNull
    public String getComponentName() {
        return "Railways.ProjectComponent";
    }


    public void projectOpened() {
        myProject.getMessageBus().connect()
                .subscribe(ProjectTopics.MODULES, new ProjectModulesListener());
    }


    public void projectClosed() {
        // called when project is being closed
    }


    public RailwaysUtils getRailwaysAPI() {
        return railwaysAPI;
    }


    private class ProjectModulesListener extends ModuleAdapter {
        @Override
        public void moduleAdded(Project project, Module module) {
            if (project != myProject) return;

            // Notify RoutesView
            RoutesView routesView = RoutesView.getInstance(project);
            routesView.addModulePane(module);
        }


        @Override
        public void moduleRemoved(Project project, Module module) {
            if (project != myProject) return;

            // Notify RoutesView
            RoutesView routesView = RoutesView.getInstance(project);
            routesView.removeModulePane(module);
        }
    }
}
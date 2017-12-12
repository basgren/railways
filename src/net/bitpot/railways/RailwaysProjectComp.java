package net.bitpot.railways;

import com.intellij.ProjectTopics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
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


    public RailwaysProjectComp(Project project) {
        myProject = project;
    }


    public void initComponent() { /* Do nothing now */ }
    public void disposeComponent() { /* Do nothing now */ }


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


    private class ProjectModulesListener implements ModuleListener {
        @Override
        public void moduleAdded(@NotNull Project project, @NotNull Module module) {
            if (project != myProject) return;

            // Notify RoutesView
            RoutesView routesView = RoutesView.getInstance(project);
            routesView.addModulePane(module);
        }


        @Override
        public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
            if (project != myProject) return;

            // Notify RoutesView
            RoutesView routesView = RoutesView.getInstance(project);
            routesView.removeModulePane(module);
        }
    }
}
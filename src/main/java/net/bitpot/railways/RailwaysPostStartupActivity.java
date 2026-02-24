package net.bitpot.railways;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import net.bitpot.railways.routesView.RoutesView;
import org.jetbrains.annotations.NotNull;

public class RailwaysPostStartupActivity implements StartupActivity.DumbAware {
    private Project myProject;

    @Override
    public void runActivity(@NotNull Project project) {
        myProject = project;
        myProject.getMessageBus().connect()
            .subscribe(ProjectTopics.MODULES, new ProjectModulesListener());
    }


    private class ProjectModulesListener implements ModuleListener {
        @Override
        public void moduleAdded(@NotNull Project project, @NotNull Module module) {
            if (project == myProject) {
                RoutesView routesView = RoutesView.getInstance(project);
                routesView.addModulePane(module);
            }
        }


        @Override
        public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
            if (project == myProject) {
                RoutesView routesView = RoutesView.getInstance(project);
                routesView.removeModulePane(module);
            }
        }
    }
}
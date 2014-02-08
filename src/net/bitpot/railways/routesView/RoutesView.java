package net.bitpot.railways.routesView;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

/**
 * TODO: write doc
 */
public abstract class RoutesView {
    public static RoutesView getInstance(Project project) {
        return ServiceManager.getService(project, RoutesView.class);
    }


    public abstract void addModulePane(Module module);

    public abstract void removeModulePane(Module module);
}
package net.bitpot.railways.routesView;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.api.RoutesManager;
import net.bitpot.railways.routesView.impl.RoutesViewPane;
import org.jetbrains.annotations.Nullable;

/**
 * TODO: write doc
 */
public abstract class RoutesView {
    public static RoutesView getInstance(Project project) {
        return ServiceManager.getService(project, RoutesView.class);
    }

    @Nullable
    public abstract RoutesManager getCurrentRoutesManager();

    public abstract void addModulePane(Module module);

    public abstract void removeModulePane(Module module);
}
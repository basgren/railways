package net.bitpot.railways.routesView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;

public class RoutesViewToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project,
                                        @NotNull ToolWindow toolWindow) {
        RoutesView.getInstance(project).initToolWindow(toolWindow);
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return RailwaysUtils.hasRailsModules(project);
    }
}

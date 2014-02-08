package net.bitpot.railways.routesView.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import net.bitpot.railways.routesView.RoutesView;

/**
 * @author Basil Gren
 */
public class RoutesViewToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ((RoutesViewImpl) RoutesView.getInstance(project)).initToolWindow(toolWindow);
    }
}

package net.bitpot.railways.routesView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

/**
 * @author Basil Gren
 */
public class RoutesViewToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        RoutesView.getInstance(project).initToolWindow(toolWindow);
    }
}

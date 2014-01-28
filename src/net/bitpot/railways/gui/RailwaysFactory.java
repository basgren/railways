package net.bitpot.railways.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import net.bitpot.railways.api.Railways;
import net.bitpot.railways.models.RouteTableModel;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;

/**
 * Class implements ToolWindowFactory.
 */
public class RailwaysFactory implements ToolWindowFactory
{
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow)
    {
        MainPanel panel = new MainPanel(project);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel.getRootPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}

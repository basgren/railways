package net.bitpot.railways.routesView.impl;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import net.bitpot.railways.gui.MainPanel;
import net.bitpot.railways.routesView.RoutesView;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

import javax.swing.*;
import java.util.ArrayList;

/**
 * TODO: write doc
 */
public class RoutesViewImpl extends RoutesView implements Disposable {

    private Project myProject;
    private ContentManager myContentManager;

    private MainPanel mainPanel;

    private ArrayList<RoutesViewPane> myPanes = new ArrayList<>();


    public RoutesViewImpl(Project project) {
        myProject = project;
        mainPanel = new MainPanel(project);
    }


    /**
     * Initializes tool window.
     *
     * @param toolWindow
     */
    public synchronized void initToolWindow(final ToolWindow toolWindow) {
        myContentManager = toolWindow.getContentManager();

        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            toolWindow.setContentUiType(ToolWindowContentUiType.getInstance("combo"), null);
            toolWindow.getComponent().putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true");
        }

        // Add all modules that are already added till this moment.
        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        for (Module m : modules)
            addModulePane(m);

        // Add listener to update mainPanel when different module is selected
        myContentManager.addContentManagerListener(new ContentManagerAdapter() {
            @Override
            public void selectionChanged(ContentManagerEvent event) {
                // When user selects a module from tool window combo,
                // selectionChanges is called twice:
                // 1. With 'remove' operation -  for previously selected item,
                // 2. With 'add' operation - for newly selected item.
                if (event.getOperation() == ContentManagerEvent.ContentOperation.add)
                    viewSelectionChanged();
            }
        });
    }


    private void viewSelectionChanged() {
        Content content = myContentManager.getSelectedContent();
        if (content == null) return;

        // TODO: implement route panels switching.
    }


    @Override
    public void dispose() {
        // Do nothing now
    }


    private JComponent getComponent() {
        return mainPanel.getRootPanel();
    }


    @Override
    public void addModulePane(Module module) {
        // Skip if RoutesView is not initialized or if added module is not
        // Rails application.
        if ((myContentManager == null) ||
                (RailsApp.fromModule(module) == null))
            return;

        RoutesViewPane pane = new RoutesViewPane(myProject, module);

        // Register pane content, so we'll have a combo-box instead tool window
        // title, and each item will represent a module.
        Content content = myContentManager.getFactory().createContent(getComponent(),
                pane.getTitle(), false);
        content.setTabName(pane.getTitle());
        content.setIcon(pane.getIcon());

        // Set tool window icon to be the same as selected module icon
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        myContentManager.addContent(content);

        // Bind content with pane for further use
        pane.setContent(content);
        myPanes.add(pane);
    }


    @Override
    public void removeModulePane(Module module) {
        // Find corresponding content by module...
        for (RoutesViewPane pane : myPanes) {
            if (pane.getModule() != module) continue;

            // ... and remove it from panels list.
            myContentManager.removeContent(pane.getContent(), true);
            myPanes.remove(pane);
            break;
        }
    }
}
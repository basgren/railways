package net.bitpot.railways.routesView;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
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
import com.intellij.util.ui.UIUtil;
import net.bitpot.railways.actions.RailwaysActionsFields;
import net.bitpot.railways.gui.MainPanel;
import net.bitpot.railways.navigation.ChooseByRouteRegistry;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Implements tool window logic. Synchronizes the number of tool window panes
 * with the number of opened Rails modules in the project.
 */
public class RoutesView implements Disposable {

    public static RoutesView getInstance(Project project) {
        return ServiceManager.getService(project, RoutesView.class);
    }

    private Project myProject;
    private ContentManager myContentManager;

    private MainPanel mainPanel;

    private ArrayList<RoutesViewPane> myPanes = new ArrayList<>();
    private RoutesViewPane currentPane = null;

    // Hmmm... I don't remember why I needed this... Some glitches with action state update?
    private RailwaysActionsFields railwaysActionsFields = new RailwaysActionsFields();

    public RoutesView(Project project) {
        myProject = project;
        mainPanel = new MainPanel(project);
    }


    /**
     * Initializes tool window.
     *
     * @param toolWindow Tool window to initialize.
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

        // Add listener to update mainPanel when a module is selected from
        // tool window header.
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

        // Find selected pane by content.
        RoutesViewPane pane = null;
        for(RoutesViewPane p: myPanes)
            if (p.getContent() == content) {
                pane = p;
                break;
            }

        setCurrentPane(pane);
    }


    @Override
    public void dispose() {
        // Do nothing now
    }


    private JComponent getComponent() {
        return mainPanel.getRootPanel();
    }


    @Nullable
    public RoutesManager getCurrentRoutesManager() {
        return (currentPane == null) ? null : currentPane.getRoutesManager();
    }


    public void setCurrentPane(RoutesViewPane pane) {
        if (currentPane == pane)
            return;

        currentPane = pane;

        if (pane != null) {
            mainPanel.setDataSource(pane);
            syncPanelWithRoutesManager(pane.getRoutesManager());
        }
    }


    public void addModulePane(Module module) {
        // Skip if RoutesView is not initialized or if added module is not
        // Rails application.
        if ((myContentManager == null) ||
                (RailsApp.fromModule(module) == null))
            return;

        RoutesViewPane pane = new RoutesViewPane(module);

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

        // Register contributor
        ChooseByRouteRegistry.getInstance(myProject)
                .addContributorFor(pane.getRoutesManager());

        // Subscribe to RoutesManager events.
        pane.getRoutesManager().addListener(new MyRoutesManagerListener());

        // And select pane if it's the first one.
        if (myPanes.size() == 1)
            setCurrentPane(pane);
    }


    public void removeModulePane(Module module) {
        // Find corresponding content by module...
        for (RoutesViewPane pane : myPanes) {
            if (pane.getModule() != module) continue;

            // ... and remove it from panels list.
            myContentManager.removeContent(pane.getContent(), true);
            myPanes.remove(pane);

            // Register contributor
            ChooseByRouteRegistry.getInstance(myProject)
                    .removeContributor(pane.getRoutesManager());

            // TODO: handle currentPane if it's removed.
            break;
        }
    }


    /**
     * Returns an object with information used internally by plugin actions.
     *
     * @return Object with info
     */
    public RailwaysActionsFields getRailwaysActionsFields() {
        return railwaysActionsFields;
    }


    /**
     * Updates appearance of MainPanel according to the state of RoutesManager.
     *
     * @param routesManager Routes manager which state will be used for
     *                      appearance sync.
     */
    private void syncPanelWithRoutesManager(RoutesManager routesManager) {
        switch(routesManager.getState()) {
            case RoutesManager.UPDATING:
                mainPanel.showLoading();
                break;

            case RoutesManager.UPDATED:
                mainPanel.setUpdatedRoutes(routesManager.getRouteList());
                break;

            case RoutesManager.ERROR:
                mainPanel.showRoutesUpdateError();
                break;
        }
    }


    private class MyRoutesManagerListener implements RoutesManagerListener {
        @Override
        public void stateChanged(final RoutesManager routesManager) {
            // Railways can invoke this event from another thread
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                public void run() {
                    // Synchronize with routesManager only if it belongs to
                    // currently selected pane.
                    if (routesManager == getCurrentRoutesManager())
                        syncPanelWithRoutesManager(routesManager);
                }
            });
        }
    }
}
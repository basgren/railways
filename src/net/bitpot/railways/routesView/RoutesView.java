package net.bitpot.railways.routesView;

import com.intellij.ide.PowerSaveMode;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import net.bitpot.railways.gui.MainPanel;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.navigation.ChooseByRouteRegistry;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Implements tool window logic. Synchronizes the number of tool window panes
 * with the number of opened Rails modules in the project.
 */
@State(
        name="RoutesToolWindow",
        storages= {
                @Storage(value = StoragePathMacros.WORKSPACE_FILE)
        }
)
public class RoutesView implements PersistentStateComponent<RoutesView.State>,
        Disposable {

    public static RoutesView getInstance(Project project) {
        return ServiceManager.getService(project, RoutesView.class);
    }

    @NotNull private final Project myProject;
    private ContentManager myContentManager;

    private MainPanel mainPanel;

    private ArrayList<RoutesViewPane> myPanes = new ArrayList<>();
    private RoutesViewPane currentPane = null;
    private ToolWindow myToolWindow;
    private MessageBusConnection myConnection;

    private State myState = new State();


    public RoutesView(@NotNull Project project) {
        myProject = project;
        mainPanel = new MainPanel(project);
        myConnection = myProject.getMessageBus().connect();

        // Subscribe on files changes to update Route list regularly.
        // We connect to project bus, as module bus don't work with this topic
        MessageBusConnection conn = project.getMessageBus().connect();
        conn.subscribe(PsiModificationTracker.TOPIC, new PSIModificationListener());
    }


    static class State {
        int selectedTabId;
        boolean hideMountedRoutes;
    }

    @Nullable
    @Override
    public RoutesView.State getState() {
        return myState;
    }


    @Override
    public void loadState(@NotNull RoutesView.State state) {
        myState = state;
    }


    /**
     * Initializes tool window.
     *
     * @param toolWindow Tool window to initialize.
     */
    synchronized void initToolWindow(final ToolWindow toolWindow) {
        myToolWindow = toolWindow;
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
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                // When user selects a module from tool window combo,
                // selectionChanges is called twice:
                // 1. With 'remove' operation -  for previously selected item,
                // 2. With 'add' operation - for newly selected item.
                if (event.getOperation() == ContentManagerEvent.ContentOperation.add) {
                    viewSelectionChanged();
                    refreshRouteActionsStatus();
                }
            }
        });


        // Open tab that was active in previous IDE session
        Content savedContent = myContentManager.getContent(myState.selectedTabId);
        if (savedContent != null)
            myContentManager.setSelectedContent(savedContent);

        mainPanel.getRouteFilter().setMountedRoutesVisible(!myState.hideMountedRoutes);

        myConnection.subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {

            /**
             * This method is called when ToolWindow changes its state, i.e.
             * expanded/collapsed, docked to another panel, etc.
             */
            @Override
            public void stateChanged() {
                // We have to check if our tool window is still registered, as
                // otherwise it will raise an exception when project is closed.
                if (ToolWindowManagerEx.getInstanceEx(myProject).getToolWindow("Routes") == null)
                    return;

                updateToolWindowOrientation(toolWindow);

                if (toolWindow.isVisible())
                    if (currentPane != null && currentPane.isRoutesInvalidated())
                        currentPane.updateRoutes();

                refreshRouteActionsStatus();
            }
        });

        updateToolWindowOrientation(toolWindow);
    }


    public boolean isMountedRoutesVisible() {
        return mainPanel.getRouteFilter().isMountedRoutesVisible();
    }


    public void setMountedRoutesVisible(boolean value) {
        mainPanel.getRouteFilter().setMountedRoutesVisible(value);
        myState.hideMountedRoutes = !value;
    }


    private void updateToolWindowOrientation(ToolWindow toolWindow) {
        if (toolWindow.isDisposed())
            return;

        ToolWindowAnchor anchor = toolWindow.getAnchor();
        boolean isVertical = (anchor == ToolWindowAnchor.LEFT ||
                anchor == ToolWindowAnchor.RIGHT);

        mainPanel.setOrientation(isVertical);
    }


    private void viewSelectionChanged() {
        Content content = myContentManager.getSelectedContent();
        if (content == null) return;

        // Find selected pane by content.
        RoutesViewPane pane = null;
        int index = 0;
        for(RoutesViewPane p: myPanes) {
            if (p.getContent() == content) {
                pane = p;
                myState.selectedTabId = index;
                break;
            }

            index++;
        }

        setCurrentPane(pane);
    }


    @Override
    public void dispose() {
        myConnection.disconnect();
    }


    private JComponent getComponent() {
        return mainPanel.getRootPanel();
    }


    @Nullable
    public RoutesManager getCurrentRoutesManager() {
        return (currentPane == null) ? null : currentPane.getRoutesManager();
    }


    private void setCurrentPane(RoutesViewPane pane) {
        if (currentPane == pane)
            return;

        currentPane = pane;

        if (pane != null) {
            mainPanel.setDataSource(pane);

            if (pane.isRoutesInvalidated())
                pane.updateRoutes();

            syncPanelWithRoutesManager(pane.getRoutesManager());
        }
    }


    public void addModulePane(Module module) {
        // Skip if RoutesView is not initialized or if added module is not
        // Rails application.
        RailsApp railsApp = RailsApp.fromModule(module);
        if ((myContentManager == null) || railsApp == null)
            return;

        // Register content, so we'll have a combo-box instead tool window
        // title, and each item will represent a module.
        String contentTitle = module.getName();
        Content content = myContentManager.getFactory().createContent(getComponent(),
                contentTitle, false);
        content.setTabName(contentTitle);
        content.setIcon(ModuleType.get(module).getIcon());

        // Set tool window icon to be the same as selected module icon
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        myContentManager.addContent(content);

        // Bind content with pane for further use
        RoutesViewPane pane = new RoutesViewPane(railsApp, myToolWindow, content);
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
        for (RoutesViewPane pane : myPanes)
            if (pane.getModule() == module) {
                // ... and remove it from panels list.
                myContentManager.removeContent(pane.getContent(), true);
                myPanes.remove(pane);

                // Remove contributor
                ChooseByRouteRegistry.getInstance(myProject)
                        .removeContributor(pane.getRoutesManager());

                Disposer.dispose(pane);
                break;
            }
    }


    /**
     * Updates appearance of MainPanel according to the state of RoutesManager.
     *
     * @param routesManager Routes manager which state will be used for
     *                      appearance sync.
     */
    private void syncPanelWithRoutesManager(RoutesManager routesManager) {
        switch(routesManager.getRoutesState()) {
            case RoutesManager.UPDATING:
                mainPanel.showLoadingMessage();
                break;

            case RoutesManager.UPDATED:
                mainPanel.setUpdatedRoutes(routesManager.getRouteList());
                break;

            case RoutesManager.ERROR:
                mainPanel.showRoutesUpdateError(routesManager.getParserErrorCode());
                break;
        }
    }

    private boolean isLiveHighlightingEnabled() {
        return currentPane.getRoutesManager().getState().liveActionHighlighting;
    }

    private void refreshRouteActionsStatus() {
        RoutesManager rm = currentPane.getRoutesManager();
        RouteList routes = rm.getRouteList();
        if (rm.isUpdating() || routes.size() == 0 || !isLiveHighlightingEnabled())
            return;

        RailwaysUtils.updateActionsStatus(currentPane.getModule(), routes);
        mainPanel.refresh();
    }


    private class PSIModificationListener implements PsiModificationTracker.Listener {
        final Alarm alarm = new Alarm();

        @Override
        public void modificationCountChanged() {
            if (PowerSaveMode.isEnabled() ||
                    myToolWindow == null || !myToolWindow.isVisible() ||
                    !isLiveHighlightingEnabled())
                return;

            alarm.cancelAllRequests();
            alarm.addRequest(RoutesView.this::refreshRouteActionsStatus, 1000, ModalityState.NON_MODAL);
        }
    }



    private class MyRoutesManagerListener implements RoutesManagerListener {
        @Override
        public void stateChanged(final RoutesManager routesManager) {
            // Railways can invoke this event from another thread
            UIUtil.invokeLaterIfNeeded(() -> {
                // Synchronize with routesManager only if it belongs to
                // currently selected pane.
                if (routesManager == getCurrentRoutesManager())
                    syncPanelWithRoutesManager(routesManager);
            });
        }
    }
}

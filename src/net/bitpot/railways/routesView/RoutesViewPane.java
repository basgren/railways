package net.bitpot.railways.routesView;

import com.intellij.ide.PowerSaveMode;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.*;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

/**
 * Stores data for a module panel in Routes tool window. Each project module
 * that can have its own routes has its own RoutesViewPane.
 */
public class RoutesViewPane implements Disposable {

    private final Module myModule;
    private Content myContent;
    private RoutesManager myRoutesManager;
    private PsiTreeChangeListener myRoutesChangeListener;
    private boolean isInvalidated = false;

    
    /**
     * Creates a separate panel for Rails module.
     *
     * @param railsApp Rails module that will be represented by this pane.
     * @param toolWindow Parent tool window
     */
    public RoutesViewPane(RailsApp railsApp, ToolWindow toolWindow, Content content) {
        myModule = railsApp.getModule();
        myContent = content;

        myRoutesManager =  ModuleServiceManager.getService(myModule, RoutesManager.class);
        myRoutesManager.initRouteList();
        
        myRoutesChangeListener = new MyPsiTreeChangeListener(
                railsApp.getRoutesFile(), toolWindow);
        
        PsiManager.getInstance(myModule.getProject())
                .addPsiTreeChangeListener(myRoutesChangeListener);
    }
    
    
    public void dispose() {
        PsiManager.getInstance(myModule.getProject())
                .removePsiTreeChangeListener(myRoutesChangeListener);
    }


    public Content getContent() {
        return myContent;
    }


    public Module getModule() {
        return myModule;
    }


    public RoutesManager getRoutesManager() {
        return myRoutesManager;
    }


    public boolean isRoutesInvalidated() {
        return isInvalidated;
    }

    
    public void updateRoutes() {
        RailwaysUtils.invokeAction("railways.UpdateRoutesList", myModule.getProject());
        isInvalidated = false;
    }
    
    
    /**
     * Sets flag when routes should be updated, but the actual update was
     * skipped by performance reasons (panel or tool window wasn't visible).
     */
    private void invalidateRoutes() {
        isInvalidated = true;
    }

    
    private class MyPsiTreeChangeListener extends PsiTreeChangeAdapter {
        private final Alarm alarm = new Alarm();
        private VirtualFile routesFile;
        private ToolWindow myToolWindow;

        public MyPsiTreeChangeListener(VirtualFile routesFile, ToolWindow toolWindow) {
            this.routesFile = routesFile;
            myToolWindow = toolWindow;
        }


        @Override
        public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
            if (PowerSaveMode.isEnabled() || !myRoutesManager.getState().autoUpdate)
                return;

            // Handle only changes in routes file.
            PsiFile f = event.getFile();
            if (f == null || !f.getVirtualFile().equals(routesFile))
                return;
            
            // Don't perform update if panel or tool window is invisible.
            if (!myToolWindow.isVisible() || !myContent.isSelected()) {
                invalidateRoutes();
                return;
            }

            alarm.cancelAllRequests();
            alarm.addRequest(new Runnable() {
                @Override
                public void run() {
                    updateRoutes();
                }
            }, 700, ModalityState.NON_MODAL);
        }
    }
}
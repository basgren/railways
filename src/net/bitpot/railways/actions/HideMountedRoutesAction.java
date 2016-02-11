package net.bitpot.railways.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.ui.ViewConstants;
import net.bitpot.railways.routesView.RoutesView;
import org.jetbrains.annotations.NotNull;

/**
 * 
 */
public class HideMountedRoutesAction extends ToggleAction {
    
    @Override
    public boolean isSelected(AnActionEvent event) {
        RoutesView view = getRoutesView(event.getProject());
        
        return (view != null) && (!view.isMountedRoutesVisible());
    }

    @Override
    public void setSelected(AnActionEvent event, boolean b) {
        RoutesView view = getRoutesView(event.getProject());
        if (view == null)
            return;
        
        view.setMountedRoutesVisible(!b);
    }
    
    
    private RoutesView getRoutesView(Project project) {
        if (project == null)
            return null;
        
        return RoutesView.getInstance(project);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        RoutesView view = RoutesView.getInstance(e.getProject());
        e.getPresentation().setEnabled(view.getViewMode() == ViewConstants.VIEW_MODE_TABLE);
    }
}

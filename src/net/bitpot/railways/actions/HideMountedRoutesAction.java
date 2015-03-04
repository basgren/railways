package net.bitpot.railways.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.RoutesFilter;
import net.bitpot.railways.routesView.RoutesView;

/**
 * 
 */
public class HideMountedRoutesAction extends ToggleAction {
    
    @Override
    public boolean isSelected(AnActionEvent event) {
        RoutesFilter filter = getRoutesFilter(event.getProject());
        
        return (filter != null) && (!filter.isMountedRoutesVisible());
    }

    @Override
    public void setSelected(AnActionEvent event, boolean b) {
        RoutesFilter filter = getRoutesFilter(event.getProject());
        if (filter == null)
            return;
        
        filter.setMountedRoutesVisible(!b);
    }
    
    
    private RoutesFilter getRoutesFilter(Project project) {
        if (project == null)
            return null;
        
        RoutesView rv = RoutesView.getInstance(project);
        if (rv == null) return null;
        
        return rv.getMainPanel().getRouteFilter();
    }
    
}

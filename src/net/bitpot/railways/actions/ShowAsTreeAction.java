package net.bitpot.railways.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import net.bitpot.railways.gui.ViewConstants;
import net.bitpot.railways.routesView.RoutesView;

/**
 * @author Basil Gren
 *         on 01.09.14.
 */
public class ShowAsTreeAction extends ToggleAction {

    @Override
    public boolean isSelected(AnActionEvent e) {
        if (e.getProject() == null)
            return false;

        RoutesView view = RoutesView.getInstance(e.getProject());
        return view.getViewMode() == ViewConstants.VIEW_MODE_TREE;
    }


    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        if (e.getProject() == null)
            return;

        RoutesView view = RoutesView.getInstance(e.getProject());
        view.setViewMode(state ? ViewConstants.VIEW_MODE_TREE : ViewConstants.VIEW_MODE_TABLE);
    }
}

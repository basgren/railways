package net.bitpot.railways.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.gui.RailwaysSettingsDialog;
import net.bitpot.railways.routesView.RoutesManager;
import net.bitpot.railways.routesView.RoutesView;
import org.jetbrains.annotations.NotNull;

/**
 * @author Basil Gren
 *         on 28.10.14.
 */
public class SettingsAction extends AnAction {

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);

        RoutesManager rm = RoutesView.getInstance(project).getCurrentRoutesManager();
        if (rm == null)
            return;

        RailwaysSettingsDialog.configure(rm.getModule());
    }


}

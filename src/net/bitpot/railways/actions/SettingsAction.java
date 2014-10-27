package net.bitpot.railways.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.gui.RailwaysSettingsDialog;

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
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        RailwaysSettingsDialog.configure(project);
    }


}

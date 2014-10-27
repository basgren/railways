package net.bitpot.railways.gui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 28.10.14.
 */
public class RailwaysSettingsForm {
    private JPanel rootPanel;
    private JBTextField routesTaskEdit;

    private final Project myProject;

    public RailwaysSettingsForm(Project project) {
        myProject = project;
    }

    public JComponent rootPanel() {
        return rootPanel;
    }



    /**
     * Resets form components to contain corresponding project settings.
     */
    public void reset() {

    }


    /**
     * Sets project settings from corresponding form component values.
     */
    public void apply() {

    }
}

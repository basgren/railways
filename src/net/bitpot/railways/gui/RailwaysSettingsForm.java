package net.bitpot.railways.gui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.ui.components.JBTextField;
import net.bitpot.railways.routesView.RoutesManager;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 28.10.14.
 */
public class RailwaysSettingsForm {
    private JPanel rootPanel;
    private JBTextField routesTaskEdit;

    private final Module myModule;

    public RailwaysSettingsForm(Module module) {
        myModule = module;
    }

    public JComponent rootPanel() {
        return rootPanel;
    }



    /**
     * Resets form components to contain corresponding project settings.
     */
    public void reset() {
        RoutesManager.State settings = getSettings();

        routesTaskEdit.setText(settings.routesTaskName);
    }


    /**
     * Sets project settings from corresponding form component values.
     */
    public void apply() {
        RoutesManager.State settings = getSettings();

        settings.routesTaskName = routesTaskEdit.getText();
    }


    private RoutesManager.State getSettings() {
        RoutesManager mgr = ModuleServiceManager.getService(myModule, RoutesManager.class);
        return mgr.getState();
    }
}

package net.bitpot.railways.gui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 28.10.14.
 */
public class RailwaysSettingsDialog extends DialogWrapper {

    private final RailwaysSettingsForm myPanel;

    protected RailwaysSettingsDialog(@NotNull Module module) {
        super(module.getProject());

        setTitle("Configure Railways - module '" + module.getName() + "'");

        // Create panel and reset components
        myPanel = new RailwaysSettingsForm(module);
        myPanel.reset();

        // Important to call inherited init() method.
        init();
    }


    /**
     * Factory method. It creates panel with dialog options. Options panel is located at the
     * center of the dialog's content pane. The implementation can return <code>null</code>
     * value. In this case there will be no options panel.
     *
     * @return center panel
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel.rootPanel();
    }


    public static void configure(Module module) {
        RailwaysSettingsDialog dlg = new RailwaysSettingsDialog(module);
        dlg.show();

        if (!dlg.isOK())
            return;

        dlg.myPanel.apply();

        dlg.dispose();
    }
}

package net.bitpot.railways.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 28.10.14.
 */
public class RailwaysSettingsDialog extends DialogWrapper {

    private final RailwaysSettingsForm myPanel;

    protected RailwaysSettingsDialog(@Nullable Project project) {
        super(project);

        setTitle("Configure Railways");

        // Create panel and reset components
        myPanel = new RailwaysSettingsForm(project);
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


    public static void configure(Project project) {
        RailwaysSettingsDialog dlg = new RailwaysSettingsDialog(project);
        dlg.show();

        if (!dlg.isOK())
            return;

        dlg.dispose();
    }
}

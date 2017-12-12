package net.bitpot.railways.gui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import net.bitpot.railways.routesView.RoutesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.RailsUtil;
import org.jetbrains.plugins.ruby.tasks.rake.RakeTaskModuleCache;
import org.jetbrains.plugins.ruby.tasks.rake.task.RakeTask;

import javax.swing.*;
import java.util.List;

/**
 * @author Basil Gren
 *         on 28.10.14.
 */
public class RailwaysSettingsForm {
    private JPanel rootPanel;

    // Autocomplete field is inherited from LanguageTextField, but doesn't have
    // default constructor, so we'd better use parent class LanguageTextField.
    private LanguageTextField routesTaskEdit;
    private JComboBox<String> environmentCombo;
    private JCheckBox autoUpdateChk;
    private JCheckBox liveActionHighlightingChk;

    private final Module myModule;

    public RailwaysSettingsForm(Module module) {
        myModule = module;
    }


    private TextFieldWithAutoCompletion<RakeTask> createRakeTaskNamesEdit() {

        TextFieldWithAutoCompletionListProvider<RakeTask> listProvider =
                new TextFieldWithAutoCompletionListProvider<RakeTask>(null) {
                    @Nullable
                    @Override
                    protected Icon getIcon(@NotNull RakeTask rakeTask) {
                        return RailwaysIcons.RAKE;
                    }

                    @NotNull
                    @Override
                    protected String getLookupString(@NotNull RakeTask rakeTask) {
                        if (rakeTask.getFullCommand() == null)
                            return "";

                        return rakeTask.getFullCommand();
                    }

                    @Nullable
                    @Override
                    protected String getTailText(@NotNull RakeTask rakeTask) {
                        return " " + rakeTask.getDescription();
                    }

                    @Nullable
                    @Override
                    protected String getTypeText(@NotNull RakeTask rakeTask) {
                        return null;
                    }

                    @Override
                    public int compare(RakeTask rakeTask, RakeTask rakeTask2) {
                        return StringUtil.compare(rakeTask.getFullCommand(),
                                rakeTask2.getFullCommand(), false);
                    }
                };


        TextFieldWithAutoCompletion<RakeTask> field = new TextFieldWithAutoCompletion<>(
                myModule.getProject(), listProvider, true, null);

        RakeTaskModuleCache cachedTasks = RakeTaskModuleCache.getInstance(myModule);
        List<RakeTask> rakeList = cachedTasks != null ? cachedTasks.getRakeTaskCmdsList() : null;
        listProvider.setItems(rakeList);

        return field;
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
        initRailsEnvsComboBox(settings.environment, environmentCombo, myModule);
        autoUpdateChk.setSelected(settings.autoUpdate);
        liveActionHighlightingChk.setSelected(settings.liveActionHighlighting);
    }


    /**
     * Sets project settings from corresponding form component values.
     */
    public void apply() {
        RoutesManager.State settings = getSettings();

        settings.routesTaskName = routesTaskEdit.getText();
        settings.environment = environmentCombo.getSelectedIndex() == 0 ? null :
                (String)(environmentCombo.getSelectedItem());
        settings.autoUpdate = autoUpdateChk.isSelected();
        settings.liveActionHighlighting = liveActionHighlightingChk.isSelected();
    }


    private void initRailsEnvsComboBox(@Nullable String value,
                                       @NotNull JComboBox<String> combo,
                                       @Nullable Module module) {
        String[] envs = RailsUtil.getAllEnvironments(module);
        String[] strings = new String[envs.length + 1];
        strings[0] = "Default";
        System.arraycopy(envs, 0, strings, 1, envs.length);
        combo.setModel(new DefaultComboBoxModel<>(strings));
        combo.setSelectedItem(value == null ? "Default" : value);
    }


    private RoutesManager.State getSettings() {
        RoutesManager mgr = ModuleServiceManager.getService(myModule, RoutesManager.class);
        return mgr.getState();
    }


    private void createUIComponents() {
        routesTaskEdit = createRakeTaskNamesEdit();
    }
}

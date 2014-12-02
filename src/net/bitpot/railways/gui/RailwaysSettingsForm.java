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


        TextFieldWithAutoCompletion<RakeTask> field = new TextFieldWithAutoCompletion<RakeTask>(
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


    private void createUIComponents() {
        routesTaskEdit = createRakeTaskNamesEdit();
    }
}

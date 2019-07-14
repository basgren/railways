package net.bitpot.railways.utils;

import com.intellij.execution.ExecutionModes;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import net.bitpot.railways.gui.ErrorInfoDlg;
import net.bitpot.railways.gui.StringFormatter;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.routesView.RoutesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

/**
 * Class that contains all API methods for Railways plugin.
 */
public class RailwaysUtils {
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(RailwaysUtils.class.getName());

    public final static StringFormatter STRIP_REQUEST_FORMAT = RailwaysUtils::stripRequestFormat;

    /**
     * Returns true if specified project has at least one Ruby on Rails module.
     *
     * @param project Project which should be checked for Rails modules.
     * @return True if a project has at least one Ruby on Rails module.
     */
    public static boolean hasRailsModules(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module m : modules)
            if (RailsApp.fromModule(m) != null)
                return true;

        return false;
    }


    /**
     * Internally used method that runs rake task and gets its output. This
     * method should be called from backgroundable task.
     *
     * @param module Rails module for which rake task should be run.
     * @return Output of 'rake routes'.
     */
    @Nullable
    public static ProcessOutput queryRakeRoutes(Module module,
                                                String routesTaskName, String railsEnv) {
        // Get root path of Rails application from module.
        RailsApp app = RailsApp.fromModule(module);
        if ((app == null) || (app.getRailsApplicationRoot() == null))
            return null;

        String moduleContentRoot = app.getRailsApplicationRoot().getPresentableUrl();

        ModuleRootManager mManager = ModuleRootManager.getInstance(module);
        Sdk sdk = mManager.getSdk();
        if (sdk == null) {
            Notifications.Bus.notify(new Notification("Railways",
                    "Railways Error",
                    "Cannot update route list for '" + module.getName() +
                    "' module, because its SDK is not set",
                    NotificationType.ERROR)
                    , module.getProject());
            return null;
        }

        try {
            railsEnv = (railsEnv == null) ? "" : "RAILS_ENV=" + railsEnv;

            // Will work on IntelliJ platform since 2017.3
            return RubyGemExecutionContext.create(sdk, "rake")
                    .withModule(module)
                    .withWorkingDirPath(moduleContentRoot)
                    .withExecutionMode(new ExecutionModes.SameThreadMode())
                    .withArguments(routesTaskName, "--trace", railsEnv)
                    .executeScript();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Shows a dialog with 'rake routes' error stacktrace.
     *
     * @param routesManager RoutesManager which error stacktrace should be
     *                      displayed.
     */
    public static void showErrorInfo(@NotNull RoutesManager routesManager) {
        ErrorInfoDlg.showError("Error information:",
                routesManager.getParseErrorStackTrace());
    }


    /**
     * Invokes action with specified ID. This method provides very simple
     * implementation of invoking action manually when ActionEvent and
     * DataContext are unavailable. Created DataContext in this method provides
     * only CommonDataKeys.PROJECT value.
     *
     * @param actionId ID of action to invoke
     * @param project Current project
     */
    public static void invokeAction(String actionId, final Project project) {
        AnAction act = ActionManager.getInstance().getAction(actionId);

        // For simple actions which don't heavily use data context, we can create
        // it manually.
        DataContext dataContext = dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId))
                return project;

            return null;
        };

        act.actionPerformed(new AnActionEvent(null, dataContext,
                ActionPlaces.UNKNOWN, act.getTemplatePresentation(),
                ActionManager.getInstance(), 0));
    }


    public static void updateActionsStatus(Module module, RouteList routeList) {
        RailsApp app = RailsApp.fromModule(module);
        if ((app == null) || (DumbService.isDumb(module.getProject())))
            return;

        // TODO: investigate multiple calls of this method when switching focus from code to tool window without any changes.

        for (Route route: routeList)
            route.updateActionStatus(app);
    }


    private final static String FORMAT_STR = "(.:format)";

    @NotNull
    public static String stripRequestFormat(@NotNull String routePath) {
        int endIndex = routePath.length() - FORMAT_STR.length();
        if (routePath.indexOf(FORMAT_STR) == endIndex)
            return routePath.substring(0, endIndex);

        return routePath;
    }

}
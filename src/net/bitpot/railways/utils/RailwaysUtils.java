package net.bitpot.railways.utils;

import com.intellij.ProjectTopics;
import com.intellij.execution.ExecutionMode;
import com.intellij.execution.ExecutionModes;
import com.intellij.execution.Output;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import net.bitpot.railways.RailwaysProjectComp;
import net.bitpot.railways.actions.RailwaysActionsFields;
import net.bitpot.railways.gui.ErrorInfoDlg;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.routesView.RoutesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.gem.GemsRunner;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Class that contains all API methods for Railways plugin.
 */
public class RailwaysUtils implements Disposable {
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(RailwaysUtils.class.getName());

    private Project myProject;

    // Contains list of Rails modules.
    private ArrayList<RoutesManager> routesManagerList = new ArrayList<>();

    private RailwaysActionsFields railwaysActionsFields = new RailwaysActionsFields();


    public RailwaysUtils(RailwaysProjectComp projectComponent) {
        myProject = projectComponent.getProject();

        // At this moment there's no modules added to the project. They are
        // added somewhere later by OpenAPI.
        myProject.getMessageBus().connect(this)
                .subscribe(ProjectTopics.MODULES, new ProjectModulesListener());
    }


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
     * Internally used method that runs rake task and gets its output. This method should be called
     * from backgroundable task.
     *
     * @param module Rails module for which rake task should be run.
     * @return Output of 'rake routes'.
     */
    @Nullable
    public static ProcessOutput queryRakeRoutes(Module module) {
        String errorTitle = "Error in rake command.";

        // Get root path of Rails application from module.
        RailsApp app = RailsApp.fromModule(module);
        if ((app == null) || (app.getRailsApplicationRoot() == null))
            return null;

        String moduleContentRoot = app.getRailsApplicationRoot().getPresentableUrl();


        ModuleRootManager mManager = ModuleRootManager.getInstance(module);
        Sdk sdk = mManager.getSdk();
        if (sdk == null) {
            Notifications.Bus.notify(new Notification("Railways", "Railways Error",
                    "Cannot update routes list, because SDK is not specified for the current project", NotificationType.ERROR)
                    , module.getProject());
            return null;
        }

        try {
            // TODO: Think about dropping support of older RubyMine versions.

            //  !!! Note!!! Since RubyMine 5 EAP (Build 122.633) runGemsExecutableScript
            // returns ProcessOutput object. Previous versions returned Output.
            // Let's do this trick through reflections.
            Method method = null;
            String[] params = {"routes", "--trace"};
            Object output = null;

            try {
                // This this declaration was until RubyMine 5 EAP (122.782)
                method = GemsRunner.class.getDeclaredMethod("runGemsExecutableScript",
                        Sdk.class, Module.class, String.class, String.class,
                        String.class, ExecutionMode.class, boolean.class,
                        String.class, java.lang.String[].class);

                output = method.invoke(null, sdk, module,
                        "rake", "rake",
                        moduleContentRoot,
                        new ExecutionModes.SameThreadMode(), false,
                        errorTitle, params);
            } catch (NoSuchMethodException e) { /* Do nothing */ }

            if (method == null) {
                // Try to find and invoke by declaration which was introduced since build 122.782
                method = GemsRunner.class.getDeclaredMethod("runGemsExecutableScript",
                        Sdk.class, Module.class, String.class, String.class,
                        String.class, ExecutionMode.class,
                        java.lang.String[].class);

                output = method.invoke(null, sdk, module,
                        "rake", "rake",
                        moduleContentRoot,
                        new ExecutionModes.SameThreadMode(),
                        params);
            }


            /* If support of older RubyMine versions (below 4.5) is dropped, we can use this:
            Object output = GemsRunner.runGemsExecutableScript(sdk, module,
                   "rake", "rake",
                   moduleContentRoot,
                   new ExecutionModes.SameThreadMode(), false,
                   errorTitle, "routes", "--trace");

            or this in the newest RubyMine 5:

            Object output = GemsRunner.runGemsExecutableScript(sdk, module,
                   "rake", "rake",
                   moduleContentRoot,
                   new ExecutionModes.SameThreadMode(),
                   "routes", "--trace");
            */


            // Here we must take into account that for RubyMine prior to 122.633 (RubyMine 5) build will be of an Output class,
            // but since 122.633 build it is ProcessOutput type.
            ProcessOutput processOutput;
            if (output instanceof Output)
                processOutput = convertToProcessOutput((Output) output);
            else
                processOutput = (ProcessOutput) output;


            return processOutput;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Converts Output object to ProcessOutput.
     *
     * @param output Output object to convert
     * @return ProcessOutput object
     */
    private static ProcessOutput convertToProcessOutput(@NotNull Output output) {
        ProcessOutput res = new ProcessOutput();

        // Unfortunately, methods setExitCode, appendStdout and
        // appendStderr are declared as private prior to 122.633 build.
        // So we will try to use reflection to call them.

        Method method;
        try {
            method = ProcessOutput.class.getDeclaredMethod("setExitCode", int.class);
            method.setAccessible(true);
            method.invoke(res, output.getExitCode());

            method = ProcessOutput.class.getDeclaredMethod("appendStdout", String.class);
            method.setAccessible(true);
            method.invoke(res, output.getStdout());

            method = ProcessOutput.class.getDeclaredMethod("appendStderr", String.class);
            method.setAccessible(true);
            method.invoke(res, output.getStderr());

        } catch (Exception e) {
            return null;
        }

        return res;
    }


    /**
     * Returns Railways API methods for specified project.
     *
     * @param project Project
     * @return Railways API object.
     */
    public static RailwaysUtils getAPI(@NotNull Project project) {
        RailwaysProjectComp comp = project.getComponent(RailwaysProjectComp.class);
        return comp.getRailwaysAPI();
    }


    /**
     * Returns a list of parsed routes.
     *
     * @return List of parsed routes.
     */
    public RouteList getRoutes() {
        // TODO: Debug stub. Rework!
        // Just to avoid many code changes at once.
        return getActiveRoutesManager().getRouteList();
    }


    public
    @Nullable
    RoutesManager getActiveRoutesManager() {
        if (routesManagerList.isEmpty())
            return null;

        // TODO: WIP - remove this stub
        return routesManagerList.get(0);
    }


    /**
     * Shows a dialog with error info.
     */
    public void showErrorInfo() {
        // TODO: Debug stub. Rework!
        ErrorInfoDlg.showError("Error information:",
                routesManagerList.get(0).getParseErrorStacktrace());
    }


    /**
     * Returns an object with information used internally by plugin actions.
     *
     * @return Object with info
     */
    public RailwaysActionsFields getRailwaysActionsFields() {
        return railwaysActionsFields;
    }


    public void navigateToRouteAction(@NotNull Route route) {
        navigateToRouteAction(route, false);
    }


    /**
     * Opens controller source file and moves cursor to the method that is
     * linked with passed route.
     *
     * @param route Route which controller action should be navigated to.
     */
    public void navigateToRouteAction(@NotNull Route route, boolean requestFocus) {
        if (route.getType() == Route.REDIRECT || route.getType() == Route.MOUNTED)
            return;

        RailsApp app = getRailsApp();
        if ((app == null) || route.getController().isEmpty())
            return;

        RailsController controller = app.findController(route.getController());
        if (controller == null)
            return;

        if (!route.getAction().isEmpty()) {
            RMethod method = controller.getAction(route.getAction());
            if (method != null)
                method.navigate(requestFocus);
        }
    }


    @Override
    public void dispose() {
        // Do nothing
    }


    /**
     * Returns RailsApp object which is returned only if opened project is a
     * Rails Application project. Otherwise null will be returned.
     *
     * @return RailsApp object or null.
     */
    public
    @Nullable
    RailsApp getRailsApp() {
        // TODO: rework this!!!
        if (routesManagerList.size() > 0)
            return RailsApp.fromModule(routesManagerList.get(0).getModule());

        return null;
    }


    private class ProjectModulesListener extends ModuleAdapter {
        @Override
        public void moduleAdded(Project project, Module module) {
            // Ignore event not addressed to this project.
            if (myProject != project)
                return;

            boolean isRails = RailsApp.fromModule(module) != null;

            if (isRails)
                routesManagerList.add(new RoutesManager(project, module));
        }


        @Override
        public void moduleRemoved(Project project, Module module) {
            // Ignore event not addressed to this project.
            if (myProject != project)
                return;

            // Find routes manager by module and remove it
            for (RoutesManager rm : routesManagerList)
                if (rm.getModule() == module) {
                    routesManagerList.remove(rm);
                    break;
                }
        }
    }

}
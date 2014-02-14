package net.bitpot.railways.utils;

import com.intellij.execution.ExecutionMode;
import com.intellij.execution.ExecutionModes;
import com.intellij.execution.Output;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import net.bitpot.railways.gui.ErrorInfoDlg;
import net.bitpot.railways.routesView.RoutesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.gem.GemsRunner;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

import java.lang.reflect.Method;

/**
 * Class that contains all API methods for Railways plugin.
 */
public class RailwaysUtils {
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(RailwaysUtils.class.getName());

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
     * Shows a dialog with 'rake routes' error stacktrace.
     *
     * @param routesManager RoutesManager which error stacktrace should be
     *                      displayed.
     */
    public static void showErrorInfo(@NotNull RoutesManager routesManager) {
        ErrorInfoDlg.showError("Error information:",
                routesManager.getParseErrorStacktrace());
    }
}
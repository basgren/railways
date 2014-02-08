package net.bitpot.railways.api;

import com.intellij.execution.ExecutionMode;
import com.intellij.execution.ExecutionModes;
import com.intellij.execution.Output;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.parser.RailsRoutesParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.gem.GemsRunner;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Class provides manipulation routines for a Rails module.
 */
public class RoutesManager {

    private LinkedList<RoutesManagerListener> listeners = new LinkedList<RoutesManagerListener>();

    // This field is set only when route update task is being executed.
    private ProgressIndicator routesUpdateIndicator = null;

    private ProcessOutput output;
    private Project project;

    private RailsRoutesParser parser;
    private RouteList routeList = null;

    // Rails module
    private Module module = null;


    public RoutesManager(Project project, Module railsModule) {
        this.project = project;
        parser = new RailsRoutesParser(project);
        module = railsModule;
    }


    public Module getModule() {
        return module;
    }


    // API methods


    public void addListener(RoutesManagerListener listener) {
        listeners.add(listener);
    }


    @SuppressWarnings("unused")
    public boolean removeListener(RoutesManagerListener listener) {
        return listeners.remove(listener);
    }


    @Nullable
    public RouteList getRouteList() {
        return routeList;
    }


    /**
     * Initializes route list. Does nothing if cache is disabled.
     * When cache is enabled, tries to get routes from cache and if not found
     * tries to update routes.
     */
    public void initRouteList() {
        String cachedOutput = getCachedOutput();
        if (cachedOutput != null) {
            parseRakeRoutesOutput(cachedOutput, null);
        } else
            updateRouteList();
    }


    /**
     * Returns true if routes update task is in progress.
     *
     * @return True if routes are being updated.
     */
    public boolean isUpdating() {
        return routesUpdateIndicator != null;
    }


    /**
     * Cancels route update if the task is in progress.
     */
    public void cancelRoutesUpdate() {
        if (!isUpdating())
            return;

        routesUpdateIndicator.cancel();
    }


    /**
     * Updates route list. The method starts task that call 'rake routes' and parses result after complete.
     * After routes are parsed, Routes panel is updated.
     *
     * @return True if update task is started, false if new task is not started because routes update is in progress.
     */
    public boolean updateRouteList() {
        if (isUpdating())
            return false;

        for (RoutesManagerListener l : listeners)
            l.beforeRoutesUpdate();

        // Start background task.
        (new UpdateRoutesTask()).queue();
        return true;
    }


    public String getParseErrorStacktrace() {
        return parser.getErrorStacktrace();
    }


    /**
     * Internal class that is responsible for updating routes list.
     */
    private class UpdateRoutesTask extends Task.Backgroundable {

        public UpdateRoutesTask() {
            super(project, "Rake task", true);

            setCancelText("Cancel task");
        }


        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.setText("Updating routes list...");
            indicator.setFraction(0.0);

            ModuleManager modMgr = ModuleManager.getInstance(getProject());
            final Module[] modules = modMgr.getModules();

            // Save indicator to be able to cancel task execution.
            routesUpdateIndicator = indicator;

            output = queryRakeRoutes(modules[0]);

            if (output == null)
                for (RoutesManagerListener l : listeners)
                    l.routesUpdated();


            indicator.setFraction(1.0);
        }


        @Override
        public void onSuccess() {
            routesUpdateIndicator = null;

            if ((output == null) || (!myProject.isOpen()) || myProject.isDisposed())
                return;

            parseRakeRoutesOutput(output.getStdout(), output.getStderr());
        }


        @Override
        public void onCancel() {
            routesUpdateIndicator = null;

            for (RoutesManagerListener l : listeners)
                l.routesUpdated();

            super.onCancel();
        }
    }


    /**
     * Internally used method that runs rake task and gets its output. This method should be called
     * from backgroundable task.
     *
     * @param module Module for which rake task should be run.
     * @return Output of 'rake routes'.
     */
    @Nullable
    private static ProcessOutput queryRakeRoutes(Module module) {
        String errorTitle = "Error in rake command.";

        // Here several options are possible:
        // 1) project.getLocation() - it is present in all versions, but it's
        //    already deprecated.
        // 2) project.getBasePath() - is absent in RubyMine 4.0.1.
        // 3) project.getPresentableUrl() - works fine.
        // The last option is used.
        String moduleContentRoot = module.getProject().getPresentableUrl();


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
     * Parses 'rake routes' output and notifies all listeners that route list was updated.
     *
     * @param stdOut Rake routes result.
     * @param stdErr Rake routes stderr output. Can be null.
     */
    private void parseRakeRoutesOutput(String stdOut, @Nullable String stdErr) {
        routeList = parser.parse(stdOut, stdErr);

        // After routes parsing we can have several situations:
        // 1. parser contains routes and isErrorReported = false. Everything is OK.
        // 2. parser contains no routes and isErrorsReported = true. It means that there was an exception thrown.
        // 3. parser contains routes and isErrorReported = true. In the most cases it's warnings (deprecation etc),
        //    so everything is OK.
        // TODO: possibly, we should report about warnings somehow.
        if (routeList.size() == 0 && parser.isErrorReported()) {
            for (RoutesManagerListener l : listeners)
                l.routesUpdateError();
        } else {
            cacheOutput(stdOut);

            for (RoutesManagerListener l : listeners)
                l.routesUpdated();
        }
    }


    /**
     * Saves passed output string to cache file and sets the same modification time as routes.rb has.
     *
     * @param output String that contains stdout of 'rake routes' command.
     */
    private void cacheOutput(String output) {
        try {
            long routesMTime = getRoutesFileMTime();

            File f = new File(getCacheFileName());
            FileUtil.writeToFile(f, output.getBytes(), false);
            f.setLastModified(routesMTime);
        } catch (Exception e) {
            // Do nothing
            //e.printStackTrace();
        }
    }


    /**
     * Returns modification time of routes.rb file for Rails project.
     *
     * @return Modification time of routes.rb file or 0 if it cannot be retrieved.
     */
    private long getRoutesFileMTime() {
        RailsApp railsApp = Railways.getAPI(project).getRailsApp();
        if (railsApp == null || railsApp.getRoutesFile() == null)
            return 0;

        String routesRbPath = railsApp.getRoutesFile().getPresentableUrl();
        return new File(routesRbPath).lastModified();
    }


    /**
     * Returns cached output if cache file exists and actual. Cache file is
     * considered to be actual if its modification time is the same as for
     * routes.rb file of current project.
     *
     * @return String that contains cached output or null if no valid cache
     * date is found.
     */
    @Nullable
    private String getCachedOutput() {
        try {
            String fileName = getCacheFileName();
            File f = new File(fileName);

            // Check if cached file still contains actual data. Cached file and routes.rb file should have the same
            // modification time.
            long routesMTime = getRoutesFileMTime();
            if (routesMTime != f.lastModified())
                return null;

            return FileUtil.loadFile(f);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns name of the cache file which contains output data.
     *
     * @return Name of the cache file.
     */
    private String getCacheFileName() {
        VirtualFile projectFile = project.getProjectFile();
        if (projectFile == null || projectFile.getParent() == null)
            return null;

        return projectFile.getParent().getPresentableUrl() +
                File.separator + "railways.cache";
    }
}
package net.bitpot.railways.routesView;

import com.intellij.execution.ExecutionMode;
import com.intellij.execution.ExecutionModes;
import com.intellij.execution.Output;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.bitpot.railways.utils.RailwaysUtils;
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

            // Save indicator to be able to cancel task execution.
            routesUpdateIndicator = indicator;

            output = RailwaysUtils.queryRakeRoutes(getModule());

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
        RailsApp railsApp = RailwaysUtils.getAPI(project).getRailsApp();
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
        // TODO: enable cache after debug is over.
        return null;

        /*try {
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
        }*/
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
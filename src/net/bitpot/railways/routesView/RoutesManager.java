package net.bitpot.railways.routesView;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.parser.RailsRoutesParser;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

import java.io.File;
import java.util.LinkedList;

/**
 * Class is responsible for receiving and storing the list of routes for
 * a Rails module.
 */
@State(
        name = "RailwaysModuleConfiguration",
        storages = {@Storage(value = "$MODULE_FILE$")}
)
public class RoutesManager implements PersistentStateComponent<RoutesManager.State> {

    /**
     * Default state. It is set just after RoutesManager is created.
     */
    public static final int DEFAULT     = 0;


    /**
     * This state is set when RoutesManager is requested routes and awaiting
     * for result.
     */
    public static final int UPDATING    = 1;

    /**
     * The state is set when routes were successfully parsed and RouteList
     * created.
     */
    public static final int UPDATED     = 2;

    /**
     * This state is set when routes couldn't be retrieved by some reasons
     */
    public static final int ERROR       = 3;


    private int myRoutesState = DEFAULT;

    private LinkedList<RoutesManagerListener> listeners = new LinkedList<>();

    // This field is set only when route update task is being executed.
    private ProgressIndicator routesUpdateIndicator = null;

    private ProcessOutput output;

    private RailsRoutesParser parser;
    private RouteList routeList = new RouteList();

    // Rails module
    private Module module;

    private State myModuleSettings = new State();


    public static class State {
        // Name of rake task which retrieves routes.
        public String routesTaskName = "routes";

        // Environment which is used to run rake task.
        public String environment = null;

        // Automatically update routes when routes.rb file is changed.
        public Boolean autoUpdate = false;

        // Check whether route action is found in the project and highlight
        // actions in route list depending on their availability.
        public boolean liveActionHighlighting = true;
    }


    /**
     * Constructor of RoutesManager.
     *
     * @param railsModule Rails module which routes will be served by
     *                    RoutesManager. Specified module should be a Rails
     *                    application module.
     */
    public RoutesManager(Module railsModule) {
        parser = new RailsRoutesParser(railsModule);
        module = railsModule;
    }


    /**
     * Returns current state of RouteManager.
     * @return Current state.
     */
    public int getRoutesState() {
        return myRoutesState;
    }


    /**
     * Returns plugin module settings.
     * @return ModuleSettings instance.
     */
    @NotNull
    @Override
    public State getState() {
        return myModuleSettings;
    }


    /**
     * This method is called when new component state is loaded. A component should expect this method
     * to be called at any moment of its lifecycle. The method can and will be called several times, if
     * config files were externally changed while IDEA running.
     */
    @Override
    public void loadState(@NotNull State state) {
        myModuleSettings = state;
    }


    /**
     * Returns a module which is served by the RoutesManager.
     *
     * @return Linked module.
     */
    public Module getModule() {
        return module;
    }


    public void addListener(RoutesManagerListener listener) {
        listeners.add(listener);
    }


    @SuppressWarnings("unused")
    public boolean removeListener(RoutesManagerListener listener) {
        return listeners.remove(listener);
    }


    /**
     * Returns current RouteList. Returned value is always valid RouteList
     * object, but this object is recreated each time routes are updated.
     * @return Current route list.
     */
    @NotNull
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

        setState(UPDATING);

        // Save all documents to make sure that requestMethods will be collected using actual files.
        TransactionGuard.submitTransaction(ApplicationManager.getApplication(), () -> {
            FileDocumentManager.getInstance().saveAllDocuments();

            // Start background task.
            (new UpdateRoutesTask()).queue();
        });

        return true;
    }


    public int getParserErrorCode() {
        return parser.getErrorCode();
    }


    /**
     * Returns ruby exception stack trace from output of executed rake-task.
     *
     * @return Error stack trace.
     */
    public String getParseErrorStackTrace() {
        return parser.getErrorStacktrace();
    }


    /**
     * Sets new state of RoutesManager and notifies all listeners if the state
     * was changed.
     *
     * @param newState New state of RouteManager.
     */
    private void setState(int newState) {
        if (myRoutesState == newState)
            return;

        myRoutesState = newState;

        // Notify listeners.
        for (RoutesManagerListener l : listeners)
            l.stateChanged(this);
    }


    /**
     * Internal class that is responsible for executing rake task and receiving
     * its output.
     */
    private class UpdateRoutesTask extends Task.Backgroundable {

        public UpdateRoutesTask() {
            super(module.getProject(), "Rake Task", true);

            setCancelText("Cancel Task");
        }


        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.setText("Updating route list for module "  +
                    getModule().getName() + "...");
            indicator.setFraction(0.0);
            indicator.setIndeterminate(false);

            // Save indicator to be able to cancel task execution.
            routesUpdateIndicator = indicator;

            output = RailwaysUtils.queryRakeRoutes(getModule(),
                    myModuleSettings.routesTaskName,
                    myModuleSettings.environment);

            if (output == null)
                setState(UPDATED);

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

            setState(UPDATED);

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

        RailwaysUtils.updateActionsStatus(getModule(), routeList);

        // After routes parsing we can have several situations:
        // 1. parser contains routes and isErrorReported = false. Everything is OK.
        // 2. parser contains no routes and isErrorsReported = true. It means that there was an exception thrown.
        // 3. parser contains routes and isErrorReported = true. In the most cases it's warnings (deprecation etc),
        //    so everything is OK.
        // TODO: possibly, we should report about warnings somehow.
        if (routeList.size() == 0 && parser.isErrorReported()) {
            setState(ERROR);
        } else {
            cacheOutput(stdOut);
            setState(UPDATED);
        }
    }


    /**
     * Saves passed output string to cache file and sets the same modification time as routes.rb has.
     *
     * @param output String that contains stdout of 'rake routes' command.
     */
    private void cacheOutput(String output) {
        try {
            String fileName = getCacheFileName();
            if (fileName == null)
                return;

            // Cache output
            File f = new File(fileName);
            FileUtil.writeToFile(f, output.getBytes(), false);

            // Set cache file modification date/time the same as for routes.rb
            f.setLastModified(getRoutesFilesMTime());
        } catch (Exception e) {
            // Do nothing
        }
    }


    /**
     * Returns modification time of the most recently modified routes file in Rails project.
     *
     * @return Modification time of routes.rb file or 0 if it cannot be retrieved.
     */
    private long getRoutesFilesMTime() {
        RailsApp railsApp = RailsApp.fromModule(module);

        if (railsApp == null)
            return 0;

        RailsApp.RoutesFiles<VirtualFile> files = railsApp.getRoutesFiles();

        return files.allFiles()
                .map((routesFile) -> new File(routesFile.getPresentableUrl()).lastModified())
                .max((o1, o2) -> (int) (o1 - o2))
                .orElse((long) 0);
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
            if (fileName == null)
                return null;

            File f = new File(fileName);

            // Check if cached file still contains actual data. Cached file and routes.rb file should have the same
            // modification time.
            long routesMTime = getRoutesFilesMTime();
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
    @Nullable
    private String getCacheFileName() {
        // TODO: check where cache file is placed in IntelliJ IDEA
        VirtualFile moduleFile = getModule().getModuleFile();
        if (moduleFile == null || moduleFile.getParent() == null)
            return null;

        return moduleFile.getParent().getPresentableUrl() +
                File.separator + "railways.cache";
    }

}
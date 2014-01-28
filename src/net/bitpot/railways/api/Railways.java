package net.bitpot.railways.api;

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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import net.bitpot.railways.RailwaysProjectComp;
import net.bitpot.railways.actions.RailwaysActionsFields;
import net.bitpot.railways.gui.ErrorInfoDlg;
import net.bitpot.railways.parser.RailsRoutesParser;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.gem.GemsRunner;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Class that contains all API methods for Railways plugin.
 */
public class Railways
{
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(Railways.class.getName());

    private Project project;
    private RailsRoutesParser parser;

    // This field is set only when route update task is being executed.
    private ProgressIndicator routesUpdateIndicator = null;

    private ProcessOutput output;

    private LinkedList<RailwaysListener> listeners = new LinkedList<RailwaysListener>();

    private RailwaysActionsFields railwaysActionsFields = new RailwaysActionsFields();




    public Railways(RailwaysProjectComp projectComponent)
    {
        project = projectComponent.getProject();
        parser = new RailsRoutesParser(project);
    }


    /**
     * Returns Railways API methods for specified project.
     * @param project Project
     * @return Railways API object.
     */
    public static Railways getAPI(@NotNull Project project)
    {
        RailwaysProjectComp comp = project.getComponent(RailwaysProjectComp.class);
        return comp.getRailwaysAPI();
    }
    
    
    // API methods

    public void addRailwaysListener(RailwaysListener listener)
    {
        listeners.add(listener);
    }


    @SuppressWarnings("unused")
    public boolean removeRailwaysListener(RailwaysListener listener)
    {
        return listeners.remove(listener);
    }


    /**
     * Returns a list of parsed routes.
     * @return List of parsed routes.
     */
    public RouteList getRoutes()
    {
        return parser.getRoutes();
    }


    /**
     * Shows a dialog with error info.
     */
    public void showErrorInfo()
    {
        ErrorInfoDlg.showError("Error information:", parser.getStacktrace());
    }


    /**
     * Returns true if routes update task is in progress.
     * @return True if routes are being updated.
     */
    public boolean isRoutesUpdating()
    {
        return routesUpdateIndicator != null;
    }


    /**
     * Cancels route update if the task is in progress.
     */
    public void cancelRoutesUpdate()
    {
        if (!isRoutesUpdating())
            return;

        routesUpdateIndicator.cancel();
    }


    /**
     * Updates route list. The method starts task that call 'rake routes' and parses result after complete.
     * After routes are parsed, Routes panel is updated.
     *
     * @return True if update task is started, false if new task is not started because routes update is in progress.
     */
    public boolean updateRouteList()
    {
        if (isRoutesUpdating())
        {
            //log.debug("Update already in progress. Ignoring action.");
            return false;
        }

        for(RailwaysListener l: listeners)
            l.beforeRoutesUpdate();

        // Start background task.
        (new UpdateRoutesTask()).queue();
        return true;
    }


    /**
     * Returns an object with information used internally by plugin actions.
     * @return Object with info
     */
    public RailwaysActionsFields getRailwaysActionsFields()
    {
        return railwaysActionsFields;
    }


    /**
     * Checks whether specified module is a Ruby On Rails module.
     * @param module Module to check
     * @return True if module contains Ruby On Rails project.
     */
    public static boolean isRailsApp(Module module)
    {
        return RailsApp.fromModule(module) != null;
    }


    /**
     * Checks whether current project is a Rails project.
     * Ruby On Rails projects in RubyMine have only one module and if RailsApp can be retrieved from this module,
     * then we have RoR-project.
     * @return True if current project is a RoR-application.
     */
    public boolean isRailsApp()
    {
        return getRailsApp() != null;
    }



    /**
     * Returns RailsApp object which is returned only if opened project is a Rails Application project.
     * Otherwise null will be returned.
     * @return RailsApp object or null.
     */
    public @Nullable RailsApp getRailsApp()
    {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        if (modules.length < 1)
            return null;

        return RailsApp.fromModule(modules[0]);
    }

    public void navigateToRouteAction(@NotNull Route route)
    {
        navigateToRouteAction(route, false);
    }

    /**
     * Opens controller source file and moves cursor to the method that is linked with passed route.
     *
     * @param route Route which controller action should be navigated to.
     */
    public void navigateToRouteAction(@NotNull Route route, boolean requestFocus)
    {
        if (route.getType() == Route.REDIRECT || route.getType() == Route.MOUNTED)
            return;

        RailsApp app = getRailsApp();
        if ((app == null) || route.getController().isEmpty())
            return;

        RailsController controller = app.findController(route.getController());
        if (controller == null)
            return;

        if (!route.getAction().isEmpty())
        {
            RMethod method = controller.getAction(route.getAction());
            if (method != null)
                method.navigate(requestFocus);
        }
    }


    /**
     * Internally used method that runs rake task and gets its output. This method should be called
     * from backgroundable task.
     * @param module Module for which rake task should be run.
     * @return Output of 'rake routes'.
     */
    private static @Nullable ProcessOutput queryRakeRoutes(Module module)
    {
        String errorTitle = "Error in rake command.";

        // Here several options are possible:
        // 1) project.getLocation() - it is present in all versions, but it's already deprecated.
        // 2) project.getBasePath() - is absent in RubyMine 4.0.1.
        // 3) project.getPresentableUrl() - works fine.
        // The last option is used.
        String moduleContentRoot = module.getProject().getPresentableUrl();


        ModuleRootManager mManager = ModuleRootManager.getInstance(module);
        Sdk sdk = mManager.getSdk();
        if (sdk == null)
        {
            Notifications.Bus.notify(new Notification("Railways", "Railways Error",
                    "Cannot update routes list, because SDK is not specified for the current project", NotificationType.ERROR)
                    , module.getProject());
            return null;
        }

        try
        {
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
            }
            catch (NoSuchMethodException e) { /* Do nothing */ }

            if (method == null)
            {
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
                processOutput = convertToProcessOutput((Output)output);
            else
                processOutput = (ProcessOutput)output;


            return processOutput;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Converts Output object to ProcessOutput.
     * @param output Output object to convert
     * @return ProcessOutput object
     */
    private static ProcessOutput convertToProcessOutput(@NotNull Output output)
    {
        ProcessOutput res = new ProcessOutput();

        // Unfortunately, methods setExitCode, appendStdout and
        // appendStderr are declared as private prior to 122.633 build.
        // So we will try to use reflection to call them.

        Method method;
        try
        {
            method = ProcessOutput.class.getDeclaredMethod("setExitCode", int.class);
            method.setAccessible(true);
            method.invoke(res, output.getExitCode());

            method = ProcessOutput.class.getDeclaredMethod("appendStdout", String.class);
            method.setAccessible(true);
            method.invoke(res, output.getStdout());

            method = ProcessOutput.class.getDeclaredMethod("appendStderr", String.class);
            method.setAccessible(true);
            method.invoke(res, output.getStderr());

        } catch (Exception e)
        {
            return null;
        }

        return res;
    }


    /**
     * Initializes route list. Does nothing if cache is disabled.
     * When cache is enabled, tries to get routes from cache and if not found tries to update routes.
     */
    public void initRouteList()
    {
        String cachedOutput = getCachedOutput();
        if (cachedOutput != null)
        {
            parseRakeRoutesOutput(cachedOutput, null);
        }
        else
            updateRouteList();
    }


    /**
     * Returns name of the cache file which contains output data.
     *
     * @return Name of the cache file.
     */
    private String getCacheFileName()
    {
        try
        {
            return project.getProjectFile().getParent().getPresentableUrl() +
                    File.separator + "railways.cache";
        }
        catch (NullPointerException e)
        {
            return null;
        }

    }


    /**
     * Returns modification time of routes.rb file for Rails project.
     *
     * @return Modification time of routes.rb file or 0 if it cannot be retrieved.
     */
    private long getRoutesFileMTime()
    {
        try
        {
            File routesFile = new File(getRailsApp().getRoutesFile().getPresentableUrl());
            return routesFile.lastModified();
        }
        catch(NullPointerException e)
        {
            return 0;
        }
    }


    /**
     * Saves passed output string to cache file and sets the same modification time as routes.rb has.
     *
     * @param output String that contains stdout of 'rake routes' command.
     */
    private void cacheOutput(String output)
    {
        try
        {
            long routesMTime = getRoutesFileMTime();

            File f = new File(getCacheFileName());
            FileUtil.writeToFile(f, output.getBytes(), false);
            f.setLastModified(routesMTime);
        }
        catch (Exception e)
        {
            // Do nothing
            //e.printStackTrace();
        }
    }


    /**
     * Returns cached output if cache file exists and actual. Cache file is considered to be actual if its
     * modification time is the same as for routes.rb file of current project.
     *
     * @return String that contains cached output or null if no valid cache date is found.
     */
    private @Nullable String getCachedOutput()
    {
        try
        {
            String fileName = getCacheFileName();
            File f = new File(fileName);

            // Check if cached file still contains actual data. Cached file and routes.rb file should have the same
            // modification time.
            long routesMTime = getRoutesFileMTime();
            if (routesMTime != f.lastModified())
                return null;

            return FileUtil.loadFile(f);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    /**
     * Parses 'rake routes' output and notifies all listeners that route list was updated.
     * @param stdOut Rake routes result.
     * @param stdErr Rake routes stderr output. Can be null.
     */
    private void parseRakeRoutesOutput(String stdOut, @Nullable String stdErr)
    {
        parser.parse(stdOut, stdErr);

        // After routes parsing we can have several situations:
        // 1. parser contains routes and isErrorReported = false. Everything is OK.
        // 2. parser contains no routes and isErrorsReported = true. It means that there was an exception thrown.
        // 3. parser contains routes and isErrorReported = true. In the most cases it's warnings (deprecation etc),
        //    so everything is OK.
        // TODO: possibly, we should report about warnings somehow.
        if (parser.getRoutes().size() == 0 && parser.isErrorReported())
        {
            for(RailwaysListener l: listeners)
                l.routesUpdateError();
        }
        else
        {
            cacheOutput(stdOut);

            for(RailwaysListener l: listeners)
                l.routesUpdated();
        }
    }



    /**
     * Internal class that is responsible for updating routes list.
     */
    private class UpdateRoutesTask extends Task.Backgroundable
    {

        public UpdateRoutesTask()
        {
            super(project, "Rake task", true);

            setCancelText("Cancel task");
        }


        @Override
        public void run(@NotNull ProgressIndicator indicator)
        {
            indicator.setText("Updating routes list...");
            indicator.setFraction(0.0);

            ModuleManager modMgr = ModuleManager.getInstance(getProject());
            final Module[] modules = modMgr.getModules();

            // Save indicator to be able to cancel task execution.
            routesUpdateIndicator = indicator;

            output = queryRakeRoutes(modules[0]);

            if (output == null)
                for(RailwaysListener l: listeners)
                    l.routesUpdated();


            indicator.setFraction(1.0);
        }

        @Override
        public void onSuccess()
        {
            routesUpdateIndicator = null;

            if ((output == null) || (!myProject.isOpen()) || myProject.isDisposed())
                return;

            parseRakeRoutesOutput(output.getStdout(), output.getStderr());
        }

        @Override
        public void onCancel()
        {
            routesUpdateIndicator = null;

            for(RailwaysListener l: listeners)
                l.routesUpdated();

            super.onCancel();
        }
    }


    /*

    // Test method.
    public void fillTestRoutes()
    {
        parser.parse("photo_album_photos POST   /photo_albums/:photo_album_id/photos(.:format)           {:action=>\"create\", :controller=>\"photos\"}\n" +
                "             new_photo_album GET    /photo_albums/new(.:format)                              {:action=>\"new\", :controller=>\"photo_albums\"}\n" +
                "            edit_photo_album GET    /photo_albums/:id/edit(.:format)                         {:action=>\"edit\", :controller=>\"photo_albums\"}\n" +
                "                 photo_album GET    /photo_albums/:id(.:format)                              {:action=>\"show\", :controller=>\"photo_albums\"}\n" +
                "                             PUT    /photo_albums/:id(.:format)                              {:action=>\"update\", :controller=>\"photo_albums\"}\n" +
                "                             DELETE /photo_albums/:id(.:format)                              {:action=>\"destroy\", :controller=>\"photo_albums\"}\n" +
                "    test_server        /test                     {:to=>TestServer}\n" +
                "    test_server2        /test2                     Test::Server\n"
        );

        for(RailwaysListener l: listeners)
            l.routesUpdated();
    }

    */
}

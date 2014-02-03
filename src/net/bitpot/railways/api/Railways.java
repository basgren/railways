package net.bitpot.railways.api;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.RailwaysProjectComp;
import net.bitpot.railways.actions.RailwaysActionsFields;
import net.bitpot.railways.gui.ErrorInfoDlg;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;

/**
 * Class that contains all API methods for Railways plugin.
 */
public class Railways
{
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(Railways.class.getName());

    private Project project;

    private RailwaysActionsFields railwaysActionsFields = new RailwaysActionsFields();

    private RoutesManager routesManager;


    public Railways(RailwaysProjectComp projectComponent)
    {
        project = projectComponent.getProject();
        routesManager = new RoutesManager(project, getRailsApp());
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

    public RoutesManager getRoutesManager()
    {
        return routesManager;
    }

    
    /**
     * Returns a list of parsed routes.
     * @return List of parsed routes.
     */
    public RouteList getRoutes()
    {
        return getRoutesManager().getRouteList();
    }


    /**
     * Shows a dialog with error info.
     */
    public void showErrorInfo()
    {
        ErrorInfoDlg.showError("Error information:",
                getRoutesManager().getParseErrorStacktrace());
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
     * Returns RailsApp object which is returned only if opened project is a
     * Rails Application project. Otherwise null will be returned.
     *
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
     * Opens controller source file and moves cursor to the method that is
     * linked with passed route.
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
}
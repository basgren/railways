package net.bitpot.railways.api;

import com.intellij.ProjectTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleAdapter;
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

import java.util.ArrayList;

/**
 * Class that contains all API methods for Railways plugin.
 */
public class Railways implements Disposable
{
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(Railways.class.getName());

    private Project myProject;

    // Contains list of Rails modules.
    private ArrayList<Module> moduleList = new ArrayList<>();

    private RailwaysActionsFields railwaysActionsFields = new RailwaysActionsFields();

    private RoutesManager routesManager;


    public Railways(RailwaysProjectComp projectComponent)
    {
        myProject = projectComponent.getProject();

        // At this moment there's no modules added to the project. They are
        // added somewhere later by OpenAPI.
        myProject.getMessageBus().connect(this)
                .subscribe(ProjectTopics.MODULES, new ProjectModulesListener());

        routesManager = new RoutesManager(myProject, getRailsApp());
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


    public boolean hasRailsModules() {
        return moduleList.size() > 0;
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
    public @Nullable RailsApp getRailsApp()
    {
        // TODO: rework this!!!
        if (moduleList.size() > 0)
            return RailsApp.fromModule(moduleList.get(0));

        return null;
    }


    private class ProjectModulesListener extends ModuleAdapter
    {
        @Override
        public void moduleAdded(Project project, Module module)
        {
            // Ignore event not addressed to this project.
            if (myProject != project)
                return;

            boolean isRails = RailsApp.fromModule(module) != null;

            if (isRails)
            {
                moduleList.add(module);
                log.info("Rails module added: " + module.toString());
            }
        }

        @Override
        public void moduleRemoved(Project project, Module module)
        {
            // Ignore event not addressed to this project.
            if (myProject != project)
                return;

            moduleList.remove(module);
            log.info("Rails module removed: " + module.toString());
        }
    }

}
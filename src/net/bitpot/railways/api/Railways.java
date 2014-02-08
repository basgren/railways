package net.bitpot.railways.api;

import com.intellij.ProjectTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
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
public class Railways implements Disposable {
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(Railways.class.getName());

    private Project myProject;

    // Contains list of Rails modules.
    private ArrayList<RoutesManager> routesManagerList = new ArrayList<>();
    private int activeRoutesManager = -1;

    private RailwaysActionsFields railwaysActionsFields = new RailwaysActionsFields();


    public Railways(RailwaysProjectComp projectComponent) {
        myProject = projectComponent.getProject();

        // At this moment there's no modules added to the project. They are
        // added somewhere later by OpenAPI.
        myProject.getMessageBus().connect(this)
                .subscribe(ProjectTopics.MODULES, new ProjectModulesListener());
    }


    /**
     * Returns Railways API methods for specified project.
     *
     * @param project Project
     * @return Railways API object.
     */
    public static Railways getAPI(@NotNull Project project) {
        RailwaysProjectComp comp = project.getComponent(RailwaysProjectComp.class);
        return comp.getRailwaysAPI();
    }


    public boolean hasRailsModules() {
        return routesManagerList.size() > 0;
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
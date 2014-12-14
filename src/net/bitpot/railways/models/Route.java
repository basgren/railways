package net.bitpot.railways.models;


import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.parser.route.RouteActionParser;
import net.bitpot.railways.parser.route.RoutePathParser;
import net.bitpot.railways.parser.route.TextChunk;
import net.bitpot.railways.utils.RailwaysPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;

import javax.swing.*;
import java.util.List;

/**
 * Route class stores all information about parsed route.
 */
public class Route implements NavigationItem {
    // Route types:
    public final static int DEFAULT = 0;  // General route
    public final static int REDIRECT = 1; // Redirect
    public final static int MOUNTED = 2; // Mounted rack application

    private Module module = null;


    private RequestMethod requestMethod = RequestMethod.ANY;
    private String path = "";
    private String routeName = "";
    private String controller = "";
    private String action = "";

    @Nullable
    private RailsEngine myParentEngine = null;


    @NotNull
    private RailsActionInfo actionInfo = new RailsActionInfo();

    // Cached path and action text chunks.
    private List<TextChunk> pathChunks = null;
    private List<TextChunk> actionChunks = null;
    private RailsEngine parentEngine;


    public Route(@Nullable Module module, RequestMethod requestMethod, String path,
                 String name) {
        this.module = module;

        this.requestMethod = requestMethod;
        this.path = path;
        setController(controller);
        setRouteName(name);
        myParentEngine = parentEngine;
    }


    /**
     * Returns module the route belongs to.
     * @return Route module
     */
    public Module getModule() {
        return module;
    }


    public RequestMethod getRequestMethod() {
        return requestMethod;
    }


    /**
     * Returns route type. There are 3 types of routes:
     *   1. DEFAULT - general route to some controller method
     *   2. MOUNTED - route mounted to some Rack application
     *   3. REDIRECT - route is a redirect to different route
     *
     * @return Route type
     */
    public int getType() {
        if (!controller.isEmpty() && action.isEmpty())
            return MOUNTED;

        if (controller.equals(":controller") &&
                action != null && action.equals(":action"))
            return REDIRECT;

        return DEFAULT;
    }


    /**
     * Returns fully-qualified controller method name as it's written in ruby,
     * ex. 'UsersController#index'.
     * If route is mounted, returns only the name of the controller.
     *
     * @return Fully qualified method name.
     */
    public String getControllerMethodName() {
        int routeType = getType();

        if (controller.isEmpty() || routeType == REDIRECT)
            return "";

        if (routeType == MOUNTED)
            return controller;

        String ctrlName;
        RClass ctrlClass = getActionInfo().getPsiClass();
        if (ctrlClass != null)
            ctrlName = ctrlClass.getQualifiedName();
        else
            ctrlName = RailwaysPsiUtils.getControllerClassNameByShortName(controller);


        return String.format("%s#%s", ctrlName, action);
    }


    /**
     * Returns displayable text for route action. If route leads to mounted
     * Rack application, it will return base class.
     *
     * @return Displayable text for route action, ex. "users#create"
     */
    public String getActionText() {
        switch (getType()) {
            case MOUNTED:
                return controller;
            case REDIRECT:
                return "[redirect]";
            default:
                return String.format("%s#%s", controller, action);
        }
    }


    /**
     * Returns rack icon if the route is for mounted rack-application, otherwise returns icon for
     * corresponding request method.
     *
     * @return Route icon.
     */
    public Icon getIcon() {
        if (getType() == MOUNTED)
            return RailwaysIcons.RACK_APPLICATION;

        return getRequestMethod().getIcon();
    }


    /**
     * Returns displayable name for navigation list.
     *
     * @return Display name of current route.
     */
    @Nullable
    @Override
    public String getName() {
        return path;
    }


    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        final Route route = this;

        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return path;
            }


            @Nullable
            @Override
            public String getLocationString() {
                return getActionText();
            }


            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return route.getIcon();
            }
        };
    }


    @Override
    public void navigate(boolean requestFocus) {
        int routeType = getType();

        if (routeType == Route.REDIRECT || routeType == Route.MOUNTED)
            return;

        actionInfo.update(module, controller, action);

        if (actionInfo.getPsiMethod() != null)
            actionInfo.getPsiMethod().navigate(requestFocus);
        else if (actionInfo.getPsiClass() != null)
            actionInfo.getPsiClass().navigate(requestFocus);
    }




    @Override
    public boolean canNavigate() {
        return actionInfo.getPsiMethod() != null ||
                actionInfo.getPsiClass() != null;
    }


    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }


    public String getPath() {
        return path;
    }


    public List<TextChunk> getPathChunks() {
        if (pathChunks == null)
            pathChunks = RoutePathParser.parse(getPath());

        return pathChunks;
    }


    public List<TextChunk> getActionChunks() {
        if (actionChunks == null)
            actionChunks = RouteActionParser.parse(getActionText());

        return actionChunks;
    }


    public String getRouteName() {
        if (getParentEngine() != null)
            return getParentEngine().getNamespace() + "." + routeName;

        return routeName;
    }


    public void setRouteName(String name) {
        this.routeName = name;
    }


    public void setController(String value) {
        controller = value;
    }


    public String getController() {
        return controller;
    }


    public String getAction() {
        return action;
    }


    /**
     * Checks route action status and sets isActionDeclarationFound flag.
     *
     * @param app Rails application which will be checked for controller action.
     */
    public void updateActionStatus(RailsApp app) {
        int routeType = getType();
        if (routeType == Route.REDIRECT || routeType == Route.MOUNTED)
            return;

        actionInfo.update(app, controller, action);
    }


    @Nullable
    public RailsEngine getParentEngine() {
        return myParentEngine;
    }


    @NotNull
    public RailsActionInfo getActionInfo() {
        return actionInfo;
    }


    public void setParentEngine(RailsEngine parentEngine) {
        this.parentEngine = parentEngine;
    }
}
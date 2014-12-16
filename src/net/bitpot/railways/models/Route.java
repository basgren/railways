package net.bitpot.railways.models;


import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.parser.route.RouteActionParser;
import net.bitpot.railways.parser.route.RoutePathParser;
import net.bitpot.railways.parser.route.TextChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

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


    // TODO: move action info to SimpleRoute
    @NotNull
    private RailsActionInfo actionInfo = new RailsActionInfo();

    // Cached path and action text chunks.
    private List<TextChunk> pathChunks = null;
    private List<TextChunk> actionChunks = null;


    public Route(@Nullable Module module, RequestMethod requestMethod, String path,
                 String name) {
        this.module = module;

        this.requestMethod = requestMethod;
        this.path = path;
        setRouteName(name);
    }


    /**
     * Returns module the route belongs to.
     * @return Route module
     */
    public Module getModule() {
        return module;
    }


    @NotNull
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
     * Returns displayable text for route action in short format. Short format
     * is used in routes table.
     *
     * @return Displayable text for route action, ex. "users#create"
     */
    public String getShortActionTitle() {
        return getActionTitle();
    }


    /**
     * Returns displayable text for route action.
     *
     * @return Displayable text for route action, ex. "UsersController#create"
     */
    public String getActionTitle() {
        return "";
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
                return getShortActionTitle();
            }


            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return route.getRequestMethod().getIcon();
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
            actionChunks = RouteActionParser.parse(getShortActionTitle());

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

    public void setParentEngine(RailsEngine parentEngine) {
        myParentEngine = parentEngine;
    }


    @NotNull
    public RailsActionInfo getActionInfo() {
        return actionInfo;
    }


    public Icon getActionIcon() {
        return RailwaysIcons.UNKNOWN;
    }
}
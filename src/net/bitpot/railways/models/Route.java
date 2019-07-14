package net.bitpot.railways.models;


import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.parser.route.RouteActionParser;
import net.bitpot.railways.parser.route.RoutePathParser;
import net.bitpot.railways.parser.route.TextChunk;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;

import javax.swing.*;
import java.util.List;

/**
 * Route class stores all information about parsed route.
 */
public class Route implements NavigationItem {

    private Module module;

    private RequestMethod requestMethod;
    private String path;
    private String routeName;

    @Nullable
    private RailsEngine myParentEngine = null;

    // Cached path and action text chunks.
    private List<TextChunk> pathChunks = null;
    private List<TextChunk> actionChunks = null;


    public Route(@Nullable Module module, RequestMethod requestMethod, String path,
                 String name) {
        this.module = module;

        this.requestMethod = requestMethod;
        this.path = path;
        this.routeName = name;
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
     * Returns displayable text for route action in short format. Short format
     * is used in routes table.
     *
     * @return Displayable text for route action, ex. "users#create"
     */
    public String getActionTitle() {
        return getQualifiedActionTitle();
    }


    /**
     * Returns qualified name for route action.
     *
     * @return Displayable text for route action, ex. "UsersController#create"
     */
    public String getQualifiedActionTitle() {
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
                return getActionTitle();
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
        // This method should be overridden in subclasses that support navigation.
    }


    @Override
    public boolean canNavigate() {
        return false;
    }


    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }


    public String getPath() {
        return path;
    }

    public String getPathWithMethod() {
        String path = RailwaysUtils.stripRequestFormat(getPath());

        if (getRequestMethod() == RequestMethods.ANY)
            return path;

        return String.format("%s %s", getRequestMethod().getName(), path);
    }


    public List<TextChunk> getPathChunks() {
        if (pathChunks == null)
            pathChunks = RoutePathParser.getInstance().parse(getPath());

        return pathChunks;
    }


    public List<TextChunk> getActionChunks() {
        if (actionChunks == null)
            actionChunks = RouteActionParser.getInstance().parse(getActionTitle());

        return actionChunks;
    }


    public String getRouteName() {
        if (getParentEngine() != null)
            return getParentEngine().getNamespace() + "." + routeName;

        return routeName;
    }


    /**
     * Checks route action status and sets isActionDeclarationFound flag.
     *
     * @param app Rails application which will be checked for controller action.
     */
    public void updateActionStatus(RailsApp app) {
        // Should be overridden in subclasses if an update is required.
    }


    @Nullable
    public RailsEngine getParentEngine() {
        return myParentEngine;
    }

    public void setParentEngine(RailsEngine parentEngine) {
        myParentEngine = parentEngine;
    }


    public Icon getActionIcon() {
        return RailwaysIcons.NODE_UNKNOWN;
    }
}
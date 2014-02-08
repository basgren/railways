package net.bitpot.railways.models;


import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import net.bitpot.railways.api.Railways;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.routes.RequestType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Route implements NavigationItem {
    // Route types
    public final static int DEFAULT = 0;
    public final static int REDIRECT = 1; // Route redirects, so it's hard to determine where it will be routed
    // as rake routes does not show this info.
    public final static int MOUNTED = 2; // Mounted rack application.


    private Project project = null;

    private int routeType = DEFAULT;
    private RequestType requestType = RequestType.ANY;
    private HashMap<String, String> requirements = null;

    private String path = "";
    private String routeName = "";
    private String controller = "";
    private String action = "";


    public Route() {
        this(null);
    }


    public Route(@Nullable Project project) {
        this.project = project;
    }


    /**
     * Sets request type for the route.
     *
     * @param value Request type.
     */
    public void setRequestType(@NotNull RequestType value) {
        requestType = value;
    }


    /**
     * Sets request type by its string representation. Available values: "get", "post", "put', "delete".
     * All other values are treated as any request type.
     *
     * @param verb String name of request type.
     */
    public void setRequestType(String verb) {
        verb = verb.toLowerCase();

        if (verb.equals("get")) requestType = RequestType.GET;
        else if (verb.equals("post")) requestType = RequestType.POST;
        else if (verb.equals("put")) requestType = RequestType.PUT;
        else if (verb.equals("patch")) requestType = RequestType.PATCH;
        else if (verb.equals("delete")) requestType = RequestType.DELETE;
        else
            requestType = RequestType.ANY;
    }


    public RequestType getRequestType() {
        return requestType;
    }


    public boolean isValid() {
        return !path.isEmpty() && !controller.isEmpty();
    }


    public void setRoute(String name, String path, @NotNull RequestType type, String controller, String action) {
        this.routeName = name;
        this.path = path;
        this.requestType = type;

        this.controller = controller;
        this.action = action;
    }


    public int getType() {
        return routeType;
    }


    /**
     * Tries to determine route type and set inner field. Should be called after all fields of the route is set.
     */
    public void updateType() {
        if (!controller.isEmpty() && action.isEmpty()) {
            routeType = MOUNTED;
            return;
        }

        if (controller != null && controller.equals(":controller") && action != null && action.equals(":action")) {
            routeType = REDIRECT;
            return;
        }

        routeType = DEFAULT;
    }


    public Map<String, String> getRequirements() {
        if (requirements == null)
            requirements = new HashMap<String, String>();

        return requirements;
    }


    public boolean hasRequirements() {
        return (requirements != null) && (requirements.size() > 0);
    }


    /**
     * Returns fully-qualified controller method name as it's writte in ruby, ex. 'UsersController#index'.
     * If route is mounted, returns only the name of the controller.
     *
     * @return Fully qualified method name.
     */
    public String getControllerMethodName() {
        if (controller.isEmpty() || routeType == REDIRECT)
            return "";

        if (routeType == MOUNTED)
            return controller;

        return String.format("%sController#%s", toCamelCase(controller, false), action);
    }


    /**
     * Returns displayable text for route action. If route leads to mounted Rack application, it will return base class.
     *
     * @return Displayable text for route action, ex. users#create
     */
    public String getActionText() {
        switch (routeType) {
            case MOUNTED:
                return controller;
            case REDIRECT:
                return "[redirect]";
            default:
                return String.format("%s#%s", controller, action);
        }
    }


    /**
     * Converts string to camel case.
     *
     * @param value              String to convert
     * @param startWithLowerCase Set to true if first letter should remain lower-case.
     * @return String in CamelCase
     */
    private static String toCamelCase(String value, boolean startWithLowerCase) {
        String[] strings = value.toLowerCase().split("_");
        for (int i = startWithLowerCase ? 1 : 0; i < strings.length; i++) {
            strings[i] = StringUtil.capitalize(strings[i]);
        }

        return StringUtil.join(strings);
    }


    /**
     * Returns rack icon if the route is for mounted rack-application, otherwise returns icon for
     * corresponding request type.
     *
     * @return Route icon.
     */
    public Icon getIcon() {
        if (routeType == MOUNTED)
            return RailwaysIcons.RACK_APPLICATION;

        return getRequestType().getIcon();
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
        // Do nothing now
        if (project != null)
            Railways.getAPI(project).navigateToRouteAction(this, requestFocus);
    }


    @Override
    public boolean canNavigate() {
        return true;
    }


    @Override
    public boolean canNavigateToSource() {
        return true;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getRouteName() {
        return routeName;
    }


    public void setRouteName(String name) {
        this.routeName = name;
    }


    public void setController(String value) {
        controller = value;
    }


    public void setAction(String value) {
        action = value;
    }


    public String getController() {
        return controller;
    }


    public String getAction() {
        return action;
    }
}
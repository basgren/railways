package net.bitpot.railways.models;


import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.routes.RequestMethod;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.Visibility;

import javax.swing.*;

/**
 *
 */
public class Route implements NavigationItem {
    // Route types
    public final static int DEFAULT = 0;
    public final static int REDIRECT = 1; // Route redirects, so it's hard to determine where it will be routed
    // as rake routes does not show this info.
    public final static int MOUNTED = 2; // Mounted rack application.

    // TODO: move module reference to parent list.
    private Module module = null;


    private RequestMethod requestMethod = RequestMethod.ANY;
    private String path = "";
    private String routeName = "";
    private String controller = "";
    private String action = "";

    // By default let's assume that action exists.
    private boolean isActionDeclarationFound = true;

    @Nullable
    private Visibility actionVisibility = null;

    @Nullable
    private RailsEngine myParentEngine = null;


    public Route() {
        this(null);
    }


    public Route(@Nullable Module module) {
        this.module = module;
    }

    public Route(@Nullable Module module, RequestMethod requestMethod, String path,
                 String controller, String action, String name) {
        this(module);

        setRequestMethod(requestMethod);
        setPath(path);
        setController(controller);
        setAction(action);
        setRouteName(name);
    }


    /**
     * Returns module the route belongs to.
     * @return Route module
     */
    public Module getModule() {
        return module;
    }


    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }


    public RequestMethod getRequestMethod() {
        return requestMethod;
    }


    public boolean isValid() {
        return !path.isEmpty() && !controller.isEmpty();
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

        if (controller != null && controller.equals(":controller") &&
                action != null && action.equals(":action"))
            return REDIRECT;

        return DEFAULT;
    }


    /**
     * Returns fully-qualified controller method name as it's writte in ruby,
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

        // Process namespaces.
        String[] strings = controller.split("/");
        for(int i = 0; i < strings.length; i++) {
            strings[i] = toCamelCase(strings[i]);
        }

        String ctrlName = StringUtil.join(strings, "::");

        return String.format("%sController#%s", ctrlName, action);
    }


    /**
     * Returns displayable text for route action. If route leads to mounted Rack application, it will return base class.
     *
     * @return Displayable text for route action, ex. users#create
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
     * Converts string to camel case.
     *
     * @param value String to convert
     * @return String in CamelCase
     */
    private static String toCamelCase(String value) {
        String[] strings = value.toLowerCase().split("_");
        for (int i = 0; i < strings.length; i++) {
            strings[i] = StringUtil.capitalize(strings[i]);
        }

        return StringUtil.join(strings);
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

        RailsApp app = RailsApp.fromModule(module);
        if ((app == null) || controller.isEmpty())
            return;

        RailsController ctrl = app.findController(controller);
        if (ctrl == null)
            return;

        if (!action.isEmpty()) {
            RMethod method = ctrl.getAction(action);
            if (method != null)
                method.navigate(requestFocus);
            else
                ctrl.getRClass().navigate(requestFocus);
        }
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


    public void setAction(String value) {
        action = value;
    }


    public String getController() {
        return controller;
    }


    public String getAction() {
        return action;
    }


    public boolean isActionDeclarationFound() {
        return isActionDeclarationFound;
    }


    /**
     * Checks route action status and sets isActionDeclarationFound flag.
     *
     * @param app Rails application which will be checked for controller action.
     */
    public void updateActionStatus(RailsApp app) {
        isActionDeclarationFound = true;

        int routeType = getType();
        if (routeType == Route.REDIRECT || routeType == Route.MOUNTED) {
            isActionDeclarationFound = false;
            return;
        }

        RailsController appCtrl = app.findController(getController());
        if (appCtrl != null) {
            RMethod method = appCtrl.getAction(action);

            isActionDeclarationFound = (method != null);
            actionVisibility = (method != null) ? method.getVisibility() : null;
        } else {
            isActionDeclarationFound = false;
            actionVisibility = null;
        }
    }


    @Nullable
    public Visibility getActionVisibility() {
        return actionVisibility;
    }


    public void setParentEngine(RailsEngine engine) {
        myParentEngine = engine;
    }

    @Nullable
    public RailsEngine getParentEngine() {
        return myParentEngine;
    }
}
package net.bitpot.railways.models;


import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import icons.RubyIcons;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.routes.RequestMethod;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.rails.nameConventions.ControllersConventions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.Visibility;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RSuperClass;

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
    private boolean isControllerDeclarationFound = true;

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

        if (controller.equals(":controller") &&
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

        String ctrlName = RailwaysUtils.getRubyClassName(controller);

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
        if (ctrl == null) {
            RailwaysUtils.testIndexSearch(controller, module.getProject());
            return;
        }


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
        return isControllerDeclarationFound || isActionDeclarationFound();
    }


    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
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
        isActionDeclarationFound = false;
        isControllerDeclarationFound = false;

        int routeType = getType();
        if (routeType == Route.REDIRECT || routeType == Route.MOUNTED)
            return;

        RailsController appCtrl = app.findController(getController());
        if (appCtrl != null) {
            isControllerDeclarationFound = true;
            RMethod method = findMethod(app, appCtrl, action);

            isActionDeclarationFound = (method != null);
            actionVisibility = (method != null) ? method.getVisibility() : null;
        } else {
            actionVisibility = null;
        }
    }


    // TODO: refactor and unite with action search routing in navigation method.
    // TODO: display actual controller name where action is defined in RouteInfo.
    // TODO: think about using ControllersConventions class for other places where string conversion is required.
    @Nullable
    private RMethod findMethod(RailsApp app, RailsController ctrl, String methodName) {
        RMethod method;

        while ((method = ctrl.getAction(methodName)) == null) {
            // Try to look in parents
            RSuperClass parentClass = ctrl.getRClass().getPsiSuperClass();
            if ((parentClass == null) || (parentClass.getName() == null))
                return null;

            // ControllerConventions is a ruby-plugin class that helps with
            // Rails string conversions.
            String ctrlName = ControllersConventions
                    .getControllerNameByClassName(parentClass.getName());
            if (ctrlName == null)
                return null;

            ctrl = app.findController(ctrlName);
            if (ctrl == null)
                return null;
        }

        return method;
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


    public Icon getActionIcon() {
        Visibility vis = getActionVisibility();
        if (vis != null) {
            switch (vis) {
                case PRIVATE:
                case PROTECTED:
                    return AllIcons.Nodes.Method;

                case PUBLIC:
                    return RubyIcons.Rails.ProjectView.Action_method;
            }
        }

        return AllIcons.General.Error;
    }


    public boolean isControllerDeclarationFound() {
        return isControllerDeclarationFound;
    }
}
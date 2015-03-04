package net.bitpot.railways.models.routes;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.RailsActionInfo;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.utils.RailwaysPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 14.12.2014.
 */
public class SimpleRoute extends Route {

    private String controllerName;
    private String actionName;

    @NotNull
    private RailsActionInfo actionInfo = new RailsActionInfo();


    public SimpleRoute(Module myModule, RequestMethod requestMethod,
                       String routePath, String routeName,
                       String controllerName, String actionName) {
        super(myModule, requestMethod, routePath, routeName);

        this.controllerName = controllerName;
        this.actionName = actionName;
    }


    @Override
    public String getActionTitle() {
        if (!controllerName.isEmpty())
            return String.format("%s#%s", controllerName, actionName);

        return actionName;
    }


    @Override
    public String getQualifiedActionTitle() {
        // Return unqualified action title in case controller is specified as
        // parameter (ex. :controller#:action)
        if (controllerName.contains(":"))
            return getActionTitle();

        String ctrlClassName;

        RClass ctrlClass = getActionInfo().getPsiClass();
        if (ctrlClass != null)
            ctrlClassName = ctrlClass.getQualifiedName();
        else
            ctrlClassName = RailwaysPsiUtils.getControllerClassNameByShortName(controllerName);

        return String.format("%s#%s", ctrlClassName, actionName);
    }


    @Override
    public Icon getActionIcon() {
        RailsActionInfo action = getActionInfo();

        if (action.getPsiMethod() != null)
            return action.getIcon();
        else if (action.getPsiClass() != null)
            return RailwaysIcons.NODE_CONTROLLER;

        return RailwaysIcons.NODE_UNKNOWN;
    }


    @NotNull
    public RailsActionInfo getActionInfo() {
        return actionInfo;
    }


    @Override
    public void navigate(boolean requestFocus) {
        getActionInfo().update(getModule(), controllerName, actionName);

        if (getActionInfo().getPsiMethod() != null)
            getActionInfo().getPsiMethod().navigate(requestFocus);

        else if (getActionInfo().getPsiClass() != null)
            getActionInfo().getPsiClass().navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return getActionInfo().getPsiMethod() != null ||
                getActionInfo().getPsiClass() != null;
    }


    /**
     * Checks route action status and sets isActionDeclarationFound flag.
     *
     * @param app Rails application which will be checked for controller action.
     */
    @Override
    public void updateActionStatus(RailsApp app) {
        getActionInfo().update(app, controllerName, actionName);
    }
}

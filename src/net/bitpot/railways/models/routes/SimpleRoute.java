package net.bitpot.railways.models.routes;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.utils.RailwaysPsiUtils;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;

/**
 * @author Basil Gren
 *         on 14.12.2014.
 */
public class SimpleRoute extends Route {

    private String controllerName;
    private String actionName;

    public SimpleRoute(Module myModule, RequestMethod requestMethod,
                       String routePath, String routeName,
                       String controllerName, String actionName) {
        super(myModule, requestMethod, routePath, routeName);

        this.controllerName = controllerName;
        this.actionName = actionName;
    }


    @Override
    public String getShortActionTitle() {
        if (!controllerName.isEmpty())
            return String.format("%s#%s", controllerName, actionName);

        return actionName;
    }


    @Override
    public String getActionTitle() {
        String ctrlClassName;

        RClass ctrlClass = getActionInfo().getPsiClass();
        if (ctrlClass != null)
            ctrlClassName = ctrlClass.getQualifiedName();
        else
            ctrlClassName = RailwaysPsiUtils.getControllerClassNameByShortName(controllerName);


        return String.format("%s#%s", ctrlClassName, actionName);
    }
}

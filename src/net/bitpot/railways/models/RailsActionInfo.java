package net.bitpot.railways.models;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.rails.nameConventions.ControllersConventions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.Visibility;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RSuperClass;

import javax.swing.*;

/**
 * Contains information about controller action.
 *
 * @author Basil Gren
 *         on 23.11.2014.
 */
public class RailsActionInfo {

    //private String controllerName = "";
    //private String actionName = "";

    private RClass psiClass = null;
    private RMethod psiMethod = null;


    public RClass getPsiClass() {
        return psiClass;
    }

    public RMethod getPsiMethod() {
        return psiMethod;
    }


    public Icon getIcon() {
        Visibility vis = getMethodVisibility();
        if (vis != null)
            switch (vis) {
                case PRIVATE:
                case PROTECTED:
                    // TODO: move icon references to RailwaysIcons
                    return RailwaysIcons.METHOD_NODE;

                case PUBLIC:
                    return RailwaysIcons.ROUTE_ACTION_NODE;
            }

        return RailwaysIcons.ERROR_NODE;
    }


    public Visibility getMethodVisibility() {
        if (getPsiMethod() == null)
            return null;

        return psiMethod.getVisibility();
    }


    public void update(Module module, String controllerName, String actionName) {
        RailsApp app = RailsApp.fromModule(module);
        update(app, controllerName, actionName);
    }

    // TODO: cache found classes and methods to reuse found values.

    public void update(RailsApp app, String controllerName, String actionName) {
        //this.controllerName = controllerName;
        //this.actionName = actionName;
        psiMethod = null;
        psiClass = null;

        if ((app == null) || controllerName.isEmpty())
            return;


        // Lookup in application controllers
        RailsController ctrl = app.findController(controllerName);
        if (ctrl != null) {
            psiClass = ctrl.getRClass();

            if (!actionName.isEmpty())
                psiMethod = findMethod(app, ctrl, actionName);
        } else {
            psiClass = RailwaysUtils.findControllerInIndex(controllerName,
                    app.getProject());

            if (psiClass != null && !actionName.isEmpty())
                psiMethod = psiClass.findMethodByName(actionName);
        }
    }


    // TODO: parent method can be from library. Handle this case.

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

}

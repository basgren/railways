package net.bitpot.railways.models;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.utils.RailwaysPsiUtils;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.Visibility;

import javax.swing.*;

/**
 * Contains information about controller action.
 *
 * @author Basil Gren
 *         on 23.11.2014.
 */
public class RailsActionInfo {

    // Class which is referenced by route action, it might not have
    // implementation of the method, as the method can be inherited.
    private RClass psiClass = null;

    // Route action method.
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

    public void update(RailsApp app, String controllerShortName, String actionName) {
        psiMethod = null;
        psiClass = null;

        if ((app == null) || controllerShortName.isEmpty())
            return;

        String qualifiedName = RailwaysPsiUtils.getControllerClassNameByShortName(
                controllerShortName);
        psiClass = RailwaysPsiUtils.findControllerClass(app, qualifiedName);

        if (psiClass != null)
            psiMethod = RailwaysPsiUtils.findControllerMethod(app,
                    psiClass, actionName);
    }

}

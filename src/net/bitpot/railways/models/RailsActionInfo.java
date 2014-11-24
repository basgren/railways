package net.bitpot.railways.models;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.rails.nameConventions.ControllersConventions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
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

    // Class which is referenced by route action, it might not have
    // implementation of the methid, as the method can be inherited.
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
        psiMethod = null;
        psiClass = null;

        if ((app == null) || controllerName.isEmpty())
            return;

        psiClass = findController(app, controllerName);
        if (psiClass != null)
            psiMethod = findMethod(app, psiClass, actionName);

//        System.out.println(String.format("Updated %s#%s: controller = %s, implementation class = %s",
//                controllerName, actionName,
//                (psiClass == null) ? "" : psiClass.getQualifiedName(),
//                (psiMethod == null) ? "" : psiMethod.getParentContainer().getFullName()));
    }


    @Nullable
    private RMethod findMethod(RailsApp app, RClass ctrlClass, String methodName) {
        RMethod method;

        while (true) {
            method = RubyPsiUtil.getMethodWithPossibleZeroArgsByName(ctrlClass, methodName);
            if (method != null)
                return method;

            // Try to look in parents
            RSuperClass parentClass = ctrlClass.getPsiSuperClass();
            if ((parentClass == null) || (parentClass.getName() == null))
                return null;

            // ControllerConventions is a ruby-plugin class that helps with
            // Rails string conversions.
            String ctrlName = ControllersConventions
                    .getControllerNameByClassName(parentClass.getName());
            if (ctrlName == null)
                return null;

            ctrlClass = findController(app, ctrlName);
            if (ctrlClass == null)
                return null;
        }
    }


    @Nullable
    private RClass findController(RailsApp app, String qualifiedClassName) {
        if ((app == null) || qualifiedClassName.isEmpty())
            return null;

        // Lookup in application controllers
        RailsController ctrl = app.findController(qualifiedClassName);
        if (ctrl != null)
            return ctrl.getRClass();

        // If controller is not found among application classes, proceed with
        // global class lookup
        return RailwaysUtils.findControllerInIndex(qualifiedClassName,
                app.getProject());
    }

}

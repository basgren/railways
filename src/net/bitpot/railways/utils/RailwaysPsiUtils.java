package net.bitpot.railways.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.rails.model.RailsController;
import org.jetbrains.plugins.ruby.rails.nameConventions.ControllersConventions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyProjectAndLibrariesScope;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RSuperClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.indexes.RubyClassModuleNameIndex;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.utils.NamingConventions;

import java.util.Collection;

/**
 * Class that contains helper methods for working with PSI elements.
 *
 * Created by Basil Gren on 11/27/14.
 */
public class RailwaysPsiUtils {

    // TODO: what about inflections? When we have, for example, module 'API', but in short name it will be as 'api'
    // TODO: display controller icon for route item action when controller is found.


    /**
     * Searches for controller in application and libraries.
     *
     * @param app Rails app for the current module
     * @param qualifiedClassName Full class name with modules, ex. "Devise::SessionsController"
     * @return Found RClass object or null if nothing is found.
     */
    @Nullable
    public static RClass findControllerClass(RailsApp app, String qualifiedClassName) {
        if ((app == null) || qualifiedClassName.isEmpty())
            return null;

        // Lookup in application controllers
        RailsController ctrl = app.findController(qualifiedClassName);
        if (ctrl != null)
            return ctrl.getRClass();

        // If controller is not found among application classes, proceed with
        // global class lookup
        return findControllerInIndex(qualifiedClassName,
                app.getProject());
    }


    /**
     * Searched for method implementation recursively in current class and all
     * included modules, then if not found, in parent class and all included
     * modules, etc.
     *
     * @param app Rails app.
     * @param ctrlClass Class in which method implementation will be searched for.
     * @param methodName Name of the method to find.
     * @return RMethod object of null.
     */
    @Nullable
    public static RMethod findControllerMethod(RailsApp app,
                                               @NotNull RClass ctrlClass,
                                               @NotNull String methodName) {
        RClass currentClass = ctrlClass;

        while (true) {
            RMethod method = RubyPsiUtil.getMethodWithPossibleZeroArgsByName(
                    currentClass, methodName);
            if (method != null)
                return method;

            method = findMethodInClassModules(currentClass, methodName);
            if (method != null)
                return method;

            // Try to look in parents
            RSuperClass psiParentRef = currentClass.getPsiSuperClass();
            if ((psiParentRef == null) || (psiParentRef.getName() == null))
                return null;

            String parentName = ControllersConventions
                    .getControllerNameByClassName(psiParentRef.getName());
            if (parentName == null)
                return null;

            currentClass = findControllerClass(app, parentName);
            if (currentClass == null)
                return null;
        }
    }


    /**
     * Performs search of specified controller name in IDE indexes.
     *
     * @param qualifiedName Full name of the controller (with modules, ex. Devise::SessionsController)
     * @param project Current project.
     * @return RClass object or null.
     */
    @Nullable
    public static RClass findControllerInIndex(@NotNull String qualifiedName,
                                               @NotNull Project project) {
        // Search should be performed using only class name, without modules.
        // For example, if we have Devise::SessionsController, we should search
        // for only 'SessionsController'
        String[] classPath = qualifiedName.split("::");
        String className = classPath[classPath.length - 1];

        Collection items = getClassOrModuleByName(className, project);

        // We can
        for (Object item: items) {
            if (!(item instanceof RClass))
                continue;

            RClass rubyClass = (RClass)item;

            if (qualifiedName.equals(rubyClass.getQualifiedName()))
                return rubyClass;
        }

        return null;
    }


    public static RModule findModuleInIndex(String qualifiedName, Project project) {
        String[] modulePath = qualifiedName.split("::");
        String moduleName = modulePath[modulePath.length - 1];

        Collection items = getClassOrModuleByName(moduleName, project);

        for (Object item: items) {
            if (!(item instanceof RModule))
                continue;

            RModule rubyModule = (RModule)item;

            if (qualifiedName.equals(rubyModule.getQualifiedName()))
                return rubyModule;
        }

        return null;
    }


    /**
     * Finds specified ruby class or module in IDE index.
     *
     * @param name Name of class or module to search for. This should be a name
     *             without any modules, so if we wand to find
     *             Devise::SessionsController, we should pass only
     *             SessionsController here.
     * @param project Current project.
     * @return Collection of PSI elements which match specified name.
     */
    @NotNull
    public static Collection getClassOrModuleByName(String name, Project project) {
        Object scope = new RubyProjectAndLibrariesScope(project);

        // TODO: check if StubIndex.getElements exists prior to RubyMine 6.3
        return StubIndex.getElements(RubyClassModuleNameIndex.KEY,
                name, project, (GlobalSearchScope) scope, RContainer.class);
    }


    public static String getControllerClassNameByShortName(String shortName) {
        return StringUtil.join(getControllerClassPathByShortName(shortName), "::");
    }


    public static String[] getControllerClassPathByShortName(String shortName) {
        // Process namespaces
        String[] classPath = (shortName + "_controller").split("/");
        for(int i = 0; i < classPath.length; i++)
            classPath[i] = NamingConventions.toCamelCase(classPath[i]);

        return classPath;
    }

    public static void logPsiParentChain(PsiElement elem) {
        while (elem != null) {
            if (elem instanceof PsiNamedElement) {
                System.out.println(elem.getClass().getName() + " --> Name: " + ((PsiNamedElement)elem).getName());

                if (elem instanceof RClass)
                    System.out.println(" ----- Class qualified name: " + ((RClass)elem).getQualifiedName());

            } else
                System.out.println(elem.getClass().getName() + " --> No name");

            elem = elem.getParent();
        }
    }


    /**
     * Filter that selects only 'include Module::Name' expressions.
     */
    private final static PsiElementFilter INCLUDE_MODULE_FILTER = new PsiElementFilter() {
        @Override
        public boolean isAccepted(PsiElement psiElement) {
            return (psiElement instanceof RCall) &&
                    ((RCall)psiElement).getCommand().equals("include");

        }
    };


    /**
     * Performs search in modules that are included in specified class.
     *
     * @param ctrlClass Class to look for modules.
     */
    private static RMethod findMethodInClassModules(RClass ctrlClass, String methodName) {
        PsiElement[] elements = PsiTreeUtil.collectElements(ctrlClass,
                INCLUDE_MODULE_FILTER);

        // Iterate from the end of the list as next included module can override
        // same-named methods of previously included module.
        int i = elements.length;
        while (--i >= 0) {
            RCall call = (RCall)elements[i];

            RPsiElement arg = call.getCallArguments().getElement(0);
            if (arg == null)
                continue;

            RModule module = findModuleInIndex(arg.getText(),
                    ctrlClass.getProject());
            if (module == null)
                continue;

            return module.findMethodByName(methodName);
        }

        return null;
    }
}

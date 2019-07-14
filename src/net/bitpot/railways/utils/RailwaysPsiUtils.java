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
        RContainer cont = findClassOrModule(qualifiedClassName,
                app.getProject());
        return cont instanceof RClass ? (RClass)cont : null;
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
            RMethod method = RubyPsiUtil.getMethodWithPossibleZeroArgsByName(currentClass, methodName);
            if (method != null)
                return method;

            method = findMethodInClassModules(currentClass, methodName);
            if (method != null)
                return method;

            // Search in parent classes
            RSuperClass psiParentRef = currentClass.getPsiSuperClass();
            if ((psiParentRef == null) || (psiParentRef.getName() == null))
                return null;

            currentClass = findControllerClass(app, psiParentRef.getName());
            if (currentClass == null)
                return null;
        }
    }


    /**
     * Performs search of specified class or module in IDE indexes.
     *
     * @param qualifiedName Full name of specified class or module controller
     *                      (with parent modules, ex. Devise::SessionsController)
     * @param project Current project.
     * @return RClass object, RModule object or null.
     */
    @Nullable
    public static RContainer findClassOrModule(@NotNull String qualifiedName,
                                               @NotNull Project project) {
        // Search should be performed using only class name, without modules.
        // For example, if we have Devise::SessionsController, we should search
        // for only 'SessionsController'
        String[] classPath = qualifiedName.split("::");
        String className = classPath[classPath.length - 1];

        Collection items = findClassesAndModules(className, project);

        for (Object item: items) {
            String name = null;

            if (item instanceof RClass)
                name = ((RClass)item).getQualifiedName();
            else if (item instanceof RModule)
                name = ((RModule)item).getQualifiedName();

            // Perform case insensitive comparison to avoid mess with acronyms.
            if (qualifiedName.equalsIgnoreCase(name))
                return (RContainer)item;
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
    public static Collection findClassesAndModules(String name, Project project) {
        GlobalSearchScope scope = new RubyProjectAndLibrariesScope(project);

        // StubIndex.getElements was introduced in 134.231 build (RubyMine 6.3)
        return StubIndex.getElements(RubyClassModuleNameIndex.KEY,
                name, project, scope, RContainer.class);
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


    /**
     * Filter that selects only 'include Module::Name' expressions.
     */
    private final static PsiElementFilter INCLUDE_MODULE_FILTER = psiElement ->
            (psiElement instanceof RCall) &&
            ((RCall)psiElement).getCommand().equals("include");


    /**
     * Performs search in all modules that are included in specified class. So
     * if ruby-class contains explicit includes:
     *
     *     include Admin::MyModule
     *     include Concerns::Logging
     *
     * the specified methodName will be searched in both modules in order
     * of their declaration.
     *
     * @param ctrlClass Class to look for modules.
     * @param methodName Method name to search within modules of specified ruby
     *                   class
     * @return First method which name matches methodName
     */
    @Nullable
    private static RMethod findMethodInClassModules(RClass ctrlClass, String methodName) {
        PsiElement[] elements = PsiTreeUtil.collectElements(ctrlClass,
                INCLUDE_MODULE_FILTER);

        // Iterate from the end of the list as next included module can override
        // same-named methods of previously included module.
        int i = elements.length;
        while (--i >= 0) {
            RCall includeMethodCall = (RCall)elements[i];

            RPsiElement moduleNameArg = includeMethodCall.getArguments().get(0);
            if (moduleNameArg == null)
                continue;

            RContainer cont = findClassOrModule(moduleNameArg.getText(),
                    ctrlClass.getProject());

            if (cont instanceof RModule)
                return RubyPsiUtil.getMethodWithPossibleZeroArgsByName(cont, methodName);
        }

        return null;
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
}

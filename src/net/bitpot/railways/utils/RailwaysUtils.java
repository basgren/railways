package net.bitpot.railways.utils;

import com.intellij.execution.ExecutionModes;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import net.bitpot.railways.gui.ErrorInfoDlg;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.routesView.RoutesManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.gem.GemsRunner;
import org.jetbrains.plugins.ruby.rails.model.RailsApp;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyProjectAndLibrariesScope;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.indexes.RubyClassModuleNameIndex;
import org.jetbrains.plugins.ruby.utils.NamingConventions;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Class that contains all API methods for Railways plugin.
 */
public class RailwaysUtils {
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getInstance(RailwaysUtils.class.getName());

    /**
     * Returns true if specified project has at least one Ruby on Rails module.
     *
     * @param project Project which should be checked for Rails modules.
     * @return True if a project has at least one Ruby on Rails module.
     */
    public static boolean hasRailsModules(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module m : modules)
            if (RailsApp.fromModule(m) != null)
                return true;

        return false;
    }


    /**
     * Internally used method that runs rake task and gets its output. This
     * method should be called from backgroundable task.
     *
     * @param module Rails module for which rake task should be run.
     * @return Output of 'rake routes'.
     */
    @Nullable
    public static ProcessOutput queryRakeRoutes(Module module, String routesTaskName) {
        // Get root path of Rails application from module.
        RailsApp app = RailsApp.fromModule(module);
        if ((app == null) || (app.getRailsApplicationRoot() == null))
            return null;

        String moduleContentRoot = app.getRailsApplicationRoot().getPresentableUrl();

        ModuleRootManager mManager = ModuleRootManager.getInstance(module);
        Sdk sdk = mManager.getSdk();
        if (sdk == null) {
            Notifications.Bus.notify(new Notification("Railways",
                    "Railways Error",
                    "Cannot update routes list for '" + module.getName() +
                    "' module, because its SDK is not set",
                    NotificationType.ERROR)
                    , module.getProject());
            return null;
        }

        try {
            // Will work on IntelliJ platform since 122.633 build (RubyMine 5)
            return GemsRunner.runGemsExecutableScript(sdk, module,
                    "rake", "rake",
                    moduleContentRoot, new ExecutionModes.SameThreadMode(),
                    routesTaskName, "--trace");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Shows a dialog with 'rake routes' error stacktrace.
     *
     * @param routesManager RoutesManager which error stacktrace should be
     *                      displayed.
     */
    public static void showErrorInfo(@NotNull RoutesManager routesManager) {
        ErrorInfoDlg.showError("Error information:",
                routesManager.getParseErrorStacktrace());
    }


    /**
     * Invokes action with specified ID. This method provides very simple
     * implementation of invoking action manually when ActionEvent and
     * DataContext are unavailable. Created DataContext in this method provides
     * only CommonDataKeys.PROJECT value.
     *
     * @param actionId ID of action to invoke
     * @param project Current project
     */
    public static void invokeAction(String actionId, final Project project) {
        AnAction act = ActionManager.getInstance().getAction(actionId);

        // For simple actions which don't heavily use data context, we can create
        // it manually.
        DataContext dataContext = new DataContext() {
            @Nullable
            @Override
            public Object getData(@NonNls String dataId) {
                if (CommonDataKeys.PROJECT.is(dataId))
                    return project;

                return null;
            }
        };

        act.actionPerformed(new AnActionEvent(null, dataContext,
                ActionPlaces.UNKNOWN, act.getTemplatePresentation(),
                ActionManager.getInstance(), 0));
    }


    public static void updateActionsStatus(Module module, RouteList routeList) {
        RailsApp app = RailsApp.fromModule(module);
        if (app == null)
            return;

        // TODO: investigate multiple calls of this method when switching focus from code to tool window without any changes.

        for (Route route: routeList)
            route.updateActionStatus(app);
    }


    public static void testIndexSearch(String name, Project project) {
        String[] classPath = RailwaysUtils.getRubyClassPathByShortName(name + "_controller");

        String className = classPath[classPath.length - 1];

        Collection items = RailwaysUtils.getItemsByName(className, project);

        System.out.println(String.format("Searched for: %s; found %d items.", className, items.size()));
    }


    @NotNull
    public static Collection getItemsByName(String name, Project project) {
        Object scope = new RubyProjectAndLibrariesScope(project);

        // TODO: find out how com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider handles name separators.

        // TODO: check if StubIndex.getElements exists prior to RubyMine 6.3
        Collection collection = StubIndex.getElements(
                RubyClassModuleNameIndex.KEY,
                name, project,
                (GlobalSearchScope) scope, RContainer.class);

        return collection;
    }


    public static String getRubyClassNameByShortName(String shortName) {
        return StringUtil.join(getRubyClassPathByShortName(shortName), "::");
    }

    public static String[] getRubyClassPathByShortName(String shortName) {
        // Process namespaces
        String[] classPath = shortName.split("/");
        for(int i = 0; i < classPath.length; i++)
            classPath[i] = NamingConventions.toCamelCase(classPath[i]);

        return classPath;
    }


    @Nullable
    public static RClass findClassInIndex(String classShortName, Project project) {
        String[] classPath = getRubyClassPathByShortName(classShortName);
        String className = classPath[classPath.length - 1];

        Collection items = RailwaysUtils.getItemsByName(className, project);
        System.out.println(String.format("Searched for: %s; found %d items.",
                className, items.size()));

        for (Object item: items) {
            if (!(item instanceof RClass))
                continue;



            RClass rubyClass = (RClass)item;

            System.out.println("---=== Parent chain for class: " + rubyClass.getName());
            logPsiParentChain(rubyClass);


            if (isClassMatchesPath(rubyClass, classPath))
                return rubyClass;
        }


        return null;
    }


    private static boolean isClassMatchesPath(RClass rubyClass, String[] classPath) {
        // PSI tree traversal is a bit tricky - RClass may have parents which
        // are not PsiNamedElement descendants. As we want to check class modules,
        // we should traverse taking into account only named elements and
        // their class.
        PsiElement elem = rubyClass;
        LinkedList<PsiNamedElement> psiPath = new LinkedList<PsiNamedElement>();

        while (elem != null && !(elem instanceof RFile)) {
            if (elem instanceof PsiNamedElement)
                psiPath.addFirst((PsiNamedElement)elem);

            elem = elem.getParent();
        }

        if (psiPath.size() != classPath.length)
            return false;

        int i = classPath.length;
        while (--i >= 0) {
            String psiName = psiPath.pollLast().getName();

            if (!classPath[i].equals(psiName))
                return false;
        }

        return true;
    }



    public static void logPsiParentChain(PsiElement elem) {
        while (elem != null) {
            if (elem instanceof PsiNamedElement)
                System.out.println(elem.getClass().getName() + " --> Name: " + ((PsiNamedElement)elem).getName());
            else
                System.out.println(elem.getClass().getName() + " --> No name");

            elem = elem.getParent();
        }
    }

}
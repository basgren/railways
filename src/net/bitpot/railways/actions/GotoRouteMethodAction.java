package net.bitpot.railways.actions;

import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.navigation.ChooseByRouteNameFilter;
import net.bitpot.railways.navigation.GotoRouteFilterConfiguration;
import net.bitpot.railways.navigation.GotoRouteMethodModel;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Action opens popup with list of all routes.
 */
public class GotoRouteMethodAction extends GotoActionBase {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(GotoRouteMethodAction.class.getName());


    @Override
    protected void gotoActionPerformed(AnActionEvent e) {

        final Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null)
            return;

        final GotoRouteMethodModel model = new GotoRouteMethodModel(project);
        PsiDocumentManager.getInstance(project).commitAllDocuments();

        showNavigationPopup(e, model, new GotoActionCallback<RequestMethod>() {

            @Override
            protected ChooseByNameFilter<RequestMethod> createFilter(@NotNull ChooseByNamePopup popup) {
                return new ChooseByRouteNameFilter(popup, model,
                        GotoRouteFilterConfiguration.getInstance(project), project);
            }


            @Override
            public void elementChosen(ChooseByNamePopup popup, Object element) {
                if (element instanceof Route)
                    ((Route) element).navigate(true);
            }
        });
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null)
            return;

        event.getPresentation().setEnabled(RailwaysUtils.hasRailsModules(project));
    }
}
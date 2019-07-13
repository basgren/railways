package net.bitpot.railways.navigation;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.requestMethods.RequestMethod;

/**
 * @author Basil Gren
 */
@State(
        name = "GotoRouteFilterConfiguration",
        storages = {@Storage(value = StoragePathMacros.WORKSPACE_FILE)})
public class GotoRouteFilterConfiguration extends ChooseByNameFilterConfiguration<RequestMethod> {

    public static ChooseByNameFilterConfiguration<RequestMethod> getInstance(Project project) {
        return ServiceManager.getService(project, GotoRouteFilterConfiguration.class);
    }


    @Override
    protected String nameForElement(RequestMethod method) {
        return method.getName();
    }
}

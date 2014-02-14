package net.bitpot.railways.navigation;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.routes.RequestType;

/**
 * @author Basil Gren
 */
@State(
        name = "GotoRouteFilterConfiguration",
        storages = {@Storage(file = StoragePathMacros.WORKSPACE_FILE)})
public class GotoRouteFilterConfiguration extends ChooseByNameFilterConfiguration<RequestType> {

    public static ChooseByNameFilterConfiguration<RequestType> getInstance(Project project) {
        return ServiceManager.getService(project, GotoRouteFilterConfiguration.class);
    }


    @Override
    protected String nameForElement(RequestType type) {
        return type.getName();
    }
}

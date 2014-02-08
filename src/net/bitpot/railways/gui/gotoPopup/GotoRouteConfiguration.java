package net.bitpot.railways.gui.gotoPopup;

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
        name = "GotoRouteConfiguration",
        storages = {@Storage(file = StoragePathMacros.WORKSPACE_FILE)})
public class GotoRouteConfiguration extends ChooseByNameFilterConfiguration<RequestType> {

    public static ChooseByNameFilterConfiguration<RequestType> getInstance(Project project) {
        return ServiceManager.getService(project, GotoRouteConfiguration.class);
    }


    @Override
    protected String nameForElement(RequestType type) {
        return type.getName();
    }
}

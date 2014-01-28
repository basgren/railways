package net.bitpot.railways.gui.gotoPopup;

import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.routes.RequestType;
import net.bitpot.railways.models.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author toXXIc
 */
public class GotoRouteMethodModel extends FilteringGotoByModel<RequestType>
{
    private static RouteItemsContributor[] contributors = {new RouteItemsContributor()};

    public GotoRouteMethodModel(Project project) {
        super(project, contributors);
    }


    @Nullable
    @Override
    protected RequestType filterValueFor(NavigationItem item) {
        return (item instanceof Route) ? ((Route) item).getRequestType() : null;
    }

    @Override
    public String getPromptText() {
        return "Enter URL part";
    }

    @Override
    public String getNotInMessage() {
        return "No matches found";
    }

    @Override
    public String getNotFoundMessage() {
        return "No matches found";
    }

    @Nullable
    @Override
    public String getCheckBoxName() {
        return null;
    }

    @Override
    public char getCheckBoxMnemonic() {
        return 0;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {
        // Do nothing
    }

    @NotNull
    @Override
    public String[] getSeparators() {
        return new String[0];
    }

    @Nullable
    @Override
    public String getFullName(Object element) {
        return ((Route)element).getPath();
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }
}

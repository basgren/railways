package net.bitpot.railways.navigation;

import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.routes.RequestMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Basil Gren
 */
public class GotoRouteMethodModel extends FilteringGotoByModel<RequestMethod> {

    public GotoRouteMethodModel(Project project) {
        super(project, ChooseByRouteRegistry.getInstance(project).getRouteContributors());
    }


    @Nullable
    @Override
    protected RequestMethod filterValueFor(NavigationItem item) {
        return (item instanceof Route) ? ((Route) item).getRequestMethod() : null;
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
        return ((Route) element).getPath();
    }


    @Override
    public boolean willOpenEditor() {
        return true;
    }
}
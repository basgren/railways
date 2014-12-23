package net.bitpot.railways.navigation;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.routesView.RoutesManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author Basil Gren
 */
public class RouteItemsContributor implements ChooseByNameContributor {

    private RoutesManager myRoutesManager;

    public RouteItemsContributor(RoutesManager routesManager) {
        myRoutesManager = routesManager;
    }


    public RoutesManager getRoutesManager() {
        return myRoutesManager;
    }


    /**
     * Returns the list of names for the specified project to which it is possible to navigate
     * by name.
     *
     * @param project                the project in which the navigation is performed.
     * @param includeNonProjectItems if true, the names of non-project items (for example,
     *                               library classes) should be included in the returned array.
     * @return the array of names.
     */
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        RouteList routes = myRoutesManager.getRouteList();

        int count = routes.size();
        String[] data = new String[count];

        while (count-- > 0)
            data[count] = routes.get(count).getName();

        return data;
    }


    /**
     * Returns the list of navigation items matching the specified name.
     *
     * @param name                   the name selected from the list.
     * @param pattern                the original pattern entered in the dialog
     * @param project                the project in which the navigation is performed.
     * @param includeNonProjectItems if true, the navigation items for non-project items (for example,
     *                               library classes) should be included in the returned array.
     * @return the array of navigation items.
     */
    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern,
                                           Project project, boolean includeNonProjectItems) {
        RouteList routes = myRoutesManager.getRouteList();

        return routes.getRoutesByName(name);
    }
}

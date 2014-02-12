package net.bitpot.railways.navigation;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.routesView.RoutesManager;
import net.bitpot.railways.routesView.RoutesView;
import net.bitpot.railways.models.RouteList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Basil Gren
 */
public class RouteItemsContributor implements ChooseByNameContributor {

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
        // TODO: aggregate all routes from different modules.
        // Debug stub
        RoutesManager rm = RoutesView.getInstance(project).getCurrentRoutesManager();
        if (rm == null)
            return new String[0];

        RouteList routes = rm.getRouteList();

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
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {

        // TODO: implementation
        // Debug stub
        RoutesManager rm = RoutesView.getInstance(project).getCurrentRoutesManager();
        if (rm == null)
            return new NavigationItem[0];

        RouteList routes = rm.getRouteList();

        return routes.getRoutesByName(name);
    }
}

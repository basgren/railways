package net.bitpot.railways.navigation;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.routesView.RoutesManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Basil Gren
 *         on 12.02.14.
 */
public class ChooseByRouteRegistry {

    private List<RouteItemsContributor> myContributors = new ArrayList<>();


    public static ChooseByRouteRegistry getInstance(Project project) {
        return ServiceManager.getService(project, ChooseByRouteRegistry.class);
    }


    /**
     * Creates and adds to the list a new contributor linked with specified
     * RouteManager.
     *
     * @param routesManager Route manager which will be used by new contributor.
     */
    public void addContributorFor(RoutesManager routesManager) {
        RouteItemsContributor contributor = new RouteItemsContributor(routesManager);
        myContributors.add(contributor);
    }


    /**
     * Removes contributor linked with specified RoutesManager.
     *
     * @param routesManager Routes manger
     */
    public void removeContributor(RoutesManager routesManager) {
        for(int i = myContributors.size() - 1; i >= 0; i--)
            if (myContributors.get(i).getRoutesManager() == routesManager)
                myContributors.remove(i);
    }

    public RouteItemsContributor[] getRouteContributors() {
        return myContributors.toArray(new RouteItemsContributor[0]);
    }

}
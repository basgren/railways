package net.bitpot.railways.models;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Class that contains all parameters of routes filtration.
 */
public class RoutesFilter {
    private RouteTableModel model;
    private String pathFilter;


    public RoutesFilter(@NotNull RouteTableModel parent) {
        model = parent;
        reset();
    }


    public void reset() {
        pathFilter = "";
    }


    public String getPathFilter() {
        return pathFilter;
    }


    public void setPathFilter(String pathFilter) {
        pathFilter = pathFilter.toLowerCase();
        if (!this.pathFilter.equals(pathFilter)) {
            this.pathFilter = pathFilter;
            filterChanged();
        }
    }


    /**
     * Returns true if no filter parameter is set.
     *
     * @return True when all filters are reset, false - when at least one filter is set.
     */
    public boolean isFiltersEmpty() {
        return pathFilter.equals("");
    }


    /**
     * Applies current filter and fills target route list with suitable elements.
     *
     * @param source Source route list
     * @param target Target route list.
     */
    public void applyFilter(RouteList source, RouteList target) {
        target.clear();

        if (isFiltersEmpty()) {
            target.setSize(source.size());
            Collections.copy(target, source);
            return;
        }

        // Filter all elements
        for (Route route : source)
            if (matchesFilter(route))
                target.add(route);
    }


    /**
     * Checks whether specified route matches current filter.
     *
     * @param route Route to be matched against current filter.
     * @return True if route matches filter, false otherwise.
     */
    private boolean matchesFilter(Route route) {
        return route.getPath().toLowerCase().contains(pathFilter) ||
                route.getActionText().toLowerCase().contains(pathFilter) ||
                route.getRouteName().toLowerCase().contains(pathFilter);
    }


    private void filterChanged() {
        model.filterChanged();
    }
}

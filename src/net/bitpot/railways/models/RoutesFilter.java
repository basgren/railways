package net.bitpot.railways.models;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Class that contains all parameters of routes filtration.
 */
public class RoutesFilter {
    private RouteTableModel model;
    private String pathFilter;
    private boolean mountedRoutesVisible; 


    public RoutesFilter(@NotNull RouteTableModel parent) {
        model = parent;
        reset();
    }


    public void reset() {
        pathFilter = "";
        mountedRoutesVisible = true;
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


    public boolean isMountedRoutesVisible() {
        return mountedRoutesVisible;
    }

    public void setMountedRoutesVisible(boolean value) {
        if (mountedRoutesVisible != value) {
            mountedRoutesVisible = value;
            filterChanged();
        }
    }


    /**
     * Returns true if any filter is set and should be applied.
     * Actually checks if filter values are set to defaults or not. 
     *
     * @return True when any filter is active, false otherwise.
     */
    public boolean isFilterActive() {
        return !pathFilter.equals("") || !mountedRoutesVisible;
    }


    /**
     * Applies current filter and fills target route list with suitable elements.
     *
     * @param source Source route list
     * @param target Target route list.
     */
    public void applyFilter(@NotNull RouteList source, @NotNull RouteList target) {
        target.clear();

        if (!isFilterActive()) {
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
        if (!mountedRoutesVisible && route.getParentEngine() != null)
            return false;

        return route.getPath().toLowerCase().contains(pathFilter) ||
                route.getActionTitle().toLowerCase().contains(pathFilter) ||
                route.getRouteName().toLowerCase().contains(pathFilter);
    }


    private void filterChanged() {
        model.filterChanged();
    }
}

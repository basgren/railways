package net.bitpot.railways.models;

import com.intellij.util.ArrayUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Container for routes.
 */
public class RouteList extends Vector<Route> {
    // Route name is a key, array of routes with this name is a value.
    // This hash is used for quick lookup in navigation dialogs.
    private HashMap<String, ArrayList<Route>> namesIndex = new HashMap<>();


    @Override
    public void clear() {
        super.clear();
        namesIndex.clear();
    }


    /**
     * Returns an array of routes that have specified name. As several routes can have the same name (they
     * can differ by method - POST, GET, etc.) the method can return arrays with several elements.
     *
     * @param name Route name to search
     * @return Array of routes or empty route array if nothing is found.
     */
    public Route[] getRoutesByName(String name) {
        if (namesIndex.size() == 0)
            reindexRouteNames();

        List<Route> value = namesIndex.get(name);
        if (value == null)
            return new Route[0];

        return ArrayUtil.toObjectArray(value, Route.class);
    }


    /**
     * Performs route names reindex.
     */
    private void reindexRouteNames() {
        namesIndex.clear();

        for (Route route : this) {
            // As we do not use null as value, we can skip checking existence of the key
            ArrayList<Route> value = namesIndex.computeIfAbsent(route.getName(),
                    k -> new ArrayList<>());

            value.add(route);
        }
    }
}
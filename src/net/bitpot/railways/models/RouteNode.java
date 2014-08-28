package net.bitpot.railways.models;

import com.intellij.util.ArrayUtil;

import java.util.*;

/**
 * Container for routes. Can contain any RouteItem (Route of RouteNode).
 */
public class RouteNode extends Vector<RouteNode> {

    private Route route;
    private boolean isContainer;
    private String title;


    private static final Comparator<RouteNode> comparator = new Comparator<RouteNode>() {

        /**
         * Sorts RouteNodes. First come containers (alphabetically), then come
         * routes (alphabetically)
         *
         */
        @Override
        public int compare(RouteNode n1, RouteNode n2) {
            if (n1.isContainer() && !n2.isContainer())
                return -1;

            if (!n1.isContainer() && n2.isContainer())
                return 1;

            return n1.getTitle().compareTo(n2.getTitle());
        }
    };


    public RouteNode(String title, Route route, boolean isContainer) {
        this.title = title;
        this.route = route;
        this.isContainer = isContainer;
    }


    /**
     *
     * @param list
     * @return
     */
    public static RouteNode buildTree(RouteList list) {
        RouteNode root = new RouteNode("", null, false);

        for(Route route: list) {
            // Break route path into parts, but remove leading slash before.
            String[] parts = route.getPath().substring(1).split("/");

            pushRouteNode(root, route, parts, 0);
        }

        root.sort();

        return root;
    }


    /**
     * Sorts routes alphabetically
     */
    private void sort() {
        Collections.sort(this, RouteNode.comparator);

        for (RouteNode node: this)
            if (node.isContainer())
                node.sort();
    }


    private static void pushRouteNode(RouteNode parent, Route route,
                                      String[] parts, int partIndex) {
        boolean isContainer = (partIndex + 1 < parts.length);

        RouteNode child = null;
        String childTitle = parts[partIndex];

        // Search for existing container node
        if (isContainer) {
            for (RouteNode node: parent) {
                if (node.getTitle().equals(childTitle)) {
                    child = node;
                    break;
                }
            }
        }

        if (child == null) {
            child = new RouteNode(childTitle, route, isContainer);
            parent.add(child);
        }

        if (isContainer) {
            pushRouteNode(child, route, parts, partIndex + 1);
        }
    }


    public String getTitle() {
        return title;
    }


    public boolean isContainer() {
        return isContainer;
    }
}
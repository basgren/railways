package net.bitpot.railways.parser;

import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.models.RouteNode;

/**
 * @author Basil Gren
 *         on 31.08.14.
 */
public class RouteTreeBuilder {


    public static RouteNode buildTree(RouteList list) {
        RouteNode root = new RouteNode("", null);

        for(Route route: list) {
            // Break route path into parts, but remove leading slash before.
            String[] parts = route.getPath().substring(1).split("/");

            pushRouteNode(root, route, parts, 0);
        }

        root.sort();

        return root;
    }


    private static void pushRouteNode(RouteNode parent, Route route,
                                      String[] parts, int partIndex) {
        boolean isLeaf = (partIndex + 1 == parts.length);

        RouteNode child = null;
        String childTitle = parts[partIndex];

        // Search for existing container node
        if (!isLeaf) {

            for (int i = 0; i < parent.getChildCount(); i++) {
                RouteNode node = (RouteNode) parent.getChildAt(i);

                if (node.getTitle().equals(childTitle)) {
                    child = node;
                    break;
                }
            }
        }

        if (child == null) {
            child = new RouteNode(childTitle, isLeaf ? route : null);
            parent.add(child);
        }

        if (!isLeaf) {
            pushRouteNode(child, route, parts, partIndex + 1);
        }
    }

}

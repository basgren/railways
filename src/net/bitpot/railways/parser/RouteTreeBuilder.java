package net.bitpot.railways.parser;

import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.models.RouteNode;
import net.bitpot.railways.utils.RailwaysUtils;

/**
 * @author Basil Gren
 *         on 31.08.14.
 */
public class RouteTreeBuilder {


    public static RouteNode buildTree(RouteList list) {
        RouteNode root = new RouteNode("", null);

        for(Route route: list) {
            // Break route path into parts, but remove leading slash before.
            String path = route.getPath(); // RailwaysUtils.trimRequestFormat(route.getPath());
            String[] parts = path.substring(1).split("/");

            pushRouteNode(root, route, parts, 0);
        }

        root.sort();

        return root;
    }


    private static void pushRouteNode(RouteNode parent, Route route,
                                      String[] parts, int partIndex) {
        boolean isLastPart = (partIndex + 1 == parts.length);

        RouteNode targetNode = null;
        String currentTitle = parts[partIndex];

        // Search for existing container node
        if (!isLastPart) {
            RouteNode newTarget = parent.findByTitle(currentTitle);

            if (newTarget != null)
                targetNode = newTarget;
        }

        if (targetNode == null) {
            if (currentTitle.equals(""))
                currentTitle = "/";

            targetNode = new RouteNode(currentTitle, isLastPart ? route : null);
            parent.add(targetNode);
        }

        if (!isLastPart) {
            pushRouteNode(targetNode, route, parts, partIndex + 1);
        }
    }

}

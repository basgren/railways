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

    // For nested resource routes we should group them together under one parent
    // node. For example, we have "/books" and "/books/:id" route. In treeview
    // we should have 3 nodes:
    //
    //  + books
    //      + GET /
    //      + GET :id


    public static RouteNode buildTree(RouteList list) {
        RouteNode root = new RouteNode("", null);

        for(Route route: list) {
            // Break route path into parts, but remove leading slash before.
            String path = RailwaysUtils.trimRequestFormat(route.getPath());
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
            RouteNode newTarget = parent.findNode(currentTitle);

            // If we found a leaf node (linked to Route), we should create
            // new group and move it into new group.
            if (newTarget != null) {
                if (newTarget.getRoute() != null) {
                    targetNode = new RouteNode(currentTitle, null);
                    parent.add(targetNode);

                    // And now move all routes to new targetNode.
                    while (newTarget != null) {
                        newTarget.setTitle("/");
                        targetNode.add(newTarget);
                        newTarget = parent.findRoute(currentTitle);
                    }
                } else
                    targetNode = newTarget;
            }

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

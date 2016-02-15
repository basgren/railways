package net.bitpot.railways.parser;

import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.ui.tree.RouteNode;
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
            String path = RailwaysUtils.trimRequestFormat(route.getPath());
            String[] parts = path.substring(1).split("/");

            layoutNode(root, route, parts, 0);
        }

        root.sort();

        return root;
    }


    private static void layoutNode(RouteNode parent, Route route,
                                      String[] parts, int partIndex) {
        boolean isLastPart = (partIndex + 1 == parts.length);
        String currentTitle = parts[partIndex];

        if (isLastPart) {
            if (currentTitle.equals(""))
                currentTitle = "/";

            parent.add(new RouteNode(currentTitle, route));

            return;
        }

        // Search for existing container node
        RouteNode targetNode = parent.findGroup(currentTitle);

        if (targetNode == null) {
            RouteNode.GroupType groupType = RouteNode.GroupType.GROUP;
            if ((partIndex == 0) && (route.getParentEngine() != null))
                groupType = RouteNode.GroupType.MOUNTED_ROOT;

            targetNode = new RouteNode(currentTitle, null, groupType);
            parent.add(targetNode);
        }

        layoutNode(targetNode, route, parts, partIndex + 1);
    }


    // For nested resource routes we should group them together under one parent
    // node. For example, we have "/books" and "/books/:id" route. In treeview
    // we should have 3 nodes:
    //
    //  + books
    //      + GET /
    //      + GET :id
    // TODO: This one is experimental - need to polish a bit
    private static void layoutNodeGrouped(RouteNode parent, Route route,
                                          String[] parts, int partIndex) {
        boolean isLastPart = (partIndex + 1 == parts.length);

        RouteNode targetNode = null;
        String currentTitle = parts[partIndex];

        // Search for existing container node
        if (!isLastPart) {
            RouteNode newTarget = parent.findGroup(currentTitle);

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
            layoutNodeGrouped(targetNode, route, parts, partIndex + 1);
        }
    }

}

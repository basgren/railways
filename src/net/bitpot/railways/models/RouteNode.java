package net.bitpot.railways.models;

import com.intellij.util.ArrayUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Container for routes. Can contain any RouteItem (Route of RouteNode).
 */
public class RouteNode extends Vector<RouteTreeItem> implements RouteTreeItem {

    private Route route;
    private boolean isLeaf;
    private String title;



    public RouteNode(String title, Route route, boolean isLeaf) {
        this.title = title;
        this.route = route;
        this.isLeaf = isLeaf;
    }


    @Override
    public boolean isLeaf() {
        return isLeaf;
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

        return root;
    }


    private static void pushRouteNode(RouteNode node, Route route,
                                      String[] parts, int partIndex) {
        boolean isLeaf = (partIndex + 1 == parts.length);

        RouteNode child = new RouteNode(parts[partIndex], route, isLeaf);
        node.add(child);

        if (!isLeaf) {
            pushRouteNode(child, route, parts, partIndex + 1);
        }
    }

}
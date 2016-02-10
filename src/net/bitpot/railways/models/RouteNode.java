package net.bitpot.railways.models;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Comparator;

/**
 * Container for routes. Can contain any RouteItem (Route of RouteNode).
 */
public class RouteNode extends DefaultMutableTreeNode implements TreeNode {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(RouteNode.class.getName());

    private Route route;
    private String title;


    private static final Comparator<RouteNode> comparator = new Comparator<RouteNode>() {

        /**
         * Sorts RouteNodes. First come containers (alphabetically), then come
         * routes (alphabetically)
         *
         */
        @Override
        public int compare(RouteNode n1, RouteNode n2) {
            if (n1.isLeaf() && !n2.isLeaf())
                return 1;

            if (!n1.isLeaf() && n2.isLeaf())
                return -1;

            return n1.getTitle().compareTo(n2.getTitle());
        }
    };


    public RouteNode(String title, Route route) {
        this.title = title;
        this.route = route;
    }

    public boolean isRoute() {
        return this.route != null;
    }

    /**
     * Sorts routes alphabetically
     */
    public void sort() {
        if (this.children == null)
            return;

        Collections.sort(this.children, RouteNode.comparator);

        for (Object node: this.children)
            if (!((RouteNode)node).isLeaf())
                ((RouteNode)node).sort();
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Route getRoute() {
        return route;
    }



    @Override
    public String toString() {
        String s = getTitle();
        if (route != null)
            s = s + " [" + route.getRequestMethod() + " " + route.getPath() + "]";

        return s;
    }

    /**
     * Performs one-level search of child node of any type by title.
     *
     * @param title Title to search for
     * @return RouteNode or null if nothing is found.
     */
    @Nullable
    public RouteNode findNode(String title) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            RouteNode node = (RouteNode) getChildAt(i);

            if (node.getTitle().equals(title))
                return node;
        }

        return null;
    }

    @Nullable
    public RouteNode findGroup(String title) {
        return findNode(title, false);
    }

    @Nullable
    public RouteNode findRoute(String title) {
        return findNode(title, true);
    }

    private RouteNode findNode(String title, boolean findRoutes) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            RouteNode node = (RouteNode) getChildAt(i);

            if (node.isRoute() == findRoutes && node.getTitle().equals(title))
                return node;
        }

        return null;
    }

    public int getChildRoutesCount() {
        int totalRoutes = 0;
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            RouteNode node = (RouteNode) getChildAt(i);

            if (node.isRoute())
                totalRoutes++;
            else
                totalRoutes += node.getChildRoutesCount();
        }

        return totalRoutes;
    }
}
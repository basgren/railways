package net.bitpot.railways.models;

import com.intellij.openapi.diagnostic.Logger;

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

    public Route getRoute() {
        return route;
    }
}
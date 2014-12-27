package net.bitpot.railways.gui;

import com.intellij.ui.treeStructure.Tree;
import net.bitpot.railways.models.RouteNode;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Basil Gren
 *         on 28.12.2014.
 */
public class RoutesTree extends Tree {

    public RoutesTree(TreeModel treemodel) {
        super(treemodel);

        // Init routes tree
        setCellRenderer(new RouteTreeCellRenderer());

        initHandlers();
    }


    private void initHandlers() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Tree target = (Tree) e.getSource();
                    navigateToRouteNode((RouteNode) target.getLastSelectedPathComponent());
                }
            }
        });


        // Register mouse handler to handle double-clicks.
        // Double clicking a row will navigate to the action of selected route.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Tree target = (Tree) e.getSource();
                    TreePath path = target.getClosestPathForLocation(e.getX(), e.getY());
                    navigateToRouteNode((RouteNode) path.getLastPathComponent());
                }
            }
        });
    }


    private void navigateToRouteNode(RouteNode node) {
        if ((node == null) || (!node.isLeaf()))
            return;

        node.getRoute().navigate(false);
    }

}

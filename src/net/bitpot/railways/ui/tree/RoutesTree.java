package net.bitpot.railways.ui.tree;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.treeStructure.Tree;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.ui.MainPanel;
import net.bitpot.railways.models.RouteNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Basil Gren
 *         on 28.12.2014.
 */
public class RoutesTree extends Tree implements DataProvider {

    private MainPanel parentPanel;

    public RoutesTree(@NotNull TreeModel treemodel,
                      @NotNull MainPanel parentPanel) {
        super(treemodel);

        this.parentPanel = parentPanel;

        // Init routes tree
        setCellRenderer(new RouteTreeCellRenderer());

        // Single node select
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

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

        getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                updateNodeInfo();
            }
        });
    }


    public void updateNodeInfo() {
        RouteNode node = (RouteNode)getLastSelectedPathComponent();
        parentPanel.showRouteInfo(node != null ? node.getRoute() : null);
    }


    private void navigateToRouteNode(RouteNode node) {
        if ((node == null) || (!node.isLeaf()))
            return;

        node.getRoute().navigate(false);
    }

    @Nullable
    @Override
    public Object getData(@NonNls String dataId) {
        if (PlatformDataKeys.SELECTED_ITEMS.is(dataId))
            return getSelectedRoutes();

        if (PlatformDataKeys.SELECTED_ITEM.is(dataId))
            return getSelectedRoute();

        return null;
    }

    @Nullable
    public Route getSelectedRoute() {
        RouteNode node = (RouteNode)(getSelectionPath().getLastPathComponent());

        return node.getRoute();
    }

    @Nullable
    public Route[] getSelectedRoutes() {
        List<Route> result = new ArrayList<Route>();

        TreePath[] selected = getSelectionPaths();
        if (selected == null)
            return null;

        for(TreePath path: selected) {
            Route route = ((RouteNode)path.getLastPathComponent()).getRoute();
            if (route != null)
                result.add(route);
        }

        return result.toArray(new Route[result.size()]);
    }
}

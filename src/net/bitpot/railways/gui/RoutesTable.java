package net.bitpot.railways.gui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ArrayUtil;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteTableModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * We should add some behavior to default JTable to be able copy custom data.
 *
 * Our component (JTable) should implement DataProvider interface. In this
 * case when action is invoked in context of this component, we will be able to
 * pass our own data to the action handler.
 */
public class RoutesTable extends JBTable implements DataProvider {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(RoutesTable.class.getName());


    private MainPanel parentPanel;


    /**
     * Constructs a default <code>JTable</code> that is initialized with a default
     * data model, a default column model, and a default selection
     * model.
     */
    public RoutesTable(@NotNull RouteTableModel tableModel,
                       @NotNull MainPanel parentPanel) {
        super();

        setModel(tableModel);
        this.parentPanel = parentPanel;

        // Init routes table
        setDefaultRenderer(Route.class,
                new RouteCellRenderer(tableModel.getFilter()));

        setDefaultRenderer(Object.class,
                new FilterHighlightRenderer(tableModel.getFilter()));

        setRowHeight(20);

        // ---=== Event handlers ===---

        addMouseListener(new MyPopupHandler());


        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting())
                    updateNodeInfo();
            }
        });


        // Register mouse handler to handle double-clicks.
        // Double clicking a row will navigate to the action of selected route.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    navigateToRouteInRow(target.rowAtPoint(e.getPoint()));
                }
            }
        });


        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    JTable target = (JTable) e.getSource();
                    navigateToRouteInRow(target.getSelectedRow());
                }
            }
        });
    }


    /**
     * Navigates to a route in specified rowIndex, if row exists.
     * @param rowIndex Row index in table view which contains route to navigate to.
     */
    private void navigateToRouteInRow(int rowIndex) {
        if (rowIndex < 0)
            return;

        int row = convertRowIndexToModel(rowIndex);
        parentPanel.getRouteTableModel().getRoute(row).navigate(false);
    }


    /**
     * Method returns custom data object. It's useful for replacing IDE's data providers by custom ones.
     *
     * @param dataId Data key
     * @return Data object.
     */
    @Nullable
    @Override
    public Object getData(@NonNls String dataId) {
        // Good example of usage is in com.intellij.openapi.editor.impl.EditorComponentImpl (see getData method)

        if (PlatformDataKeys.SELECTED_ITEMS.is(dataId))
            return getSelectedRoutes();

        if (PlatformDataKeys.SELECTED_ITEM.is(dataId))
            return getSelectedRoute();

        return null;
    }


    @Nullable
    private Route getSelectedRoute() {
        int selectedId = convertRowIndexToModel(getSelectedRow());
        if (selectedId < 0)
            return null;

        return ((RouteTableModel) getModel()).getRoute(selectedId);
    }


    @NotNull
    private Route[] getSelectedRoutes() {
        RouteTableModel model = (RouteTableModel) getModel();
        int[] selectedRows = getSelectedRows();

        Route[] selectedRoutes = new Route[selectedRows.length];

        for(int i = 0; i < selectedRows.length; i++) {
            selectedRoutes[i] =
                    model.getRoute(convertRowIndexToModel(selectedRows[i]));
        }

        return selectedRoutes;
    }


    public void updateNodeInfo() {
        int id = convertRowIndexToModel(getSelectedRow());
        if (id < 0)
            return;

        Route route = ((RouteTableModel) getModel()).getRoute(id);
        parentPanel.showRouteInfo(route);
    }


    private class MyPopupHandler extends PopupHandler {

        @Override
        public void mousePressed(MouseEvent e) {
            handleRightClick(e);
            super.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            handleRightClick(e);
            super.mouseReleased(e);
        }

        private void handleRightClick(MouseEvent e) {
            if (!SwingUtilities.isRightMouseButton(e) || !e.isPopupTrigger())
                return;

            // Before showing the popup, we should update selection properly:
            //  * if right-clicked on existing selection - do nothing.
            //  * if clicked outside current selection - clear it and select
            //    only item that was clicked.
            int clickedRowIndex = rowAtPoint(e.getPoint());
            boolean isSelectionClicked =
                    ArrayUtil.indexOf(getSelectedRows(), clickedRowIndex) >= 0;

            if (!isSelectionClicked && clickedRowIndex >= 0 &&
                    clickedRowIndex < getRowCount()) {
                setRowSelectionInterval(clickedRowIndex, clickedRowIndex);
            }
        }

        @Override
        public void invokePopup(Component comp, int x, int y) {
            ActionManager actMgr = ActionManager.getInstance();
            ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("railways.PopupMenu");

            ActionPopupMenu popupMenu = actMgr.createActionPopupMenu(ActionPlaces.UNKNOWN, group);
            popupMenu.getComponent().show(comp, x, y);
        }
    }
}
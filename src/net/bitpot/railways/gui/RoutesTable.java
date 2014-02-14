package net.bitpot.railways.gui;

import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.PopupHandler;
import net.bitpot.railways.models.RouteTableModel;
import net.bitpot.railways.models.Route;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;


/**
 * We should add some behavior to default JTable to be able copy custom data.
 * To add ability to copy data we should follow next steps:
 * 1. Our component (JTable) should implement DataProvider interface. In this case when action is invoked
 * in context of this component, we will be able to pass out own data object, i.e. CopyProvider.
 * 2. Implement CopyProvider interface (it also could be done in private class) which is responsible for
 * changing Copy action status and copying custom information.
 */
public class RoutesTable extends JTable implements CopyProvider, DataProvider {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(RoutesTable.class.getName());


    /**
     * Constructs a default <code>JTable</code> that is initialized with a default
     * data model, a default column model, and a default selection
     * model.
     */
    public RoutesTable() {
        super();

        addMouseListener(new MyPopupHandler());
    }


    /**
     * Method returns custom data object. It's useful for replacing IDE's data providers by custom ones.
     *
     * @param dataId Data key
     * @return Data object.
     */
    @Override
    public Object getData(@NonNls String dataId) {
        // Good example of usage is in com.intellij.openapi.editor.impl.EditorComponentImpl (see getData method)

        // We have custom copy provider, so return it when requested.
        if (PlatformDataKeys.COPY_PROVIDER.is(dataId))
            return this;

        return null;
    }


    /**
     * Performs copy operation of the first selected route in the table.
     *
     * @param dataContext Data context which action was invoked in.
     */
    @Override
    public void performCopy(@NotNull DataContext dataContext) {
        if (getSelectedRow() < 0)
            return;

        Route route = ((RouteTableModel) getModel()).getRoute(convertRowIndexToModel(getSelectedRow()));
        CopyPasteManager.getInstance().setContents(new StringSelection(route.getRouteName()));
    }


    /**
     * Specifies conditions when copy action is enabled or disabled in routes popup menu.
     *
     * @param dataContext Data context.
     * @return True or false.
     */
    @Override
    public boolean isCopyEnabled(@NotNull DataContext dataContext) {
        return getSelectedRowCount() > 0;
    }


    @Override
    public boolean isCopyVisible(@NotNull DataContext dataContext) {
        return true;
    }


    private class MyPopupHandler extends PopupHandler {

        @Override
        public void mouseReleased(MouseEvent e) {
            // Here we override default behavior of mouseReleased to add implementation of
            // row selection by right-click before popup is shown.
            if (e.getButton() == MouseEvent.BUTTON3) {
                int r = rowAtPoint(e.getPoint());
                if (r >= 0 && r < getRowCount()) {
                    setRowSelectionInterval(r, r);
                } else {
                    clearSelection();
                }
            }

            // And finally we can show handle popup
            super.mouseReleased(e);
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
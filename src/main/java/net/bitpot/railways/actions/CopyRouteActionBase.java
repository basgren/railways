package net.bitpot.railways.actions;

import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.*;
import net.bitpot.railways.gui.RoutesCopyProvider;
import net.bitpot.railways.models.Route;

/**
 * Provides functionality to copy route data from selected items. Requires
 * data context to implement DataProvider interface and provide data for
 * SELECTED_ITEMS and SELECTED_ITEM keys.
 */
public abstract class CopyRouteActionBase extends AnAction {

    /**
     * Should return from route a string to be copied.
     * @param route Route which data will be copied.
     * @return A string to be copied.
     */
    abstract public String getRouteValue(Route route);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DataContext ctx = anActionEvent.getDataContext();

        Route[] routes = (Route[])PlatformDataKeys.SELECTED_ITEMS.getData(ctx);

        CopyProvider provider = new RoutesCopyProvider(routes) {
            @Override
            public String getCopyValue(Route route) {
                return getRouteValue(route);
            }
        };

        provider.performCopy(ctx);
    }

    @Override
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        DataContext dataContext = event.getDataContext();

        // Selected item is faster than building selected route list every time,
        // so we'll use it to check whether anything is selected.
        Object selectedRoute = PlatformDataKeys.SELECTED_ITEM.getData(dataContext);

        presentation.setEnabled(selectedRoute != null);
        presentation.setVisible(true);
    }
}

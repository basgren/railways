package net.bitpot.railways.actions;

import com.intellij.ide.CopyProvider;
import com.intellij.ide.TextCopyProvider;
import com.intellij.openapi.actionSystem.*;
import net.bitpot.railways.models.Route;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CopyRouteAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DataContext ctx = anActionEvent.getDataContext();

        Route[] routes = (Route[])PlatformDataKeys.SELECTED_ITEMS.getData(ctx);

        CopyProvider provider = new MyCopyProvider(routes);
        provider.performCopy(ctx);
    }

    private class MyCopyProvider extends TextCopyProvider {

        private Route[] routes;

        public MyCopyProvider(Route[] routes) {
            this.routes = routes;
        }

        @Nullable
        @Override
        public Collection<String> getTextLinesToCopy() {
            if (routes == null)
                return null;

            List<String> copyLines = new ArrayList<String>(routes.length);

            for (Route route : routes) {
                copyLines.add(route.getPath());
            }

            return copyLines;
        }
    }

    @Override
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        DataContext dataContext = event.getDataContext();
        Object selectedRoute = PlatformDataKeys.SELECTED_ITEM.getData(dataContext);

        presentation.setEnabled(selectedRoute != null);
        presentation.setVisible(true);
    }
}

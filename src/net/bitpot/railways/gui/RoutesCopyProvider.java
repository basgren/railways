package net.bitpot.railways.gui;

import com.intellij.ide.TextCopyProvider;
import net.bitpot.railways.models.Route;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class RoutesCopyProvider extends TextCopyProvider {

    private Route[] routes;

    public RoutesCopyProvider(Route[] routes) {
        this.routes = routes;
    }

    /**
     * Returns route value that should be copied to clipboard.
     * @param route Route which value should be copied to clipboard (route name in this
     *              implementation).
     * @return String to copy
     */
    public abstract String getCopyValue(Route route);

    @Nullable
    @Override
    public Collection<String> getTextLinesToCopy() {
        if (routes == null)
            return null;

        List<String> copyLines = new ArrayList<>(routes.length);

        for (Route route : routes) {
            copyLines.add(getCopyValue(route));
        }

        return copyLines;
    }
}

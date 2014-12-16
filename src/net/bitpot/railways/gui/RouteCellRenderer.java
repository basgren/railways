package net.bitpot.railways.gui;

import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.models.RailsActionInfo;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteTableModel;
import net.bitpot.railways.models.RoutesFilter;
import net.bitpot.railways.models.routes.SimpleRoute;
import net.bitpot.railways.parser.route.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteCellRenderer extends FilterHighlightRenderer {


    public RouteCellRenderer(@NotNull RoutesFilter filter) {
        super(filter);
    }


    @Override
    protected void customizeCellRenderer(JTable table, Object value,
                                         boolean selected, boolean hasFocus, int row, int column) {
        // Value can be null in older JDKs (below 1.7, I suppose).
        // Info: http://stackoverflow.com/questions/3054775/jtable-strange-behavior-from-getaccessiblechild-method-resulting-in-null-point
        if (value == null)
            return;

        Route route = (Route) value;

        int modelCol = table.convertColumnIndexToModel(column);
        switch (modelCol) {
            case RouteTableModel.COL_ACTION:
                renderRouteAction(route);
                break;

            default:
                renderRoutePath(route);
        }
    }


    private void renderRouteAction(Route route) {
        String tooltipText = null;
        RailsActionInfo action = null;

        // Set icons and hints
        if (route instanceof SimpleRoute) {
            action = ((SimpleRoute)route).getActionInfo();

            if (action.getPsiMethod() == null)
                tooltipText = action.getPsiClass() != null ?
                        "Cannot find action declaration" :
                        "Cannot find controller declaration";
        }

        setIcon(route.getActionIcon());
        setToolTipText(tooltipText);

        // Now append text taking into account colors and highlighting.
        List<TextChunk> chunks = TextChunkHighlighter.highlight(
                route.getActionChunks(), getFilter().getPathFilter());

        for(TextChunk chunk: chunks) {
            SimpleTextAttributes textAttrs;

            if (action != null &&
                    ((chunk.getType() == RouteActionChunk.CONTAINER &&
                    action.getPsiClass() == null) ||
                    (chunk.getType() == RouteActionChunk.ACTION &&
                            action.getPsiMethod() == null))) {

                textAttrs = chunk.isHighlighted() ?
                        RailwaysColors.DISABLED_ITEM_HL_ATTR :
                        RailwaysColors.DISABLED_ITEM_ATTR;
            } else
                textAttrs = chunk.getTextAttrs();

            append(chunk.getText(), textAttrs);
        }
    }


    private void renderRoutePath(Route route) {
        List<TextChunk> chunks = TextChunkHighlighter.highlight(
                route.getPathChunks(), getFilter().getPathFilter());

        for(TextChunk chunk: chunks)
            append(chunk.getText(), chunk.getTextAttrs());

        setToolTipText(null);
        setIcon(route.getRequestMethod().getIcon());
    }
}
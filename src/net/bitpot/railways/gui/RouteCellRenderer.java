package net.bitpot.railways.gui;

import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.models.RailsActionInfo;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteTableModel;
import net.bitpot.railways.models.RoutesFilter;
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


    // TODO: cleanup and refactor (move attribute calculation to TextChunk classes?)
    private void renderRouteAction(Route route) {
        Icon icon;

        SimpleTextAttributes textAttrs;
        String tooltipText = null;

        RailsActionInfo action = route.getActionInfo();

        List<TextChunk> chunks = RouteActionParser.parse(route.getActionText());

        if (route.getType() == Route.MOUNTED) {
            icon = RailwaysIcons.RACK_APPLICATION;

        } else if (action.getPsiMethod() != null) {
            icon = action.getIcon();

        } else if (action.getPsiClass() != null) {
            icon = RailwaysIcons.CONTROLLER_NODE;
            tooltipText = "Cannot find action declaration";
        } else {
            icon = RailwaysIcons.UNKNOWN;
            tooltipText = "Cannot find controller declaration";
        }

        setIcon(icon);
        setToolTipText(tooltipText);

        List<TextChunk> hlChunks = TextChunkHighlighter.highlight(chunks,
                getFilter().getPathFilter());

        for(TextChunk chunk: hlChunks) {
            textAttrs = chunk.isHighlighted() ?
                    RailwaysColors.REGULAR_HL_ATTR :
                    SimpleTextAttributes.REGULAR_ATTRIBUTES;

            if (chunk.getType() == RouteActionChunk.CONTAINER) {
                if (action.getPsiClass() == null)
                    textAttrs = chunk.isHighlighted() ?
                            RailwaysColors.DISABLED_ITEM_HL_ATTR :
                            RailwaysColors.DISABLED_ITEM_ATTR;

            } else if (chunk.getType() == RouteActionChunk.ACTION) {
                if (action.getPsiMethod() == null)
                    textAttrs = chunk.isHighlighted() ?
                            RailwaysColors.DISABLED_ITEM_HL_ATTR :
                            RailwaysColors.DISABLED_ITEM_ATTR;
                else
                    textAttrs = chunk.isHighlighted() ?
                            RailwaysColors.METHOD_HL_ATTR :
                            RailwaysColors.METHOD_ATTR;
            }

            append(chunk.getText(), textAttrs);
        }
    }


    private void renderRoutePath(Route route) {
        List<TextChunk> chunks = TextChunkHighlighter.highlight(
                RoutePathParser.parse(route.getPath()), getFilter().getPathFilter());

        for(TextChunk chunk: chunks)
            append(chunk.getText(), getRoutePathTextAttrs(chunk));

        setToolTipText(null);
        setIcon(route.getIcon());
    }


    private SimpleTextAttributes getRoutePathTextAttrs(TextChunk chunk) {

        switch(chunk.getType()) {
            case RoutePathChunk.PARAMETER:
                return chunk.isHighlighted() ?
                        RailwaysColors.PARAM_TOKEN_HL_ATTR :
                        RailwaysColors.PARAM_TOKEN_ATTR;

            case RoutePathChunk.OPTIONAL:
                return chunk.isHighlighted() ?
                        RailwaysColors.OPTIONAL_TOKEN_HL_ATTR :
                        RailwaysColors.OPTIONAL_TOKEN_ATTR;

            default:
                return chunk.isHighlighted() ?
                        RailwaysColors.REGULAR_HL_ATTR :
                        SimpleTextAttributes.REGULAR_ATTRIBUTES;
        }
    }
}
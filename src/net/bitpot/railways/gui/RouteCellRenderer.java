package net.bitpot.railways.gui;

import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteTableModel;
import net.bitpot.railways.models.RoutesFilter;
import net.bitpot.railways.parser.route.RouteParser;
import net.bitpot.railways.parser.route.RouteToken;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

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
        boolean isError = !route.isActionAvailable();

        appendHighlighted(route.getActionText(),
                getFilter().getPathFilter(), isError);
    }


    private void renderRoutePath(Route route) {
        RouteToken[] tokens = RouteParser.parseAndHighlight(route.getPath(),
                getFilter().getPathFilter());

        for(RouteToken token: tokens)
            append(token.text, getTextAttributes(token));

        setIcon(route.getIcon());
    }


    private SimpleTextAttributes getTextAttributes(RouteToken token) {

        switch(token.tokenType) {
            case RouteToken.PARAMETER:
                return token.isHighlighted ?
                        RailwaysColors.PARAM_TOKEN_HL_ATTR :
                        RailwaysColors.PARAM_TOKEN_ATTR;

            case RouteToken.OPTIONAL:
                return token.isHighlighted ?
                        RailwaysColors.OPTIONAL_TOKEN_HL_ATTR :
                        RailwaysColors.OPTIONAL_TOKEN_ATTR;

            default:
                return token.isHighlighted ?
                        RailwaysColors.REGULAR_HL_ATTR :
                        SimpleTextAttributes.REGULAR_ATTRIBUTES;
        }
    }
}
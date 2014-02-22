package net.bitpot.railways.gui;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.parser.route.RouteParser;
import net.bitpot.railways.parser.route.RouteToken;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteCellRenderer extends ColoredTableCellRenderer {

    private static final SimpleTextAttributes QUERY_PARAM_ATTR =
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.MAGENTA);

    private static final SimpleTextAttributes OPTIONAL_TOKEN_ATTR =
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY);


    @Override
    protected void customizeCellRenderer(JTable table, Object value,
                                         boolean selected, boolean hasFocus, int row, int column) {
        Route route = (Route) value;

        RouteToken[] tokens = RouteParser.parseRoute(route.getPath());

        for(RouteToken token: tokens) {
            append(token.text, getTextAttributes(token));
        }


        setIcon(((Route) value).getIcon());
    }

    private SimpleTextAttributes getTextAttributes(RouteToken token) {
        switch(token.tokenType) {
            case RouteToken.PARAMETER:
                return QUERY_PARAM_ATTR;
            case RouteToken.OPTIONAL:
                return OPTIONAL_TOKEN_ATTR;

            default:
                return SimpleTextAttributes.REGULAR_ATTRIBUTES;
        }
    }
}
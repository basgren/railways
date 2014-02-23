package net.bitpot.railways.gui;

import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RoutesFilter;
import net.bitpot.railways.parser.route.RouteParser;
import net.bitpot.railways.parser.route.RouteToken;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteCellRenderer extends HighlightingTableCellRenderer {


    private static final SimpleTextAttributes PARAM_TOKEN_ATTR =
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, PARAM_TOKEN_COLOR);

    private static final SimpleTextAttributes PARAM_TOKEN_HL_ATTR =
            new SimpleTextAttributes(HIGHLIGHT_BG_COLOR, PARAM_TOKEN_COLOR,
                    null, SimpleTextAttributes.STYLE_PLAIN);

    private static final SimpleTextAttributes OPTIONAL_TOKEN_ATTR =
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, OPTIONAL_TOKEN_COLOR);

    private static final SimpleTextAttributes OPTIONAL_TOKEN_HL_ATTR =
            new SimpleTextAttributes(HIGHLIGHT_BG_COLOR, OPTIONAL_TOKEN_COLOR,
                    null, SimpleTextAttributes.STYLE_PLAIN);


    private RoutesFilter filter;


    public RouteCellRenderer(@NotNull RoutesFilter filter) {
        this.filter = filter;
    }


    @Override
    protected void customizeCellRenderer(JTable table, Object value,
                                         boolean selected, boolean hasFocus, int row, int column) {
        Route route = (Route) value;

        RouteToken[] tokens = RouteParser.parseAndHighlight(route.getPath(), filter.getPathFilter());

        for(RouteToken token: tokens)
            append(token.text, getTextAttributes(token));

        setIcon(((Route) value).getIcon());
    }


    private SimpleTextAttributes getTextAttributes(RouteToken token) {

        switch(token.tokenType) {
            case RouteToken.PARAMETER:
                return token.isHighlighted ? PARAM_TOKEN_HL_ATTR : PARAM_TOKEN_ATTR;

            case RouteToken.OPTIONAL:
                return token.isHighlighted ? OPTIONAL_TOKEN_HL_ATTR : OPTIONAL_TOKEN_ATTR;

            default:
                return token.isHighlighted ? REGULAR_HL_ATTR : SimpleTextAttributes.REGULAR_ATTRIBUTES;
        }
    }
}
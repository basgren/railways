package net.bitpot.railways.gui;

import com.intellij.icons.AllIcons;
import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteTableModel;
import net.bitpot.railways.models.RoutesFilter;
import net.bitpot.railways.parser.route.RouteParser;
import net.bitpot.railways.parser.route.RouteToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.Visibility;

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
        Icon icon = null;

        // TODO: remove isActionAvailable, as it's unavailable when action visibility == null
        SimpleTextAttributes textAttrs;
        String tooltipText = null;

        if (route.getParentEngine() == null) {
            if (route.isActionAvailable()) {
                textAttrs = SimpleTextAttributes.REGULAR_ATTRIBUTES;
            } else {
                textAttrs = RailwaysColors.MISSING_ACTION_ATTR;
                tooltipText = "Method not found";
            }
        } else
            textAttrs = RailwaysColors.DISABLED_ITEM_ATTR;

        setToolTipText(tooltipText);


        appendHighlighted(route.getActionText(),
                getFilter().getPathFilter(), textAttrs);


        if (route.getType() == Route.MOUNTED) {
            icon = RailwaysIcons.RACK_APPLICATION;
        } else if (route.getParentEngine() != null) {
            icon = RailwaysIcons.UNKNOWN;
        } else {
            Visibility vis = route.getActionVisibility();
            if (vis == null)
                icon = AllIcons.General.Error;
            else {
                switch (vis) {
                    case PRIVATE: icon = AllIcons.Nodes.C_private; break;
                    case PROTECTED: icon = AllIcons.Nodes.C_protected; break;
                    case PUBLIC: icon = AllIcons.Nodes.C_public; break;
                }
            }
        }

        setIcon(icon);
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
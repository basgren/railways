package net.bitpot.railways.gui.tree;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import net.bitpot.railways.gui.RailwaysColors;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteNode;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 31.08.14.
 */
public class RouteTreeCellRenderer extends ColoredTreeCellRenderer {
    /**
     * This method is invoked only for customization of component.
     * All component attributes are cleared when this method is being invoked.
     */
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // Value can be null in older JDKs (below 1.7, I suppose).
        // Info: http://stackoverflow.com/questions/3054775/jtable-strange-behavior-from-getaccessiblechild-method-resulting-in-null-point
        if (value == null)
            return;

        RouteNode node = (RouteNode) value;

        if (node.isLeaf()) {
            Route route = node.getRoute();

            // When there are no routes, root node can be a leaf
            if (route == null)
                return;

            append(RailwaysUtils.trimRequestFormat(node.getTitle()));
            String text = route.getActionTitle();

            if (text != null)
                append("  " + text, RailwaysColors.CONTROLLER_METHOD_ATTR);

            setIcon(route.getRequestMethod().getIcon());
        } else {
            setIcon(RailwaysIcons.ROUTE_PARENT);
            append(node.getTitle());
        }

        SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, true, selected);
    }
}

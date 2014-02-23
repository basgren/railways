package net.bitpot.railways.gui;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import net.bitpot.railways.models.RoutesFilter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 */
public class RouteTableCellRenderer extends ColoredTableCellRenderer {

    // Contains formatted text.
    private RoutesFilter filter;


    public RouteTableCellRenderer(@NotNull RoutesFilter filter) {
        this.filter = filter;
    }


    @Override
    protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        String text = value.toString();

        if (selected) {
            append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setBackground(UIUtil.getTableSelectionBackground());
            setForeground(UIUtil.getTableSelectionForeground());
        } else {
            setBackground(UIUtil.getTableBackground());
            setForeground(UIUtil.getTableForeground());
            appendHighlighted(text, filter.getPathFilter());
        }
    }


    private void appendHighlighted(String value, String highlight) {
        if (highlight.equals("")) {
            append(value, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            return;
        }

        int fromIndex = 0;
        int pos;
        String lowCaseValue = value.toLowerCase();

        while ((pos = lowCaseValue.indexOf(highlight, fromIndex)) >= 0) {
            append(value.substring(fromIndex, pos), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            // Highlighted part must be taken from the value as its character
            // case can differ from highlight parameter.
            append(value.substring(pos, pos + highlight.length()), RailwaysColors.REGULAR_HL_ATTR);

            fromIndex = pos + highlight.length();
        }

        append(value.substring(fromIndex), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
}

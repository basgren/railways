package net.bitpot.railways.gui.table;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import net.bitpot.railways.gui.RailwaysColors;
import net.bitpot.railways.models.RoutesFilter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 *
 *
 * @author Basil Gren
 *         on 02.11.14.
 */
public class FilterHighlightRenderer extends ColoredTableCellRenderer {

    // Contains formatted text.
    private RoutesFilter filter;


    public FilterHighlightRenderer(@NotNull RoutesFilter filter) {
        this.filter = filter;
    }


    protected RoutesFilter getFilter() {
        return filter;
    }


    @Override
    protected void customizeCellRenderer(JTable table, Object value,
                                         boolean selected, boolean hasFocus, int row, int column) {
        // Value can be null in older JDKs (below 1.7, I suppose).
        // Info: http://stackoverflow.com/questions/3054775/jtable-strange-behavior-from-getaccessiblechild-method-resulting-in-null-point
        if (value == null)
            return;

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


    /**
     * Appends passed string, highlighting passed substring.
     *
     * @param value Value to be displayed.
     * @param highlight Substring of value to be highlighted.
     */
    protected void appendHighlighted(String value, String highlight) {
        appendHighlighted(value, highlight,
                SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }


    /**
     * Appends passed string, highlighting passed substring.
     *
     * @param value Value to be displayed.
     * @param highlight Substring of value to be highlighted.
     * @param textAttrs Attributes of not highlighted text.
     */
    protected void appendHighlighted(String value, String highlight,
                                     SimpleTextAttributes textAttrs) {
        if (highlight.equals("")) {
            append(value, textAttrs);
            return;
        }

        int fromIndex = 0;
        int pos;
        String lowCaseValue = value.toLowerCase();

        while ((pos = lowCaseValue.indexOf(highlight, fromIndex)) >= 0) {
            append(value.substring(fromIndex, pos), textAttrs);

            // Highlighted part must be taken from the value as its character
            // case can differ from highlight parameter.
            append(value.substring(pos, pos + highlight.length()),
                    RailwaysColors.REGULAR_HL_ATTR);

            fromIndex = pos + highlight.length();
        }

        append(value.substring(fromIndex), textAttrs);
    }
}

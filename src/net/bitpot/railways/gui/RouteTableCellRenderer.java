package net.bitpot.railways.gui;

import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import net.bitpot.railways.models.RoutesFilter;
import net.bitpot.railways.models.Route;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 *
 */
public class RouteTableCellRenderer implements TableCellRenderer {
    private static final SimpleTextAttributes HIGHLIGHTED_TEXT_ATTR = new SimpleTextAttributes(Color.YELLOW, Color.BLACK, null, SimpleTextAttributes.STYLE_PLAIN);

    // Contains formatted text.
    private SimpleColoredComponent cellRenderer;
    private RoutesFilter filter;


    public RouteTableCellRenderer(@NotNull RoutesFilter filter) {
        this.filter = filter;

        cellRenderer = new SimpleColoredComponent();
    }


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String text;
        cellRenderer.clear();

        // Value can be null. We should take it into account.
        // Proof: http://stackoverflow.com/questions/3054775/jtable-strange-behavior-from-getaccessiblechild-method-resulting-in-null-point
        if (value == null)
            return cellRenderer;

        if (value instanceof Route) {
            text = ((Route) value).getPath();
            cellRenderer.setIcon(((Route) value).getIcon());
        } else {
            text = value.toString();
            cellRenderer.setIcon(null);
        }

        if (isSelected) {
            /*
                IMPORTANT!!! In RubyMine 6.0 developers changed API for SimpleColoredComponent.append method:
                was

                     public final void append(@NotNull String fragment)

                became in RubyMine 6.0:

                     public final SimpleColoredComponent append(@NotNull String fragment)

                so using the method with one argument causes NoMethodError on newer RM. To fix this problem
                we should use different method, which wasn't changed, so it doesn't cause errors:

                     public final void append(@NotNull String fragment, @NotNull SimpleTextAttributes attributes)
            */
            cellRenderer.append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            cellRenderer.setBackground(UIUtil.getTableSelectionBackground());
            cellRenderer.setForeground(UIUtil.getTableSelectionForeground());
        } else {
            cellRenderer.setBackground(UIUtil.getTableBackground());
            cellRenderer.setForeground(UIUtil.getTableForeground());
            appendHighlighted(text, filter.getPathFilter());
        }


        return cellRenderer;
    }


    private void appendHighlighted(String value, String highlight) {
        if (highlight.equals("")) {
            cellRenderer.append(value, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            return;
        }

        int fromIndex = 0;
        int pos;
        String lowCaseValue = value.toLowerCase();

        while ((pos = lowCaseValue.indexOf(highlight, fromIndex)) >= 0) {
            cellRenderer.append(value.substring(fromIndex, pos), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            // Highlighted part must be taken from the value as its character case can differ from highlight parameter.
            cellRenderer.append(value.substring(pos, pos + highlight.length()), HIGHLIGHTED_TEXT_ATTR);

            fromIndex = pos + highlight.length();
        }

        cellRenderer.append(value.substring(fromIndex), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

}

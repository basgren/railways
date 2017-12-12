package net.bitpot.railways.models;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that contains all parameters of routes filtration.
 */
public class RoutesFilter {
    private RouteTableModel tableModel;
    private String filterText = "";
    @NotNull private Pattern filterPattern = Pattern.compile("");
    private boolean mountedRoutesVisible = true;

    private static Pattern buildPattern(String filterText) {
        // Escape regex special chars except "*"
        filterText = filterText.replaceAll("[\\\\.\\[\\]{}()+\\-?^$|]", "\\\\$0");
        String regex = filterText.replace("*", ".*?");
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public RoutesFilter(@NotNull RouteTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void setFilterText(String filterText) {
        filterText = filterText.toLowerCase();
        if (!this.filterText.equals(filterText)) {
            this.filterText = filterText;
            filterPattern = buildPattern(filterText);
            tableModel.filterChanged();
        }
    }

    public boolean isMountedRoutesVisible() {
        return mountedRoutesVisible;
    }

    public void setMountedRoutesVisible(boolean value) {
        if (mountedRoutesVisible != value) {
            mountedRoutesVisible = value;
            tableModel.filterChanged();
        }
    }

    /**
     * Returns true if any filter is set and should be applied.
     * Actually checks if filter values are set to defaults or not.
     *
     * @return True when any filter is active, false otherwise.
     */
    boolean isFilterActive() {
        return !filterText.equals("") || !mountedRoutesVisible;
    }

    /**
     * Checks whether specified route matches current filter.
     *
     * @param route Route to be matched against current filter.
     * @return True if route matches filter, false otherwise.
     */
    boolean match(Route route) {
        if (!mountedRoutesVisible && route.getParentEngine() != null)
            return false;

        return filterPattern.matcher(route.getPath()).find() ||
                filterPattern.matcher(route.getActionTitle()).find() ||
                filterPattern.matcher(route.getRouteName()).find();
    }

    public String findMatchedString(String text) {
        Matcher matcher = filterPattern.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

}

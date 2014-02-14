package net.bitpot.railways.routesView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import net.bitpot.railways.utils.RailwaysUtils;

/**
 * Defines a condition when Railways tool window is showed.
 */
public class RoutesViewToolWindowCondition implements Condition {
    /**
     * Method checks if opened project is a Ruby On Rails project and if yes,
     * it will allow to show Routes tool window.
     *
     * @param o Project object.
     * @return True if tool window should be showed, false if it should be hidden.
     */
    @Override
    public boolean value(Object o) {
        return RailwaysUtils.hasRailsModules((Project) o);
    }
}
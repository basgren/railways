package net.bitpot.railways.models.requestMethods;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface RequestMethod {

    /**
     * Returns the icon used for showing routes of the type.
     *
     * @return The icon instance, or null if no icon should be shown.
     */
    Icon getIcon();

    /**
     * Returns the name of the route type. The name must be unique among all route types.
     *
     * @return The route type name.
     */
    @NotNull
    @NonNls
    String getName();

}

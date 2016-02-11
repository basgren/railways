package net.bitpot.railways.models.requestMethods;

import net.bitpot.railways.ui.RailwaysIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Basil Gren
 */
public class AnyRequestMethod extends RequestMethod {
    @Override
    public Icon getIcon() {
        return RailwaysIcons.HTTP_METHOD_ANY;
    }


    @NotNull
    @Override
    public String getName() {
        return "Any";
    }
}
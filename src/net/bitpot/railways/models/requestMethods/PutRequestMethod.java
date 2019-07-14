package net.bitpot.railways.models.requestMethods;

import net.bitpot.railways.gui.RailwaysIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Basil Gren
 */
public class PutRequestMethod implements RequestMethod {
    @Override
    public Icon getIcon() {
        return RailwaysIcons.HTTP_METHOD_PUT;
    }


    @NotNull
    @Override
    public String getName() {
        return "PUT";
    }
}

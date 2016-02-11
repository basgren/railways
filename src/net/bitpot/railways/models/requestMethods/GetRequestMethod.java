package net.bitpot.railways.models.requestMethods;

import net.bitpot.railways.ui.RailwaysIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Basil Gren
 */
public class GetRequestMethod extends RequestMethod {
    @Override
    public Icon getIcon() {
        return RailwaysIcons.HTTP_METHOD_GET;
    }


    @NotNull
    @Override
    public String getName() {
        return "GET";
    }
}

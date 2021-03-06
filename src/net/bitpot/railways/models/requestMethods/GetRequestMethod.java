package net.bitpot.railways.models.requestMethods;

import net.bitpot.railways.gui.RailwaysIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class GetRequestMethod implements RequestMethod {
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

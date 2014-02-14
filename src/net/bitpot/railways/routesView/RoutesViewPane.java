package net.bitpot.railways.routesView;

import com.intellij.openapi.module.Module;
import com.intellij.ui.content.Content;
import net.bitpot.railways.gui.RailwaysIcons;

import javax.swing.*;

/**
 * Stores data for a module panel in Routes tool window.
 */
public class RoutesViewPane {

    private final Module myModule;
    private Content myContent;
    private RoutesManager myRoutesManager;


    /**
     * Creates a separate panel for Rails module.
     *
     * @param module  Module that contains Rails application.
     */
    public RoutesViewPane(Module module) {
        myModule = module;

        myRoutesManager = new RoutesManager(module);
        myRoutesManager.initRouteList();
    }


    public String getTitle() {
        return myModule.getName();
    }


    public void setContent(Content content) {
        myContent = content;
    }


    public Content getContent() {
        return myContent;
    }


    public Module getModule() {
        return myModule;
    }


    public Icon getIcon() {
        return RailwaysIcons.RUBY_ON_RAILS;
    }


    public RoutesManager getRoutesManager() {
        return myRoutesManager;
    }
}
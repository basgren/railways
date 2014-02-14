package net.bitpot.railways.routesView;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import net.bitpot.railways.routesView.RoutesManager;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.RouteList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Stores data for a module panel in Routes tool window.
 */
public class RoutesViewPane {

    private final Project myProject;
    private final Module myModule;

    private Content myContent;

    private RoutesManager myRoutesManager;


    /**
     * Creates a separate panel for Rails module.
     *
     * @param project Project
     * @param module  Module that contains Rails application.
     */
    public RoutesViewPane(Project project, Module module) {
        myProject = project;
        myModule = module;

        myRoutesManager = new RoutesManager(project, module);
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

    @Nullable
    public RouteList getRoutesList() {
        return myRoutesManager.getRouteList();
    }
}
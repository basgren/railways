package net.bitpot.railways.routesView.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import net.bitpot.railways.gui.RailwaysIcons;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 08.02.14.
 */
public class RoutesViewPane {

    private final Project myProject;
    private final Module myModule;

    private Content myContent;


    /**
     * Creates a separate panel for Rails module.
     *
     * @param project Project
     * @param module  Module that contains Rails application.
     */
    public RoutesViewPane(Project project, Module module) {
        myProject = project;
        myModule = module;
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
}
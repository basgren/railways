package net.bitpot.railways;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Railways application component.
 */
public class RailwaysApplicationComp implements ApplicationComponent {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(RailwaysApplicationComp.class.getName());

    private static RailwaysApplicationComp instance = null;


    public RailwaysApplicationComp() {
    }


    public void initComponent() {
        // Deny two instances of the plugin. Otherwise we will get a mess-up in
        // editor when using Code Injector.
        if (instance != null)
            throw new RuntimeException("Railways plugin already initialized.");

        // Save plugin object for accessing in static methods.
        instance = this;
    }


    public void disposeComponent() {
        // Do nothing now
    }


    @NotNull
    public String getComponentName() {
        return "Railways.ApplicationComponent";
    }
}

package net.bitpot.railways.models.routes;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 14.12.2014.
 */
public class RedirectRoute extends Route {

    @NotNull
    private String redirectPath;

    public RedirectRoute(@Nullable Module module, RequestMethod requestMethod,
                         String path, String name, @Nullable String redirectPath) {
        super(module, requestMethod, path, name);

        this.redirectPath = redirectPath == null ? "" : redirectPath;
    }


    /**
     * Returns displayable text for route action in short format. Short format
     * is used in routes table.
     *
     * @return Displayable text for route action, ex. "users#create"
     */
    @Override
    public String getActionTitle() {
        return redirectPath.isEmpty() ? "[redirect]" : redirectPath;
    }


    /**
     * Returns displayable text for route action.
     *
     * @return Displayable text for route action, ex. "UsersController#create"
     */
    @Override
    public String getQualifiedActionTitle() {
        return redirectPath.isEmpty() ?
                "[runtime defined redirect]" :
                "redirect to " + redirectPath;
    }


    @Override
    public Icon getActionIcon() {
        return RailwaysIcons.NODE_REDIRECT;
    }
}

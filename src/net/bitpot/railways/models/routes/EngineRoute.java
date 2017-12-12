package net.bitpot.railways.models.routes;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.utils.RailwaysPsiUtils;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import javax.swing.*;

/**
 * @author Basil Gren
 *         on 14.12.2014.
 */
public class EngineRoute extends Route {

    private String engineClass;

    public EngineRoute(Module myModule, RequestMethod requestMethod,
                       String routePath, String routeName, String engineClass) {
        super(myModule, requestMethod, routePath, routeName);

        this.engineClass = engineClass;
    }


    @Override
    public String getQualifiedActionTitle() {
        return engineClass;
    }


    @Override
    public Icon getActionIcon() {
        return RailwaysIcons.NODE_MOUNTED_ENGINE;
    }


    @Override
    public void navigate(boolean requestFocus) {
        RContainer container = RailwaysPsiUtils.
                findClassOrModule(engineClass, getModule().getProject());

        if (container != null)
            container.navigate(requestFocus);
    }
}

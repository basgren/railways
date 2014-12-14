package net.bitpot.railways.models.routes;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;

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
}

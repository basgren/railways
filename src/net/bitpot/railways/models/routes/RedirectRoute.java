package net.bitpot.railways.models.routes;

import com.intellij.openapi.module.Module;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import org.jetbrains.annotations.Nullable;

/**
 * @author Basil Gren
 *         on 14.12.2014.
 */
public class RedirectRoute extends Route {

    private String redirectPath;

    public RedirectRoute(@Nullable Module module, RequestMethod requestMethod,
                         String path, String name, String redirectPath) {
        super(module, requestMethod, path, name);

        this.redirectPath = redirectPath;
    }
}

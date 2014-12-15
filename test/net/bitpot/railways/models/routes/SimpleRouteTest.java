package net.bitpot.railways.models.routes;

import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import org.junit.Test;

/**
 * @author Basil Gren
 *         on 16.12.2014.
 */
public class SimpleRouteTest {

    @Test
    public void testSimpleRedirectRoute()
    {
        Route route = new SimpleRoute(null, RequestMethod.GET, "/test",
                "clients", "clients", "index");

        // How to test it? SimpleRoute icons relies on Psi tree.
        //assertEquals(RailwaysIcons.ROUTE_ACTION_NODE, route.getActionIcon());
    }

}

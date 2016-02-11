package net.bitpot.railways.models.routes;

import net.bitpot.railways.ui.RailwaysIcons;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Basil Gren
 *         on 16.12.2014.
 */
public class EngineRouteTest {

    @Test
    public void testSimpleRedirectRoute()
    {
        Route route = new EngineRoute(null, RequestMethod.GET, "/test",
                "", "Test::Engine");

        assertEquals(RailwaysIcons.NODE_MOUNTED_ENGINE, route.getActionIcon());
    }

}

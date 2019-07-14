package net.bitpot.railways.models.routes;

import net.bitpot.railways.gui.RailwaysIcons;
import net.bitpot.railways.models.RequestMethods;
import net.bitpot.railways.models.Route;
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
        Route route = new EngineRoute(null, RequestMethods.GET, "/test",
                "", "Test::Engine");

        assertEquals(RailwaysIcons.NODE_MOUNTED_ENGINE, route.getActionIcon());
    }

}

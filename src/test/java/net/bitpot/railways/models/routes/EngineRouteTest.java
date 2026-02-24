package net.bitpot.railways.models.routes;

import net.bitpot.railways.models.RequestMethods;
import net.bitpot.railways.models.Route;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EngineRouteTest {

    @Test
    public void testEngineRoute()
    {
        Route route = new EngineRoute(null, RequestMethods.GET, "/test",
                "", "Test::Engine");

        assertEquals("Test::Engine", route.getActionTitle());
        assertEquals("/test", route.getPath());
        assertEquals(RequestMethods.GET, route.getRequestMethod());
    }

}

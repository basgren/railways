package net.bitpot.railways.models.routes;

import net.bitpot.railways.models.RequestMethods;
import net.bitpot.railways.models.Route;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RedirectRouteTest {

    @Test
    public void testSimpleRedirectRoute()
    {
        Route route = new RedirectRoute(null, RequestMethods.GET, "/test",
                "redirect", "/test_redirect");

        assertEquals("redirect to /test_redirect", route.getQualifiedActionTitle());
        assertEquals("/test_redirect", route.getActionTitle());
        assertEquals("/test", route.getPath());
    }


    @Test
    public void testNullRedirectPath()
    {
        Route route = new RedirectRoute(null, RequestMethods.GET, "/test",
                "redirect", null);

        assertEquals("[redirect]", route.getActionTitle());
        assertEquals("[runtime defined redirect]", route.getQualifiedActionTitle());
    }

}

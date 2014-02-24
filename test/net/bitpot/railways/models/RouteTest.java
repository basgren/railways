package net.bitpot.railways.models;

import net.bitpot.railways.models.routes.RequestType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Testing table model filtration.
 */
public class RouteTest
{
    Route route;


    @Before
    public void setUp() throws Exception
    {
        route = new Route(null, RequestType.GET,
                "/users", "users", "index", "users");
    }


    @Test
    public void testControllerMethodName()
    {
        route.setController("test_users");

        assertEquals(route.getControllerMethodName(), "TestUsersController#index");
    }
}
package net.bitpot.railways.models;

import net.bitpot.railways.models.routes.RequestType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing table model filtration.
 */
public class RouteTest extends Assert
{
    Route route;


    @BeforeMethod
    public void setUp() throws Exception
    {
        route = new Route();
        route.setRoute("users", "/users", RequestType.GET, "users", "index");
    }

    @Test
    public void testControllerMethodName()
    {
        route.setController("test_users");

        assertEquals(route.getControllerMethodName(), "TestUsersController#index");
    }

}

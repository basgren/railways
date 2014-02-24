package net.bitpot.railways.rails;

import net.bitpot.railways.models.Route;

import static org.junit.Assert.assertEquals;

/**
 * @author Basil Gren
 *         on 25.02.14.
 */
public class TestUtils {

    public static void assertRouteEquals(Route expected, Route actual) {
        assertEquals("Route names are equal",
                expected.getRouteName(), actual.getRouteName());

        assertEquals("Route types are equal",
                expected.getType(), actual.getType());

        assertEquals("Request methods are equal",
                expected.getRequestType(), actual.getRequestType());

        assertEquals("Route paths are equal",
                expected.getPath(), actual.getPath());

        assertEquals("Route controllers are equal",
                expected.getController(), actual.getController());

        assertEquals("Route actions are equal",
                expected.getAction(), actual.getAction());
    }

}

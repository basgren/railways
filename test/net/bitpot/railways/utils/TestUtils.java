package net.bitpot.railways.utils;

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
                expected.getClass(), actual.getClass());

        assertEquals("Request methods are equal",
                expected.getRequestMethod(), actual.getRequestMethod());

        assertEquals("Route paths are equal",
                expected.getPath(), actual.getPath());

        assertEquals("Route actions titles are equal",
                expected.getQualifiedActionTitle(), actual.getQualifiedActionTitle());
    }

}

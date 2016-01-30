package net.bitpot.railways.utils;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * @author Basil Gren
 *         on 23.11.2014.
 */
public class RailwaysUtilsTest {


    @Test
    public void test_getRubyClassPathByShortName() {
        String[] result;

        result = RailwaysPsiUtils.getControllerClassPathByShortName("users");
        assertArrayEquals(new String[] {"UsersController"}, result);

        result = RailwaysPsiUtils.getControllerClassPathByShortName("rails_admin/main");
        assertArrayEquals(new String[] {"RailsAdmin", "MainController"}, result);

        result = RailwaysPsiUtils.getControllerClassPathByShortName("api/users/search");
        assertArrayEquals(new String[] {"Api", "Users", "SearchController"}, result);
    }

    @Test
    public void test_stripRequestFormat() {
        String result = RailwaysUtils.stripRequestFormat("/books/:id(.:format)");
        assertEquals("/books/:id", result);

        result = RailwaysUtils.stripRequestFormat("/books");
        assertEquals("/books", result);
    }

}

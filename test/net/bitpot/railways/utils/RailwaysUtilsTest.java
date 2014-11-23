package net.bitpot.railways.utils;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


/**
 * @author Basil Gren
 *         on 23.11.2014.
 */
public class RailwaysUtilsTest {


    @Test
    public void test_getRubyClassPathByShortName() {
        String[] result;

        result = RailwaysUtils.getControllerClassPathByShortName("users");
        assertArrayEquals(new String[] {"UsersController"}, result);

        result = RailwaysUtils.getControllerClassPathByShortName("rails_admin/main");
        assertArrayEquals(new String[] {"RailsAdmin", "MainController"}, result);

        result = RailwaysUtils.getControllerClassPathByShortName("api/users/search");
        assertArrayEquals(new String[] {"Api", "Users", "SearchController"}, result);
    }


    @Test
    public void test_isClassMatchesPath() {
        // TODO: implement tests.
    }

}

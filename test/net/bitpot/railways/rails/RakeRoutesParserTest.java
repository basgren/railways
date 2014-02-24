package net.bitpot.railways.rails;


import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.models.routes.RequestMethod;
import net.bitpot.railways.parser.RailsRoutesParser;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Tests for RailsRoutesParser.
 */
public class RakeRoutesParserTest
{
    RailsRoutesParser parser = null;

    @Before
    public void setUp() throws Exception
    {
        parser = new RailsRoutesParser();
    }


    @Test
    public void testParse() throws Exception
    {
        RouteList routes = parser.parseFile("test/data/parserTest_1.txt");

        assertNotNull(routes);
        assertEquals(routes.size(), 6);
    }


    @Test
    public void testStrErrorParsing() throws IOException
    {
        String stdErr = readFile("test/data/sample_stderr.txt");

        parser.parseErrors(stdErr);
        String stack = parser.getErrorStacktrace();

        assertTrue(stack.length() > 0);
        assertFalse("Rake error wasn't cleaned from unnecessary text.",
                stack.contains("rake aborted!"));
    }


    @Test
    public void testStdErrorParserRemovesRakeMessages()
    {
        String s = "** Invoke routes (first_time)\n" +
                   "** Invoke environment (first_time)";

        parser.parseErrors(s);
        String stack = parser.getErrorStacktrace();

        assertEquals(stack.length(), 0);
    }


    @Test
    public void testRequirementsParsing()
    {
        String routeOldFormat = "mozilla_users        /mozilla_users(.:format)  {:user_agent=>/Mozilla.*/, :controller=>\"users\", :action=>\"index\"}";
        String routeNewFormat = "mozilla_users        /mozilla_users(.:format)  users#index {:user_agent=>/Mozilla.*/, :test => 34, :test_string => \"Some string\"}";

        // It's enough to check just requirements as the same strings are
        // checked in another test.
        List<Route> routeList = parser.parseLine(routeOldFormat);
        Route route = routeList.get(0);

        routeList = parser.parseLine(routeNewFormat);
        route = routeList.get(0);
    }

    @Test
    public void testConstraintsParsing()
    {
        String rails3routeConstraints = "           GET    /users/:id(.:format)                   users#show {:id=>/[A-Za-z]{3,}/}";

        // Test constraints parsing
        List<Route> routeList = parser.parseLine(rails3routeConstraints);
        Route route = routeList.get(0);

        assertEquals(route.getAction(), "show");
    }


    @Test
    public void testIsInvalidRouteLine()
    {
        String rails4routeHeader = "   Prefix Verb   URI Pattern               Controller#Action";
        String rails4routeLine = "edit_user GET    /users/:id/edit(.:format) users#edit";
        assertTrue(parser.isInvalidRouteLine(rails4routeHeader));
        assertFalse(parser.isInvalidRouteLine(rails4routeLine));
    }


    @Test
    public void testParsingMyltipleRouteTypesInASingleLine() {
        String line = "  test GET|POST /test(.:format)             clients#show  ";

        List<Route> routes = parser.parseLine(line);
        assertEquals("Resulting routes count", 2, routes.size());


        // Test first route
        Route expected = new Route(null, RequestMethod.GET,
                "/test(.:format)", "clients", "show", "test");
        Route actual = routes.get(0);

        TestUtils.assertRouteEquals(expected, actual);


        // Test second route
        expected = new Route(null, RequestMethod.POST,
                "/test(.:format)", "clients", "show", "test");
        actual = routes.get(1);

        TestUtils.assertRouteEquals(expected, actual);
    }


    private String readFile( String file ) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader (file));
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");
        String         line;

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }
}
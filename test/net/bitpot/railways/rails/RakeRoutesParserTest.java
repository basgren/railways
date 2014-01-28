package net.bitpot.railways.rails;


import net.bitpot.railways.models.routes.RequestType;
import net.bitpot.railways.parser.RailsRoutesParser;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 *
 */
public class RakeRoutesParserTest extends Assert
{
    RailsRoutesParser parser = null;


    @BeforeMethod
    public void setUp() throws Exception
    {
        parser = new RailsRoutesParser();
    }




    @DataProvider(name = "parseLineData")
    public Object[][] createParseLineData() {
        return new Object[][] {
            {"          photo_album_photos POST   /photo_albums/:photo_album_id/photos(.:format)           {:action=>\"create\", :controller=>\"photos\"}",
                    Route.DEFAULT, "photo_album_photos", RequestType.POST, "/photo_albums/:photo_album_id/photos(.:format)", "photos", "create"},

            {"             new_photo_album GET    /photo_albums/new(.:format)                              {:action=>\"new\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "new_photo_album", RequestType.GET, "/photo_albums/new(.:format)", "photo_albums", "new"},

            {"            edit_photo_album GET    /photo_albums/:id/edit(.:format)                         {:action=>\"edit\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "edit_photo_album", RequestType.GET, "/photo_albums/:id/edit(.:format)", "photo_albums", "edit"},

            {"                 photo_album GET    /photo_albums/:id(.:format)                              {:action=>\"show\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "photo_album", RequestType.GET, "/photo_albums/:id(.:format)", "photo_albums", "show"},

            {"                             PUT    /photo_albums/:id(.:format)                              {:action=>\"update\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "", RequestType.PUT, "/photo_albums/:id(.:format)", "photo_albums", "update"},

            {"                             DELETE /photo_albums/:id(.:format)                              {:action=>\"destroy\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "", RequestType.DELETE, "/photo_albums/:id(.:format)", "photo_albums", "destroy"},

            // New rails format
            {"                 photo_album GET    /photo_albums/:id(.:format)                              photo_albums#show",
                    Route.DEFAULT, "photo_album", RequestType.GET, "/photo_albums/:id(.:format)", "photo_albums", "show"},

            {"                             PUT    /photo_albums/:id(.:format)                              photo_albums#update",
                    Route.DEFAULT, "", RequestType.PUT, "/photo_albums/:id(.:format)", "photo_albums", "update"},

            {"                             DELETE /photo_albums/:id(.:format)                              photo_albums#destroy",
                    Route.DEFAULT, "", RequestType.DELETE, "/photo_albums/:id(.:format)", "photo_albums", "destroy"},

            // Test mounting rack-application
            {"    test_server        /test                     {:to=>TestServer}",
                    Route.MOUNTED, "test_server", RequestType.ANY, "/test", "TestServer", "" // No action means that it's Rack application
            },

            // Mounted rack application with namespace
            {"    test_server2        /test                     Test::Server",
                    Route.MOUNTED, "test_server2", RequestType.ANY, "/test", "Test::Server", "" // No action means that it's Rack application
            },

            // Test parsing routes with additional requirements (constraints etc.) (Old format)
            {"mozilla_users        /mozilla_users(.:format)  {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/, :controller=>\"users\", :action=>\"index\"}",
                    Route.DEFAULT, "mozilla_users", RequestType.ANY, "/mozilla_users(.:format)", "users", "index" // No action means that it's Rack application
            },

            // Test parsing routes with additional requirements (constraints etc.) (New format)
            {"mozilla_users        /mozilla_users(.:format)  users#index {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/}",
                    Route.DEFAULT, "mozilla_users", RequestType.ANY, "/mozilla_users(.:format)", "users", "index" // No action means that it's Rack application
            },

            // Test parsing of redirecting route
            {"    redirect        /redirect(.:format)                    :controller#:action",
                    Route.REDIRECT, "redirect", RequestType.ANY, "/redirect(.:format)", ":controller", ":action"},

            // Test parsing of Rails 4 PATCH method
            {"          PATCH  /users/:id(.:format)      users#update",
                    Route.DEFAULT, "", RequestType.PATCH, "/users/:id(.:format)", "users", "update"}
        };
    }



    @Test(dataProvider = "parseLineData")
    public void testParseLine(String line, int routeType, String name, RequestType rType, String path, String controller, String action)
    {
        Route r = parser.parseLine(line);
        assertNotNull(r);

        assertEquals(r.getRouteName(), name);
        assertEquals(r.getType(), routeType);
        assertEquals(r.getRequestType(), rType);
        assertEquals(r.getPath(), path);
        assertEquals(r.getController(), controller);
        assertEquals(r.getAction(), action);
    }



    @Test
    public void testParse() throws Exception
    {
        boolean result = parser.parseFile("test/data/parserTest_1.txt");
        RouteList routes = parser.getRoutes();

        assertTrue(result);
        assertEquals(routes.size(), 6);
    }


    @Test
    public void testStrErrorParsing() throws IOException
    {
        String stdErr = readFile("test/data/sample_stderr.txt");

        parser.parseErrors(stdErr);
        String stack = parser.getStacktrace();

        assertTrue(stack.length() > 0);
        assertFalse(stack.contains("rake aborted!"), "Rake error wasn't cleaned from unnecessary text.");
    }

    @Test
    public void testStdErrorParserRemovesRakeMessages()
    {
        String s = "** Invoke routes (first_time)\n" +
                   "** Invoke environment (first_time)";

        parser.parseErrors(s);
        String stack = parser.getStacktrace();

        assertEquals(stack.length(), 0);
    }


    @Test
    public void testRequirementsParsing()
    {
        String routeOldFormat = "mozilla_users        /mozilla_users(.:format)  {:user_agent=>/Mozilla.*/, :controller=>\"users\", :action=>\"index\"}";
        String routeNewFormat = "mozilla_users        /mozilla_users(.:format)  users#index {:user_agent=>/Mozilla.*/, :test => 34, :test_string => \"Some string\"}";

        // It's enough to check just requirements as the same strings are checked in another test.
        Route route = parser.parseLine(routeOldFormat);

        // Route parsing is disabled for now.
        assertFalse(route.hasRequirements());

        //assertTrue(route.hasRequirements(), "No requirements are added after parsing route of old format.");
        //assertEquals(route.getRequirements().get("user_agent"), "/Mozilla.*/");
        //assertEquals(route.getRequirements().size(), 1);



        route = parser.parseLine(routeNewFormat);

        // Route parsing is disabled for now.
        assertFalse(route.hasRequirements());


        //assertTrue(route.hasRequirements(), "No requirements are added after parsing route of new format.");
        //assertEquals(route.getRequirements().size(), 3);
        //assertEquals(route.getRequirements().get("user_agent"), "/Mozilla.*/");
        //assertEquals(route.getRequirements().get("test"), "34");
        //assertEquals(route.getRequirements().get("test_string"), "\"Some string\"");
    }

    @Test
    public void testConstraintsParsing()
    {
        String rails3routeConstraints = "           GET    /users/:id(.:format)                   users#show {:id=>/[A-Za-z]{3,}/}";

        // Test constraints parsing
        Route route = parser.parseLine(rails3routeConstraints);

        assertEquals(route.getAction(), "show");

        // This time requirements parsing is disabled. Possibly, need to create a gem that will return routes
        // more detailed format (xml).
        assertFalse(route.hasRequirements());

        //assertTrue(route.hasRequirements(), "No requirements are added after parsing route with constraints.");
        //assertEquals(route.getRequirements().get("id"), "/[A-Za-z]{3,}/");
        //assertEquals(route.getRequirements().size(), 1);
    }


    @Test
    public void test_isInvalidRouteLine()
    {
        String rails4routeHeader = "   Prefix Verb   URI Pattern               Controller#Action";
        String rails4routeLine = "edit_user GET    /users/:id/edit(.:format) users#edit";
        assertTrue(parser.isInvalidRouteLine(rails4routeHeader));
        assertFalse(parser.isInvalidRouteLine(rails4routeLine));
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

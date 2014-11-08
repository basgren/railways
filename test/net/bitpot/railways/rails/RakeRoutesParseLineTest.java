package net.bitpot.railways.rails;


import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.routes.RequestMethod;
import net.bitpot.railways.parser.RailsRoutesParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Parametrized test for testing line parsing from `rake routes` output.
 */
@RunWith(Parameterized.class)
public class RakeRoutesParseLineTest
{
    RailsRoutesParser parser = null;

    private String line, name, path, controller, action;
    private int routeType;
    private RequestMethod rType;


    public RakeRoutesParseLineTest(String line, int routeType, String name,
                                   RequestMethod rType, String path, String controller,
                                   String action) {
        this.line = line;
        this.routeType = routeType;
        this.name = name;
        this.rType = rType;
        this.path = path;
        this.controller = controller;
        this.action = action;
    }


    @Before
    public void setUp() throws Exception {
        parser = new RailsRoutesParser();
    }


    @Parameterized.Parameters
    public static Collection<Object[]> createParseLineData() {
        return Arrays.asList(new Object[][] {
            {"          photo_album_photos POST   /photo_albums/:photo_album_id/photos(.:format)           {:action=>\"create\", :controller=>\"photos\"}",
                    Route.DEFAULT, "photo_album_photos", RequestMethod.POST,
                    "/photo_albums/:photo_album_id/photos(.:format)", "photos", "create"},

            {"             new_photo_album GET    /photo_albums/new(.:format)                              {:action=>\"new\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "new_photo_album", RequestMethod.GET,
                    "/photo_albums/new(.:format)", "photo_albums", "new"},

            {"            edit_photo_album GET    /photo_albums/:id/edit(.:format)                         {:action=>\"edit\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "edit_photo_album", RequestMethod.GET,
                    "/photo_albums/:id/edit(.:format)", "photo_albums", "edit"},

            {"                 photo_album GET    /photo_albums/:id(.:format)                              {:action=>\"show\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "photo_album", RequestMethod.GET,
                    "/photo_albums/:id(.:format)", "photo_albums", "show"},

            {"                             PUT    /photo_albums/:id(.:format)                              {:action=>\"update\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "", RequestMethod.PUT,
                    "/photo_albums/:id(.:format)", "photo_albums", "update"},

            {"                             DELETE /photo_albums/:id(.:format)                              {:action=>\"destroy\", :controller=>\"photo_albums\"}",
                    Route.DEFAULT, "", RequestMethod.DELETE,
                    "/photo_albums/:id(.:format)", "photo_albums", "destroy"},

            // New rails format
            {"                 photo_album GET    /photo_albums/:id(.:format)                              photo_albums#show",
                    Route.DEFAULT, "photo_album", RequestMethod.GET,
                    "/photo_albums/:id(.:format)", "photo_albums", "show"},

            {"                             PUT    /photo_albums/:id(.:format)                              photo_albums#update",
                    Route.DEFAULT, "", RequestMethod.PUT,
                    "/photo_albums/:id(.:format)", "photo_albums", "update"},

            {"                             DELETE /photo_albums/:id(.:format)                              photo_albums#destroy",
                    Route.DEFAULT, "", RequestMethod.DELETE,
                    "/photo_albums/:id(.:format)", "photo_albums", "destroy"},

            // Test mounting rack-application
            // No action means that it's Rack application
            {"    test_server        /test                     {:to=>TestServer}",
                    Route.MOUNTED, "test_server", RequestMethod.ANY,
                    "/test", "TestServer", ""
            },

            // Mounted rack application with namespace
            // No action means that it's Rack application
            {"    test_server2        /test                     Test::Server",
                    Route.MOUNTED, "test_server2", RequestMethod.ANY,
                    "/test", "Test::Server", ""
            },

            // Test parsing routes with additional requirements (constraints etc.) (Old format)
            {"mozilla_users        /mozilla_users(.:format)  {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/, :controller=>\"users\", :action=>\"index\"}",
                    Route.DEFAULT, "mozilla_users", RequestMethod.ANY,
                    "/mozilla_users(.:format)", "users", "index"
            },

            // Test parsing routes with additional requirements (constraints etc.) (New format)
            {"mozilla_users        /mozilla_users(.:format)  users#index {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/}",
                    Route.DEFAULT, "mozilla_users", RequestMethod.ANY,
                    "/mozilla_users(.:format)", "users", "index"
            },

            // Test parsing of redirecting route
            {"    redirect        /redirect(.:format)                    :controller#:action",
                    Route.REDIRECT, "redirect", RequestMethod.ANY,
                    "/redirect(.:format)", ":controller", ":action"},

            // Test parsing of Rails 4 PATCH method
            {"          PATCH  /users/:id(.:format)      users#update",
                    Route.DEFAULT, "", RequestMethod.PATCH,
                    "/users/:id(.:format)", "users", "update"}
        });
    }


    @Test
    public void testParseLine()
    {
        List<Route> routeList = parser.parseLine(line);
        Route r = routeList.get(0);

        assertEquals(1, routeList.size());
        assertNotNull(r);

        assertEquals(r.getRouteName(), name);
        assertEquals(r.getType(), routeType);
        assertEquals(r.getRequestMethod(), rType);
        assertEquals(r.getPath(), path);
        assertEquals(r.getController(), controller);
        assertEquals(r.getAction(), action);
    }
}
package net.bitpot.railways.parser;


import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.models.routes.EngineRoute;
import net.bitpot.railways.models.routes.RedirectRoute;
import net.bitpot.railways.models.routes.SimpleRoute;
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
public class RailsRoutesParseLineTest
{
    RailsRoutesParser parser = null;

    private String line, name, path, controller, action;
    private Class routeClass;
    private RequestMethod rType;


    public RailsRoutesParseLineTest(String line, Class routeClass, String name,
                                    RequestMethod rType, String path, String controller,
                                    String action) {
        this.line = line;
        this.routeClass = routeClass;
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
                    SimpleRoute.class, "photo_album_photos", RequestMethod.POST,
                    "/photo_albums/:photo_album_id/photos(.:format)", "photos", "create"},

            {"             new_photo_album GET    /photo_albums/new(.:format)                              {:action=>\"new\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "new_photo_album", RequestMethod.GET,
                    "/photo_albums/new(.:format)", "photo_albums", "new"},

            {"            edit_photo_album GET    /photo_albums/:id/edit(.:format)                         {:action=>\"edit\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "edit_photo_album", RequestMethod.GET,
                    "/photo_albums/:id/edit(.:format)", "photo_albums", "edit"},

            {"                 photo_album GET    /photo_albums/:id(.:format)                              {:action=>\"show\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "photo_album", RequestMethod.GET,
                    "/photo_albums/:id(.:format)", "photo_albums", "show"},

            {"                             PUT    /photo_albums/:id(.:format)                              {:action=>\"update\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "", RequestMethod.PUT,
                    "/photo_albums/:id(.:format)", "photo_albums", "update"},

            {"                             DELETE /photo_albums/:id(.:format)                              {:action=>\"destroy\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "", RequestMethod.DELETE,
                    "/photo_albums/:id(.:format)", "photo_albums", "destroy"},

            // New rails format
            {"                 photo_album GET    /photo_albums/:id(.:format)                              photo_albums#show",
                    SimpleRoute.class, "photo_album", RequestMethod.GET,
                    "/photo_albums/:id(.:format)", "photo_albums", "show"},

            {"                             PUT    /photo_albums/:id(.:format)                              photo_albums#update",
                    SimpleRoute.class, "", RequestMethod.PUT,
                    "/photo_albums/:id(.:format)", "photo_albums", "update"},

            {"                             DELETE /photo_albums/:id(.:format)                              photo_albums#destroy",
                    SimpleRoute.class, "", RequestMethod.DELETE,
                    "/photo_albums/:id(.:format)", "photo_albums", "destroy"},

            // Test mounting rack-application
            // No action means that it's Rack application
            {"    test_server        /test                     {:to=>TestServer}",
                    EngineRoute.class, "test_server", RequestMethod.ANY,
                    "/test", "TestServer", ""
            },

            // Mounted rack application with namespace
            // No action means that it's Rack application
            {"    test_server2        /test                     Test::Server",
                    EngineRoute.class, "test_server2", RequestMethod.ANY,
                    "/test", "Test::Server", ""
            },

            // Test parsing routes with additional requirements (constraints etc.) (Old format)
            {"mozilla_users        /mozilla_users(.:format)  {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/, :controller=>\"users\", :action=>\"index\"}",
                    SimpleRoute.class, "mozilla_users", RequestMethod.ANY,
                    "/mozilla_users(.:format)", "users", "index"
            },

            // Test parsing routes with additional requirements (constraints etc.) (New format)
            {"mozilla_users        /mozilla_users(.:format)  users#index {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/}",
                    SimpleRoute.class, "mozilla_users", RequestMethod.ANY,
                    "/mozilla_users(.:format)", "users", "index"
            },

            // Test parsing of redirecting route
            {"    redirect        /redirect(.:format)                    :controller#:action",
                    RedirectRoute.class, "redirect", RequestMethod.ANY,
                    "/redirect(.:format)", ":controller", ":action"},

            // Test parsing of Rails 4 PATCH method
            {"          PATCH  /users/:id(.:format)      users#update",
                    SimpleRoute.class, "", RequestMethod.PATCH,
                    "/users/:id(.:format)", "users", "update"},

            // Parser should successfully parse custom action text - it's better
            // that route is added with unknown action string than ignored.
            {"    redirect_301 GET    /redirect_301(.:format)   unknown format in action field",
                    SimpleRoute.class, "redirect_301", RequestMethod.GET,
                    "/redirect_301(.:format)", "", "unknown format in action field"},

        });
    }


    @Test
    public void testParseLine()
    {
        List<Route> routeList = parser.parseLine(line);
        Route r = routeList.get(0);

        assertEquals(1, routeList.size());
        assertNotNull(r);

        assertEquals(name, r.getRouteName());
        assertEquals(routeClass, r.getClass());
        assertEquals(rType, r.getRequestMethod());
        assertEquals(path, r.getPath());
        assertEquals(controller, r.getController());
        assertEquals(action, r.getAction());
    }
}
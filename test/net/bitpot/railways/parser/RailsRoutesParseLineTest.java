package net.bitpot.railways.parser;


import net.bitpot.railways.models.RequestMethods;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import net.bitpot.railways.models.routes.EngineRoute;
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
    private RailsRoutesParser parser = null;

    private String line, name, path, actionTitle;
    private Class routeClass;
    private RequestMethod rType;


    public RailsRoutesParseLineTest(String line, Class routeClass, String name,
                                    RequestMethod rType, String path, String actionTitle) {
        this.line = line;
        this.routeClass = routeClass;
        this.name = name;
        this.rType = rType;
        this.path = path;
        this.actionTitle = actionTitle;
    }


    @Before
    public void setUp() {
        parser = new RailsRoutesParser();
    }


    @Parameterized.Parameters
    public static Collection<Object[]> createParseLineData() {
        return Arrays.asList(new Object[][] {
            // [0]
            {
                "          photo_album_photos POST   /photo_albums/:photo_album_id/photos(.:format)           {:action=>\"create\", :controller=>\"photos\"}",
                    SimpleRoute.class, "photo_album_photos", RequestMethods.POST,
                    "/photo_albums/:photo_album_id/photos(.:format)", "photos#create"
            },

            // [1]
            {
                "             new_photo_album GET    /photo_albums/new(.:format)                              {:action=>\"new\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "new_photo_album", RequestMethods.GET,
                    "/photo_albums/new(.:format)", "photo_albums#new"
            },

            // [2]
            {
                "            edit_photo_album GET    /photo_albums/:id/edit(.:format)                         {:action=>\"edit\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "edit_photo_album", RequestMethods.GET,
                    "/photo_albums/:id/edit(.:format)", "photo_albums#edit"
            },

            // [3]
            {
                "                 photo_album GET    /photo_albums/:id(.:format)                              {:action=>\"show\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "photo_album", RequestMethods.GET,
                    "/photo_albums/:id(.:format)", "photo_albums#show"
            },

            // [4]
            {
                "                             PUT    /photo_albums/:id(.:format)                              {:action=>\"update\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "", RequestMethods.PUT,
                    "/photo_albums/:id(.:format)", "photo_albums#update"
            },

            // [5]
            {
                "                             DELETE /photo_albums/:id(.:format)                              {:action=>\"destroy\", :controller=>\"photo_albums\"}",
                    SimpleRoute.class, "", RequestMethods.DELETE,
                    "/photo_albums/:id(.:format)", "photo_albums#destroy"
            },

            // [6] New rails format
            {
                "                 photo_album GET    /photo_albums/:id(.:format)                              photo_albums#show",
                    SimpleRoute.class, "photo_album", RequestMethods.GET,
                    "/photo_albums/:id(.:format)", "photo_albums#show"
            },

            // [7]
            {
                "                             PUT    /photo_albums/:id(.:format)                              photo_albums#update",
                    SimpleRoute.class, "", RequestMethods.PUT,
                    "/photo_albums/:id(.:format)", "photo_albums#update"
            },

            // [8]
            {
                "                             DELETE /photo_albums/:id(.:format)                              photo_albums#destroy",
                    SimpleRoute.class, "", RequestMethods.DELETE,
                    "/photo_albums/:id(.:format)", "photo_albums#destroy"
            },

            // [9] Test mounting rack-application
            // No action means that it's Rack application
            {
                "    test_server        /test                     {:to=>TestServer}",
                    EngineRoute.class, "test_server", RequestMethods.ANY,
                    "/test", "TestServer"
            },

            // [10] Mounted rack application with namespace
            // No action means that it's Rack application
            {
                "    test_server2        /test                     Test::Server",
                    EngineRoute.class, "test_server2", RequestMethods.ANY,
                    "/test", "Test::Server"
            },

            // [11] Test parsing routes with additional requirements (constraints etc.) (Old format)
            {
                "mozilla_users        /mozilla_users(.:format)  {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/, :controller=>\"users\", :action=>\"index\"}",
                    SimpleRoute.class, "mozilla_users", RequestMethods.ANY,
                    "/mozilla_users(.:format)", "users#index"
            },

            // [12] Test parsing routes with additional requirements (constraints etc.) (New format)
            {
                "mozilla_users        /mozilla_users(.:format)  users#index {:user_agent=>/(Mozilla.*(compatible|Windows|Macintosh|Linux|iPad)|Site 24 X 7).*/}",
                    SimpleRoute.class, "mozilla_users", RequestMethods.ANY,
                    "/mozilla_users(.:format)", "users#index"
            },

            // [13] Test parsing of redirecting route
            {
                "    redirect        /redirect(.:format)                    :controller#:action",
                    SimpleRoute.class, "redirect", RequestMethods.ANY,
                    "/redirect(.:format)", ":controller#:action"
            },

            // [14] Test parsing of Rails 4 PATCH method
            {
                "          PATCH  /users/:id(.:format)      users#update",
                    SimpleRoute.class, "", RequestMethods.PATCH,
                    "/users/:id(.:format)", "users#update"
            },

            // [15] Parser should successfully parse custom action text - it's better
            // that route is added with unknown action string than ignored.
            {
                "    redirect_301 GET    /redirect_301(.:format)   unknown format in action field",
                    SimpleRoute.class, "redirect_301", RequestMethods.GET,
                    "/redirect_301(.:format)", "unknown format in action field"
            },

            // [16] Allow parsing routes which path starts from '('
            //
            //     scope '(:locale)', locale: /en|ja/ do
            //       resources :spots
            //     end
            {
                "  spots GET    (/:locale)/spots(.:format)         spots#index {:locale=>/en|ja/}",
                    SimpleRoute.class, "spots", RequestMethods.GET,
                    "(/:locale)/spots(.:format)", "spots#index"
            }
        });
    }


    @Test
    public void testParseLine()
    {
        List<Route> routeList = parser.parseLine(line);

        assertNotNull(routeList);

        Route r = routeList.get(0);
        assertEquals(1, routeList.size());
        assertNotNull(r);

        assertEquals(name, r.getRouteName());
        assertEquals(routeClass, r.getClass());
        assertEquals(rType, r.getRequestMethod());
        assertEquals(path, r.getPath());
        //assertEquals(controller, r.getController());
        assertEquals(actionTitle, r.getActionTitle());
    }
}
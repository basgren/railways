package net.bitpot.railways.rails;


import net.bitpot.railways.parser.route.RouteParser;
import net.bitpot.railways.parser.route.RouteToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test of parsing route into tokens for syntax highlighting.
 */
@RunWith(Parameterized.class)
public class RouteParseTest
{

    private String myRouteStr;
    private RouteToken[] myTokens;

    public RouteParseTest(String routeStr, RouteToken[] tokens) {
        myRouteStr = routeStr;
        myTokens = tokens;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> createParseLineData() {
        return Arrays.asList(new Object[][] {
                {
                    "/users", new RouteToken[] {
                        token(RouteToken.PLAIN, "/users")
                    }
                }
        });
    }

    private static RouteToken token(int tokenType, String text) {
        RouteToken token = new RouteToken(tokenType, text);

        return token;
    }


    @Test
    public void testParseRoute()
    {
        RouteToken[] tokens = RouteParser.parseRoute(myRouteStr);

        assertEquals(tokens.length, myTokens.length);

        for(int i = 0; i < tokens.length; i++) {
            RouteToken myToken = myTokens[i];
            RouteToken token = tokens[i];

            assertEquals(myToken.tokenType, token.tokenType);
            assertEquals(myToken.text, token.text);
        }
    }
}
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
    private RouteToken[] expectedTokens;

    public RouteParseTest(String routeStr, RouteToken[] tokens) {
        myRouteStr = routeStr;
        expectedTokens = tokens;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> createParseLineData() {
        return Arrays.asList(new Object[][] {
                { "/users", new RouteToken[] {
                        token_plain("/users")
                    }
                },

                { "/users/:id", new RouteToken[] {
                        token_plain("/users/"), token_param(":id")
                    }
                },

                { "/users/:id/edit", new RouteToken[] {
                        token_plain("/users/"), token_param(":id"), token_plain("/edit"),
                    }
                },

                { "/users(/list)", new RouteToken[] {
                        token_plain("/users"), token_optional("(/list)")
                    }
                },

                { "/users(.:format)", new RouteToken[] {
                        token_plain("/users"), token_optional("(.:format)")
                    }
                },

                { "/users(/list(/recent))", new RouteToken[] {
                        token_plain("/users"), token_optional("(/list(/recent))")
                    }
                },

                // Unbalanced (invalid) route should also be correctly parsed,
                // but all tokens should be PLAIN
                { "/users(/list(/recent)", new RouteToken[] {
                        token_plain("/users"), token_plain("(/list(/recent)")
                    }
                }
        });
    }

    private static RouteToken token_plain(String text) {
        return new RouteToken(RouteToken.PLAIN, text);
    }

    private static RouteToken token_param(String text) {
        return new RouteToken(RouteToken.PARAMETER, text);
    }

    private static RouteToken token_optional(String text) {
        return new RouteToken(RouteToken.OPTIONAL, text);
    }


    @Test
    public void testParseRoute() {
        RouteToken[] tokens = RouteParser.parseRoute(myRouteStr);

        assertEquals("Token lists have the same length",
                expectedTokens.length, tokens.length);

        for(int i = 0; i < tokens.length; i++) {
            RouteToken expectedToken = expectedTokens[i];
            RouteToken token = tokens[i];

            assertEquals("Token types are the same",
                    expectedToken.tokenType, token.tokenType);

            assertEquals("Token text are the same",
                    expectedToken.text, token.text);
        }
    }
}
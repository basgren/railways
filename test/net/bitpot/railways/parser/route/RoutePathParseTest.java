package net.bitpot.railways.parser.route;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test of parsing route into tokens for syntax highlighting.
 */
@RunWith(Parameterized.class)
public class RoutePathParseTest
{

    private String myRouteStr;
    private RoutePathChunk[] expectedTokens;

    public RoutePathParseTest(String routeStr, RoutePathChunk[] tokens) {
        myRouteStr = routeStr;
        expectedTokens = tokens;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> createParseLineData() {
        return Arrays.asList(new Object[][] {
                { "/users", new RoutePathChunk[] {
                        chunk_plain("/users")
                    }
                },

                { "/users/:id", new RoutePathChunk[] {
                        chunk_plain("/users/"), chunk_param(":id")
                    }
                },

                { "/users/:id/edit", new RoutePathChunk[] {
                        chunk_plain("/users/"), chunk_param(":id"), chunk_plain("/edit"),
                    }
                },

                { "/users(/list)", new RoutePathChunk[] {
                        chunk_plain("/users"), chunk_optional("(/list)")
                    }
                },

                { "/users(.:format)", new RoutePathChunk[] {
                        chunk_plain("/users"), chunk_optional("(.:format)")
                    }
                },

                { "/users(/list(/recent))", new RoutePathChunk[] {
                        chunk_plain("/users"), chunk_optional("(/list(/recent))")
                    }
                },

                // Unbalanced (invalid) route should also be correctly parsed,
                // but all tokens should be PLAIN
                { "/users(/list(/recent)", new RoutePathChunk[] {
                        chunk_plain("/users"), chunk_plain("(/list(/recent)")
                    }
                }
        });
    }

    private static RoutePathChunk chunk_plain(String text) {
        return new RoutePathChunk(text, RoutePathChunk.PLAIN, 0);
    }

    private static RoutePathChunk chunk_param(String text) {
        return new RoutePathChunk(text, RoutePathChunk.PARAMETER, 0);
    }

    private static RoutePathChunk chunk_optional(String text) {
        return new RoutePathChunk(text, RoutePathChunk.OPTIONAL, 0);
    }


    @Test
    public void testParseRoute() {
        List<TextChunk> tokens = RoutePathParser.getInstance().parse(myRouteStr);

        assertEquals("Token lists have the same length",
                expectedTokens.length, tokens.size());

        for(int i = 0; i < tokens.size(); i++) {
            TextChunk expectedToken = expectedTokens[i];
            TextChunk token = tokens.get(i);

            assertEquals("Token types are the same",
                    expectedToken.getType(), token.getType());

            assertEquals("Token text are the same",
                    expectedToken.getText(), token.getText());
        }
    }
}
package net.bitpot.railways.parser.route;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
@RunWith(Parameterized.class)
public class RouteActionParserTest {

    private String myActionStr;
    private RouteActionChunk[] expectedTokens;

    public RouteActionParserTest(String routeStr, RouteActionChunk[] tokens) {
        myActionStr = routeStr;
        expectedTokens = tokens;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> createParseLineData() {
        return Arrays.asList(new Object[][]{
                {"api/users#edit", new RouteActionChunk[]{
                        chunkContainer("api/users"), chunkAction("#edit")}
                },

                {"api/users", new RouteActionChunk[]{
                        chunkContainer("api/users")}
                },

                {"", new RouteActionChunk[] {}}
        });
    }

    private static RouteActionChunk chunkContainer(String text) {
        return new RouteActionChunk(text, RouteActionChunk.CONTAINER);
    }

    private static RouteActionChunk chunkAction(String text) {
        return new RouteActionChunk(text, RouteActionChunk.ACTION);
    }



    @Test
    public void testParseRoute() {
        RouteActionChunk[] tokens = RouteActionParser.parse(myActionStr);

        assertEquals("Chunk lists have the same length",
                expectedTokens.length, tokens.length);

        for(int i = 0; i < tokens.length; i++) {
            RouteActionChunk expectedToken = expectedTokens[i];
            RouteActionChunk token = tokens[i];

            assertEquals("Chunk types are the same",
                    expectedToken.getType(), token.getType());

            assertEquals("Chunk text is the same",
                    expectedToken.getText(), token.getText());
        }
    }
}

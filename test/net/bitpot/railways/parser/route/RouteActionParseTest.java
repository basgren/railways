package net.bitpot.railways.parser.route;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
@RunWith(Parameterized.class)
public class RouteActionParseTest {

    private String myActionStr;
    private RouteActionChunk[] expectedChunks;

    public RouteActionParseTest(String routeStr, RouteActionChunk[] chunks) {
        myActionStr = routeStr;
        expectedChunks = chunks;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> createParseLineData() {
        return Arrays.asList(new Object[][]{
                {"api/users#edit", new RouteActionChunk[]{
                        chunkContainer("api/users#"), chunkAction("edit")}
                },

                {"api/users", new RouteActionChunk[]{
                        chunkContainer("api/users")}
                },

                {"", new RouteActionChunk[] {}}
        });
    }

    private static RouteActionChunk chunkContainer(String text) {
        return new RouteActionChunk(text, RouteActionChunk.CONTAINER, 0);
    }

    private static RouteActionChunk chunkAction(String text) {
        return new RouteActionChunk(text, RouteActionChunk.ACTION, 0);
    }



    @Test
    public void testParseRoute() {
        List<TextChunk> chunks = RouteActionParser.getInstance()
                .parse(myActionStr);

        assertEquals("Chunk lists have the same length",
                expectedChunks.length, chunks.size());

        for(int i = 0; i < chunks.size(); i++) {
            TextChunk expectedChunk = expectedChunks[i];
            TextChunk chunk = chunks.get(i);

            assertEquals("Chunk types are the same",
                    expectedChunk.getType(), chunk.getType());

            assertEquals("Chunk text is the same",
                    expectedChunk.getText(), chunk.getText());
        }
    }
}

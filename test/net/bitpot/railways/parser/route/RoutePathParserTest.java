package net.bitpot.railways.parser.route;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test of route parser.
 */
public class RoutePathParserTest
{
    @Test
    public void testParseAndHighlight() {
        RoutePathChunk[] chunks = RoutePathParser.parseAndHighlight("/test(.:test)", "st");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                chunkPlain("/te", false),
                chunkPlain("st", true),
                chunkOptional("(.:te", false),
                chunkOptional("st", true),
                chunkOptional(")", false)
        };

        assertChunkListEqual(expectedChunks, chunks);
    }

    @Test
    public void testParseAndHighlightWhenOneTokenHasNoHighlightedText() {
        RoutePathChunk[] chunks = RoutePathParser.parseAndHighlight("/tasks/:id", "t");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                chunkPlain("/", false),
                chunkPlain("t", true),
                chunkPlain("asks/", false),
                chunkParam(":id", false)
        };

        assertChunkListEqual(expectedChunks, chunks);
    }


    @Test
    public void testParseAndHighlightMultipleRegionsInOneChunk() {
        RoutePathChunk[] actual = RoutePathParser.parseAndHighlight("/test/test", "es");

        RoutePathChunk[] expected = new RoutePathChunk[] {
                chunkPlain("/t", false),
                chunkPlain("es", true),
                chunkPlain("t/t", false),
                chunkPlain("es", true),
                chunkPlain("t", false)
        };

        assertChunkListEqual(expected, actual);
    }

    @Test
    public void testParseAndHighlightSingleRegionInMultipleChunks() {
        RoutePathChunk[] actual = RoutePathParser.parseAndHighlight("/test/:test", "t/:te");

        RoutePathChunk[] expected = new RoutePathChunk[] {
                chunkPlain("/tes", false),
                chunkPlain("t/", true),
                chunkParam(":te", true),
                chunkParam("st", false)
        };

        assertChunkListEqual(expected, actual);
    }


    @Test
    public void testParseAndHighlightWithEmptySubstring() {
        RoutePathChunk[] actual = RoutePathParser.parseAndHighlight("/test/:test", "");

        RoutePathChunk[] expected = new RoutePathChunk[] {
                chunkPlain("/test/", false),
                chunkParam(":test", false)
        };

        assertChunkListEqual(expected, actual);
    }

    @Test
    public void testParseAndHighlightWithBlankSubstring() {
        RoutePathChunk[] chunks = RoutePathParser.parseAndHighlight("/test/:test", "   ");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                chunkPlain("/test/", false),
                chunkParam(":test", false)
        };

        assertChunkListEqual(expectedChunks, chunks);
    }


    private void assertChunkListEqual(RoutePathChunk[] expected, RoutePathChunk[] actual) {
        assertEquals("Tokens count are equal",
                expected.length, actual.length);

        for(int i = 0; i < actual.length; i++) {
            RoutePathChunk expectedChunk = expected[i];
            RoutePathChunk chunk = actual[i];

            assertEquals("Token types are the same",
                    expectedChunk.getType(), chunk.getType());

            assertEquals("Token texts are the same",
                    expectedChunk.getText(), chunk.getText());

            assertEquals("Token highlight flags are the same",
                    expectedChunk.isHighlighted(), chunk.isHighlighted());
        }
    }


    private static RoutePathChunk chunkPlain(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.PLAIN, text, isHighlighted);
    }

    private static RoutePathChunk chunkParam(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.PARAMETER, text, isHighlighted);
    }

    private static RoutePathChunk chunkOptional(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.OPTIONAL, text, isHighlighted);
    }


    private static RoutePathChunk createChunk(int type, String text, boolean isHighlighted) {
        RoutePathChunk chunk = new RoutePathChunk(text, type, 0);
        chunk.setHighlighted(isHighlighted);

        return chunk;
    }
}
package net.bitpot.railways.parser.route;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test of route parser.
 */
public class TextChunkHighlightTest
{
    private TextChunk[] parseAndHighlight(String subject, String hlStr) {
        return RoutePathParser.highlight(RoutePathParser.parse(subject), hlStr);
    }
            
    @Test
    public void testParseAndHighlight() {
        TextChunk[] actual = parseAndHighlight("/test(.:test)", "st");

        TextChunk[] expected = new TextChunk[] {
                chunkPlain("/te", false),
                chunkPlain("st", true),
                chunkOptional("(.:te", false),
                chunkOptional("st", true),
                chunkOptional(")", false)
        };

        assertChunkListEqual(expected, actual);
    }

    @Test
    public void testParseAndHighlightWhenOneTokenHasNoHighlightedText() {
        TextChunk[] actual = parseAndHighlight("/tasks/:id", "t");

        TextChunk[] expected = new TextChunk[] {
                chunkPlain("/", false),
                chunkPlain("t", true),
                chunkPlain("asks/", false),
                chunkParam(":id", false)
        };

        assertChunkListEqual(expected, actual);
    }


    @Test
    public void testParseAndHighlightMultipleRegionsInOneChunk() {
        TextChunk[] actual = parseAndHighlight("/test/test", "es");

        TextChunk[] expected = new TextChunk[] {
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
        TextChunk[] actual = parseAndHighlight("/test/:test", "t/:te");

        TextChunk[] expected = new TextChunk[] {
                chunkPlain("/tes", false),
                chunkPlain("t/", true),
                chunkParam(":te", true),
                chunkParam("st", false)
        };

        assertChunkListEqual(expected, actual);
    }


    @Test
    public void testParseAndHighlightWithEmptySubstring() {
        TextChunk[] actual = parseAndHighlight("/test/:test", "");

        TextChunk[] expected = new TextChunk[] {
                chunkPlain("/test/", false),
                chunkParam(":test", false)
        };

        assertChunkListEqual(expected, actual);
    }

    @Test
    public void testParseAndHighlightWithBlankSubstring() {
        TextChunk[] actual = parseAndHighlight("/test/:test", "   ");

        TextChunk[] expected = new TextChunk[] {
                chunkPlain("/test/", false),
                chunkParam(":test", false)
        };

        assertChunkListEqual(expected, actual);
    }


    private void assertChunkListEqual(TextChunk[] expectedList, TextChunk[] actualList) {
        assertEquals("Tokens count are equal",
                expectedList.length, actualList.length);

        for(int i = 0; i < actualList.length; i++) {
            TextChunk expected = expectedList[i];
            TextChunk actual = actualList[i];

            assertEquals("Token types are the same",
                    expected.getType(), actual.getType());

            assertEquals("Token texts are the same",
                    expected.getText(), actual.getText());

            assertEquals("Token highlight flags are the same",
                    expected.isHighlighted(), actual.isHighlighted());
        }
    }


    private static TextChunk chunkPlain(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.PLAIN, text, isHighlighted);
    }

    private static TextChunk chunkParam(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.PARAMETER, text, isHighlighted);
    }

    private static TextChunk chunkOptional(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.OPTIONAL, text, isHighlighted);
    }


    private static TextChunk createChunk(int type, String text, boolean isHighlighted) {
        TextChunk chunk = new TextChunk(text, type, 0);
        chunk.setHighlighted(isHighlighted);

        return chunk;
    }
}
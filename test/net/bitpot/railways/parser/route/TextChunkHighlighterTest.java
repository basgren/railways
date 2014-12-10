package net.bitpot.railways.parser.route;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
public class TextChunkHighlighterTest {


    @Test
    public void testParseAndHighlight() {
        RoutePathChunk[] chunks = RoutePathParser.parse("/test(.:test)");
        RoutePathChunk[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "st");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                token_plain("/te", false),
                token_plain("st", true),
                token_optional("(.:te", false),
                token_optional("st", true),
                token_optional(")", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }

    @Test
    public void testParseAndHighlightWhenOneTokenHasNoHighlightedText() {
        RoutePathChunk[] chunks = RoutePathParser.parse("/tasks/:id");
        RoutePathChunk[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "t");

        RoutePathChunk[] expectedTokens = new RoutePathChunk[] {
                token_plain("/", false),
                token_plain("t", true),
                token_plain("asks/", false),
                token_param(":id", false)
        };

        assertTokenArraysEqual(expectedTokens, highlightedChunks);
    }


    @Test
    public void testParseAndHighlightMultipleRegionsInSingleToken() {
        RoutePathChunk[] chunks = RoutePathParser.parse("/test/test");
        RoutePathChunk[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "es");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                token_plain("/t", false),
                token_plain("es", true),
                token_plain("t/t", false),
                token_plain("es", true),
                token_plain("t", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }


    @Test
    public void testParseAndHighlightSingleRegionInMultipleTokens() {
        RoutePathChunk[] chunks = RoutePathParser.parse("/test/:test");
        RoutePathChunk[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "t/:te");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                token_plain("/tes", false),
                token_plain("t/", true),
                token_param(":te", true),
                token_param("st", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }


    @Test
    public void testParseAndHighlightWithEmptySubstring() {
        RoutePathChunk[] chunks = RoutePathParser.parse("/test/:test");
        RoutePathChunk[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                token_plain("/test/", false),
                token_param(":test", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }

    @Test
    public void testParseAndHighlightWithBlankSubstring() {
        RoutePathChunk[] chunks = RoutePathParser.parse("/test/:test");
        RoutePathChunk[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "   ");

        RoutePathChunk[] expectedChunks = new RoutePathChunk[] {
                token_plain("/test/", false),
                token_param(":test", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }


    private void assertTokenArraysEqual(RoutePathChunk[] expected, RoutePathChunk[] actual) {
        assertEquals("Chunks count are equal",
                expected.length, actual.length);

        for(int i = 0; i < actual.length; i++) {
            RoutePathChunk expectedToken = expected[i];
            RoutePathChunk token = actual[i];

            assertEquals("Token types are the same",
                    expectedToken.getType(), token.getType());

            assertEquals("Token texts are the same",
                    expectedToken.getText(), token.getText());

            assertEquals("Token highlight flags are the same",
                    expectedToken.isHighlighted(), token.isHighlighted());
        }
    }


    private static RoutePathChunk token_plain(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.PLAIN, text, isHighlighted);
    }

    private static RoutePathChunk token_param(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.PARAMETER, text, isHighlighted);
    }

    private static RoutePathChunk token_optional(String text, boolean isHighlighted) {
        return createChunk(RoutePathChunk.OPTIONAL, text, isHighlighted);
    }


    private static RoutePathChunk createChunk(int type, String text, boolean isHighlighted) {
        RoutePathChunk token = new RoutePathChunk(text, type, 0);
        token.setHighlighted(isHighlighted);

        return token;
    }
}

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
        RouteToken[] chunks = RoutePathParser.parseRoute("/test(.:test)");
        RouteToken[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "st");

        RouteToken[] expectedChunks = new RouteToken[] {
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
        RouteToken[] chunks = RoutePathParser.parseRoute("/tasks/:id");
        RouteToken[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "t");

        RouteToken[] expectedTokens = new RouteToken[] {
                token_plain("/", false),
                token_plain("t", true),
                token_plain("asks/", false),
                token_param(":id", false)
        };

        assertTokenArraysEqual(expectedTokens, highlightedChunks);
    }


    @Test
    public void testParseAndHighlightMultipleRegionsInSingleToken() {
        RouteToken[] chunks = RoutePathParser.parseRoute("/test/test");
        RouteToken[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "es");

        RouteToken[] expectedChunks = new RouteToken[] {
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
        RouteToken[] chunks = RoutePathParser.parseRoute("/test/:test");
        RouteToken[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "t/:te");

        RouteToken[] expectedChunks = new RouteToken[] {
                token_plain("/tes", false),
                token_plain("t/", true),
                token_param(":te", true),
                token_param("st", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }


    @Test
    public void testParseAndHighlightWithEmptySubstring() {
        RouteToken[] chunks = RoutePathParser.parseRoute("/test/:test");
        RouteToken[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "");

        RouteToken[] expectedChunks = new RouteToken[] {
                token_plain("/test/", false),
                token_param(":test", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }

    @Test
    public void testParseAndHighlightWithBlankSubstring() {
        RouteToken[] chunks = RoutePathParser.parseRoute("/test/:test");
        RouteToken[] highlightedChunks = TextChunkHighlighter.highlight(chunks, "   ");

        RouteToken[] expectedChunks = new RouteToken[] {
                token_plain("/test/", false),
                token_param(":test", false)
        };

        assertTokenArraysEqual(expectedChunks, highlightedChunks);
    }


    private void assertTokenArraysEqual(RouteToken[] expected, RouteToken[] actual) {
        assertEquals("Chunks count are equal",
                expected.length, actual.length);

        for(int i = 0; i < actual.length; i++) {
            RouteToken expectedToken = expected[i];
            RouteToken token = actual[i];

            assertEquals("Token types are the same",
                    expectedToken.getTokenType(), token.getTokenType());

            assertEquals("Token texts are the same",
                    expectedToken.getText(), token.getText());

            assertEquals("Token highlight flags are the same",
                    expectedToken.isHighlighted(), token.isHighlighted());
        }
    }


    private static RouteToken token_plain(String text, boolean isHighlighted) {
        return createToken(RouteToken.PLAIN, text, isHighlighted);
    }

    private static RouteToken token_param(String text, boolean isHighlighted) {
        return createToken(RouteToken.PARAMETER, text, isHighlighted);
    }

    private static RouteToken token_optional(String text, boolean isHighlighted) {
        return createToken(RouteToken.OPTIONAL, text, isHighlighted);
    }


    private static RouteToken createToken(int type, String text, boolean isHighlighted) {
        RouteToken token = new RouteToken(type, text);
        token.setHighlighted(isHighlighted);

        return token;
    }
}

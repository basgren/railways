package net.bitpot.railways.rails;


import net.bitpot.railways.parser.route.RoutePathParser;
import net.bitpot.railways.parser.route.RouteToken;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test of route parser.
 */
public class RouteParserTest
{
    @Test
    public void testParseAndHighlight() {
        RouteToken[] tokens = RoutePathParser.parseAndHighlight("/test(.:test)", "st");

        RouteToken[] expectedTokens = new RouteToken[] {
                token_plain("/te", false),
                token_plain("st", true),
                token_optional("(.:te", false),
                token_optional("st", true),
                token_optional(")", false)
        };

        assertTokenArraysEqual(expectedTokens, tokens);
    }

    @Test
    public void testParseAndHighlightWhenOneTokenHasNoHighlightedText() {
        RouteToken[] tokens = RoutePathParser.parseAndHighlight("/tasks/:id", "t");

        RouteToken[] expectedTokens = new RouteToken[] {
                token_plain("/", false),
                token_plain("t", true),
                token_plain("asks/", false),
                token_param(":id", false)
        };

        assertTokenArraysEqual(expectedTokens, tokens);
    }


    @Test
    public void testParseAndHighlightMultipleRegionsInSingleToken() {
        RouteToken[] tokens = RoutePathParser.parseAndHighlight("/test/test", "es");

        RouteToken[] expectedTokens = new RouteToken[] {
                token_plain("/t", false),
                token_plain("es", true),
                token_plain("t/t", false),
                token_plain("es", true),
                token_plain("t", false)
        };

        assertTokenArraysEqual(expectedTokens, tokens);
    }

    @Test
    public void testParseAndHighlightSingleRegionInMultipleTokens() {
        RouteToken[] tokens = RoutePathParser.parseAndHighlight("/test/:test", "t/:te");

        RouteToken[] expectedTokens = new RouteToken[] {
                token_plain("/tes", false),
                token_plain("t/", true),
                token_param(":te", true),
                token_param("st", false)
        };

        assertTokenArraysEqual(expectedTokens, tokens);
    }


    @Test
    public void testParseAndHighlightWithEmptySubstring() {
        RouteToken[] tokens = RoutePathParser.parseAndHighlight("/test/:test", "");

        RouteToken[] expectedTokens = new RouteToken[] {
                token_plain("/test/", false),
                token_param(":test", false)
        };

        assertTokenArraysEqual(expectedTokens, tokens);
    }

    @Test
    public void testParseAndHighlightWithBlankSubstring() {
        RouteToken[] tokens = RoutePathParser.parseAndHighlight("/test/:test", "   ");

        RouteToken[] expectedTokens = new RouteToken[] {
                token_plain("/test/", false),
                token_param(":test", false)
        };

        assertTokenArraysEqual(expectedTokens, tokens);
    }


    private void assertTokenArraysEqual(RouteToken[] expected, RouteToken[] actual) {
        assertEquals("Tokens count are equal",
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
package net.bitpot.railways.parser.route;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test of route parser.
 */
public class TextChunkHighlightTest
{
    private List<TextChunk> parsePathAndHighlight(String subject, String hlStr) {
        RoutePathParser parser = RoutePathParser.getInstance();
        return parser.highlight(parser.parse(subject), hlStr);
    }

    private List<TextChunk> parseActionAndHighlight(String subject, String hlStr) {
        RouteActionParser parser = RouteActionParser.getInstance();
        return parser.highlight(parser.parse(subject), hlStr);
    }

    private List<TextChunk> createChunkList(TextChunk... chunks) {
        ArrayList<TextChunk> result = new ArrayList<>();
        Collections.addAll(result, chunks);

        return result;
    }

    @Test
    public void testParseAndHighlight() {
        List<TextChunk> actual = parsePathAndHighlight("/test(.:test)", "st");

        List<TextChunk> expected = createChunkList(
                chunkPlain("/te", false),
                chunkPlain("st", true),
                chunkOptional("(.:te", false),
                chunkOptional("st", true),
                chunkOptional(")", false)
        );

        assertChunkListEqual(expected, actual);
    }

    @Test
    public void testParseAndHighlightWhenOneTokenHasNoHighlightedText() {
        List<TextChunk> actual = parsePathAndHighlight("/tasks/:id", "t");

        List<TextChunk> expected = createChunkList(
                chunkPlain("/", false),
                chunkPlain("t", true),
                chunkPlain("asks/", false),
                chunkParam(":id", false)
        );

        assertChunkListEqual(expected, actual);
    }


    @Test
    public void testParseAndHighlightMultipleRegionsInOneChunk() {
        List<TextChunk> actual = parsePathAndHighlight("/test/test", "es");

        List<TextChunk> expected = createChunkList(
                chunkPlain("/t", false),
                chunkPlain("es", true),
                chunkPlain("t/t", false),
                chunkPlain("es", true),
                chunkPlain("t", false)
        );

        assertChunkListEqual(expected, actual);
    }

    @Test
    public void testParseAndHighlightSingleRegionInMultipleChunks() {
        List<TextChunk> actual = parsePathAndHighlight("/test/:test", "t/:te");

        List<TextChunk> expected = createChunkList(
                chunkPlain("/tes", false),
                chunkPlain("t/", true),
                chunkParam(":te", true),
                chunkParam("st", false)
        );

        assertChunkListEqual(expected, actual);
    }


    @Test
    public void testParseAndHighlightWithEmptySubstring() {
        List<TextChunk> actual = parsePathAndHighlight("/test/:test", "");

        List<TextChunk> expected = createChunkList(
                chunkPlain("/test/", false),
                chunkParam(":test", false)
        );

        assertChunkListEqual(expected, actual);
    }

    @Test
    public void testParseAndHighlightWithBlankSubstring() {
        List<TextChunk> actual = parsePathAndHighlight("/test/:test", "   ");

        List<TextChunk> expected = createChunkList(
                chunkPlain("/test/", false),
                chunkParam(":test", false)
        );

        assertChunkListEqual(expected, actual);
    }


    @Test
    public void testParseAndHighlightOnBounds() {
        List<TextChunk> actual = parsePathAndHighlight("/test/:test", "/test/");

        List<TextChunk> expected = createChunkList(
                chunkPlain("/test/", true),
                chunkParam(":test", false)
        );

        assertChunkListEqual(expected, actual);
    }


    @Test
    public void testParseActionAndHighlightOnBounds() {
        List<TextChunk> actual = parseActionAndHighlight("ctrl#action", "ctrl#");

        List<TextChunk> expected = createChunkList(
                chunkPlain("ctrl#", true),
                chunkParam("action", false)
        );

        assertChunkListEqual(expected, actual);
    }


    private void assertChunkListEqual(List<TextChunk> expectedList, List<TextChunk> actualList) {
        assertEquals("Tokens count are equal",
                expectedList.size(), actualList.size());

        for(int i = 0; i < actualList.size(); i++) {
            TextChunk expected = expectedList.get(i);
            TextChunk actual = actualList.get(i);

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
        TextChunk chunk = new RoutePathChunk(text, type, 0);
        chunk.setHighlighted(isHighlighted);

        return chunk;
    }
}
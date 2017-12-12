package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a single route to break it down into tokens.
 *
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RoutePathParser extends TextChunkHighlighter {

    private static final Pattern PLAIN_URI = Pattern.compile("^[^(:]+");
    private static final Pattern PARAMETER = Pattern.compile("^:[a-zA-Z_]+");


    private static RoutePathParser instance = null;

    public static RoutePathParser getInstance() {
        if (instance == null)
            instance = new RoutePathParser();

        return instance;
    }


    /**
     * Parses Rails route and returns array of tokens.
     *
     * @param routePath String with route path.
     * @return Array of RouteTokens.
     */
    @NotNull
    public List<TextChunk> parse(String routePath) {
        RoutePathChunk chunk;
        ArrayList<TextChunk> chunks = new ArrayList<>();
        int pos = 0;

        // Try to find chunk
        while((chunk = parseToken(routePath, pos)) != null) {
            chunks.add(chunk);
            pos = chunk.getEndOffset();
        }

        return chunks;
    }


    private static RoutePathChunk parseToken(String routePart, int startPos) {
        RoutePathChunk chunk;

        chunk = parseOptionalPart(routePart, startPos);

        if (chunk == null)
            chunk = parsePlainToken(routePart, startPos);

        if (chunk == null)
            chunk = parseSymbolToken(routePart, startPos);

        return chunk;
    }


    private static RoutePathChunk parsePlainToken(String routePart, int startPos) {
        RoutePathChunk token = null;

        // Firstly try to find text
        Matcher matcher = PLAIN_URI.matcher(routePart);
        matcher.region(startPos, routePart.length());

        if (matcher.find())
            token = new RoutePathChunk(
                    matcher.group(), RoutePathChunk.PLAIN, matcher.start());

        return token;
    }


    private static RoutePathChunk parseSymbolToken(String routePart, int startPos) {
        RoutePathChunk chunk = null;

        Matcher matcher = PARAMETER.matcher(routePart);
        matcher.region(startPos, routePart.length());

        if (matcher.find())
            chunk = new RoutePathChunk(
                    matcher.group(), RoutePathChunk.PARAMETER, matcher.start());

        return chunk;
    }


    private static RoutePathChunk parseOptionalPart(String routePart, int startPos) {
        if (startPos >= routePart.length() || routePart.charAt(startPos) != '(')
            return null;

        // As we check that our string should start from '(', we count that we
        // already have one opening bracket.
        int openedCount = 0;
        boolean isBalanced = false;
        int endPos = startPos;

        while (endPos < routePart.length()) {
            char c = routePart.charAt(endPos);

            if (c == ')')      openedCount--;
            else if (c == '(') openedCount++;

            endPos++;

            if (openedCount == 0) {
                isBalanced = true;
                break;
            }
        }

        return new RoutePathChunk(routePart.substring(startPos, endPos),
                isBalanced ? RoutePathChunk.OPTIONAL : RoutePathChunk.PLAIN,
                startPos);
    }


    @NotNull
    @Override
    protected TextChunk createChunk(@NotNull String text, int chunkType, int offsetAbs) {
        return new RoutePathChunk(text, chunkType, offsetAbs);
    }
}
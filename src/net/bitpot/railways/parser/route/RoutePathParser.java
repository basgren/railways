package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a single route to break it down into tokens.
 *
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RoutePathParser {

    private static final Pattern PLAIN_URI = Pattern.compile("^[^(:]+");
    private static final Pattern PARAMETER = Pattern.compile("^:[a-zA-Z_]+");


    /**
     * Parses route path taking into account that provided substring should
     * be highlighted. Highlighted parts are the same tokens, but with
     * isHighlighted flag set to true.
     *
     * @param routePath Route path.
     * @param highlightText Substring of route that should be highlighted.
     * @return Array of RouteTokens with highlighted substring.
     */
    @NotNull
    public static RoutePathChunk[] parseAndHighlight(String routePath, String highlightText) {
        RoutePathChunk[] chunks = parse(routePath);
        highlightText = highlightText.trim();

        // First, find all substring regions to be highlighted.
        int[][] regions = findSubstringRegions(routePath, highlightText);
        if (regions == null)
            return chunks;

        ArrayList<RoutePathChunk> result = new ArrayList<RoutePathChunk>();

        // Now go through every RoutePathChunk and break it down if it intersects
        // with any region. Token type is preserved.
        for(RoutePathChunk chunk: chunks) {
            Collection<RoutePathChunk> resultingChunks = breakdownChunk(chunk, regions);
            result.addAll(resultingChunks);
        }

        return result.toArray(new RoutePathChunk[result.size()]);
    }


    /**
     * Parses Rails route and returns array of tokens.
     *
     * @param routePath String with route path.
     * @return Array of RouteTokens.
     */
    @NotNull
    public static RoutePathChunk[] parse(String routePath) {
        RoutePathChunk chunk;
        ArrayList<RoutePathChunk> chunks = new ArrayList<RoutePathChunk>();
        int pos = 0;

        // Try to find chunk
        while((chunk = parseToken(routePath, pos)) != null) {
            chunks.add(chunk);
            pos = chunk.getEndOffset();
        }

        return chunks.toArray(new RoutePathChunk[chunks.size()]);
    }


    /**
     * Finds all regions of provided string which contain provided substring.
     * Returns array of arrays. Each item is an array that contains begin and
     * end offsets of a substring:
     *
     * @param s String which will be searched for substring.
     * @param subStr Substring to find.
     * @return Array of substring regions (begin and end offsets) or null if
     *         specified substring is empty.
     */
    private static int[][] findSubstringRegions(String s, String subStr) {
        // Prevent infinite loop
        if (subStr.equals(""))
            return null;

        int lastIndex = 0, regionEnd;
        ArrayList<int[]> regions = new ArrayList<int[]>();

        while(lastIndex != -1) {
            lastIndex = s.indexOf(subStr, lastIndex);

            if (lastIndex != -1) {
                regionEnd = lastIndex + subStr.length();
                regions.add(new int[]{lastIndex, regionEnd});
                lastIndex = regionEnd;
            }
        }

        return regions.toArray(new int[regions.size()][]);
    }


    private static Collection<RoutePathChunk> breakdownChunk(RoutePathChunk chunk,
                                                             @NotNull int[][] highlightedRegions) {
        ArrayList<RoutePathChunk> result = new ArrayList<RoutePathChunk>();
        int lastPos = 0, partSize;

        // We assume that regions are sorted.
        for(int[] region: highlightedRegions) {
            // region[0] is an offset of substring begin (inclusive)
            // region[1] is an offset of substring end (exclusive)

            // Skip to the next region if current does not intersect with chunk
            if (region[1] <= chunk.getBeginOffset() + lastPos || chunk.getEndOffset() < region[0])
                continue;

            int startPos = chunk.getBeginOffset() + lastPos;

            // Get intersection of chunk and region
            int intersectionBegin = Math.max(startPos, region[0]);
            int intersectionEnd = Math.min(chunk.getEndOffset(), region[1]);

            // Now breakdown chunk into parts.
            // 1st part - between chunk begin and intersection begin
            partSize = intersectionBegin - startPos;
            if (partSize > 0) {
                result.add(new RoutePathChunk(
                        chunk.getText().substring(lastPos, lastPos + partSize),
                        chunk.getType(), startPos));
            }
            lastPos += partSize;

            // 2nd part - intersection itself (highlighted part).
            partSize = intersectionEnd - intersectionBegin;
            if (partSize > 0) {
                RoutePathChunk hlChunk = new RoutePathChunk(
                        chunk.getText().substring(lastPos, lastPos + partSize),
                        chunk.getType(), intersectionBegin);
                hlChunk.setHighlighted(true);

                result.add(hlChunk);
            }
            lastPos += partSize;
        }

        // the last part - between intersection and chunk ends, if it's necessary
        partSize = chunk.getText().length() - lastPos;
        if (partSize > 0) {
            result.add(new RoutePathChunk(
                    chunk.getText().substring(lastPos, lastPos + partSize),
                    chunk.getType(), chunk.getBeginOffset() + lastPos));
        }

        return result;
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
}
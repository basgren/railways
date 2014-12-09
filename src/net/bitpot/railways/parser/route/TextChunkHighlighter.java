package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
public class TextChunkHighlighter {

    class TextRegion {
        public int startOffset;
        public int endOffset;
    }

    public static RouteToken[] highlight(RouteToken[] textChunks,
                                               String highlightSubstr) {

        highlightSubstr = highlightSubstr.trim();
        LinkedList<RouteToken> result = new LinkedList<RouteToken>();

        StringBuilder sb = new StringBuilder();
        for(RouteToken t: textChunks)
            sb.append(t.text);

        // First, find all substring regions to be highlighted.
        int[][] regions = findSubstringRegions(sb.toString(), highlightSubstr);
        if (regions == null)
            return textChunks;

        // Now go through every RouteToken and break it down if it intersects
        // with any region. Token type is preserved.
        for(RouteToken chunk: textChunks) {
            Collection<RouteToken> resultingTokens = breakdownToken(chunk, regions);
            result.addAll(resultingTokens);
        }

        return result.toArray(new RouteToken[result.size()]);
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


    private static Collection<RouteToken> breakdownToken(RouteToken token,
                                                         @NotNull int[][] highlightedRegions) {
        ArrayList<RouteToken> result = new ArrayList<RouteToken>();
        int lastPos = 0, partSize;

        // We assume that regions are sorted.
        for(int[] region: highlightedRegions) {
            // region[0] is an offset of substring begin (inclusive)
            // region[1] is an offset of substring end (exclusive)

            // Skip to the next region if current does not intersect with token
            if (region[1] <= token.startPos + lastPos || token.endPos < region[0])
                continue;

            int startPos = token.startPos + lastPos;

            // Get intersection of token and region
            int intersectionBegin = Math.max(startPos, region[0]);
            int intersectionEnd = Math.min(token.endPos, region[1]);

            // Now breakdown token into parts.
            // 1st part - between token begin and intersection begin
            partSize = intersectionBegin - startPos;
            if (partSize > 0) {
                result.add(new RouteToken(token.tokenType,
                        token.text.substring(lastPos, lastPos + partSize),
                        startPos, intersectionBegin));
            }
            lastPos += partSize;

            // 2nd part - intersection itself (highlighted part).
            partSize = intersectionEnd - intersectionBegin;
            if (partSize > 0) {
                RouteToken hlToken = new RouteToken(token.tokenType,
                        token.text.substring(lastPos, lastPos + partSize),
                        intersectionBegin, intersectionEnd);
                hlToken.isHighlighted = true;

                result.add(hlToken);
            }
            lastPos += partSize;
        }

        // the last part - between intersection and token ends, if it's necessary
        partSize = token.text.length() - lastPos;
        if (partSize > 0) {
            result.add(new RouteToken(token.tokenType,
                    token.text.substring(lastPos, lastPos + partSize),
                    token.startPos + lastPos, token.endPos));
        }

        return result;
    }


}

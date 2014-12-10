package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
public class TextChunkHighlighter {

    private static class TextRegion {
        public int startOffset;
        public int endOffset;

        public TextRegion(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
    }


    public static RouteToken[] highlight(RouteToken[] textChunks,
                                               String highlightSubstr) {

        highlightSubstr = highlightSubstr.trim();
        LinkedList<RouteToken> result = new LinkedList<RouteToken>();

        StringBuilder sb = new StringBuilder();
        for(RouteToken t: textChunks)
            sb.append(t.text);

        // First, find all substring regions to be highlighted.
        List<TextRegion> regions = findSubstringRegions(sb.toString(), highlightSubstr);
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
    private static List<TextRegion> findSubstringRegions(String s, String subStr) {
        // Prevent infinite loop
        if (subStr.equals(""))
            return null;

        int startOffset = 0, endOffset;
        ArrayList<TextRegion> regions = new ArrayList<TextRegion>();

        while(startOffset != -1) {
            startOffset = s.indexOf(subStr, startOffset);

            if (startOffset != -1) {
                endOffset = startOffset + subStr.length();
                regions.add(new TextRegion(startOffset, endOffset));
                startOffset = endOffset;
            }
        }

        return regions;
    }


    private static Collection<RouteToken> breakdownToken(RouteToken token,
                                                         @NotNull List<TextRegion> highlightedRegions) {
        ArrayList<RouteToken> result = new ArrayList<RouteToken>();
        int lastPos = 0, partSize;

        // We assume that regions are sorted.
        for(TextRegion region: highlightedRegions) {
            // Skip to the next region if current does not intersect with token
            if (region.endOffset <= token.startPos + lastPos ||
                    token.endPos < region.startOffset)
                continue;

            int startPos = token.startPos + lastPos;

            // Get intersection of token and region
            int intersectionBegin = Math.max(startPos, region.startOffset);
            int intersectionEnd = Math.min(token.endPos, region.endOffset);

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

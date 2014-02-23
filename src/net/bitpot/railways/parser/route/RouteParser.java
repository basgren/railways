package net.bitpot.railways.parser.route;

import com.intellij.openapi.util.text.StringUtilRt;
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
public class RouteParser {

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
    public static RouteToken[] parseAndHighlight(String routePath, String highlightText) {
        RouteToken[] tokens = parseRoute(routePath);

        // First, find all substring regions to be highlighted.
        int[][] regions = findSubstringRegions(routePath, highlightText);
        ArrayList<RouteToken> result = new ArrayList<>();

        // Now go through every RouteToken and break it down if it intersects
        // with any region. Token type is preserved.
        for(RouteToken token: tokens) {
            Collection<RouteToken> resultingTokens = breakdownToken(token, regions);
            result.addAll(resultingTokens);
        }

        return result.toArray(new RouteToken[result.size()]);
    }


    /**
     * Parses Rails route and returns array of tokens.
     *
     * @param route String with route path.
     * @return Array of RouteTokens.
     */
    @NotNull
    public static RouteToken[] parseRoute(String route) {
        RouteToken token;
        ArrayList<RouteToken> tokens = new ArrayList<>();
        int pos = 0;

        // Try to find token
        while((token = parseToken(route, pos)) != null) {
            tokens.add(token);
            pos = token.endPos;
        }

        return tokens.toArray(new RouteToken[tokens.size()]);
    }


    /**
     * Finds all regions of provided string which contain provided substring.
     * Returns array of arrays. Each item is an array that contains begin and
     * end offsets of a substring:
     *
     * @param s String which will be searched for substring.
     * @param substr Substring to find.
     * @return Array of substring regions (begin and end offsets).
     */
    private static int[][] findSubstringRegions(String s, String substr) {
        int lastIndex = 0, regionEnd;
        ArrayList<int[]> regions = new ArrayList<>();

        while(lastIndex != -1) {
            lastIndex = s.indexOf(substr, lastIndex);

            if (lastIndex != -1) {
                regionEnd = lastIndex + substr.length();
                regions.add(new int[]{lastIndex, regionEnd});
                lastIndex = regionEnd;
            }
        }

        return regions.toArray(new int[regions.size()][]);
    }


    private static Collection<RouteToken> breakdownToken(RouteToken token, int[][] highlightedRegions) {
        ArrayList<RouteToken> result = new ArrayList<>();
        int lastPos = 0, partSize, intsBegin, intsEnd = 0;

        // We assume that regions are sorted.
        for(int[] region: highlightedRegions) {
            // region[0] is an offset of substring begin (inclusive)
            // region[1] is an offset of substring end (exclusive)

            // Skip to the next region if current does not intersect with token
            if (region[1] <= token.startPos + lastPos || token.endPos < region[0])
                continue;

            int startPos = token.startPos + lastPos;

            // Get intersection of token and region
            intsBegin = Math.max(startPos, region[0]);
            intsEnd = Math.min(token.endPos, region[1]);

            // Now breakdown token into parts.
            // 1st part - between token begin and intersection begin
            partSize = intsBegin - startPos;
            if (partSize > 0) {
                result.add(new RouteToken(token.tokenType,
                        token.text.substring(lastPos, lastPos + partSize),
                        startPos, intsBegin));
            }
            lastPos += partSize;

            // 2nd part - intersection itself (highlighted part).
            partSize = intsEnd - intsBegin;
            if (partSize > 0) {
                RouteToken hlToken = new RouteToken(token.tokenType,
                        token.text.substring(lastPos, lastPos + partSize),
                        intsBegin, intsEnd);
                hlToken.isHighlighted = true;

                result.add(hlToken);
            }
            lastPos += partSize;
        }

        // the last part - between intersection and token ends, if it's necessary
        partSize = token.endPos - intsEnd;
        if (partSize > 0) {
            result.add(new RouteToken(token.tokenType,
                    token.text.substring(lastPos, lastPos + partSize),
                    intsEnd, token.endPos));
        }

        return result;
    }


    private static RouteToken parseToken(String routePart, int startPos) {
        RouteToken token;

        token = parseOptionalPart(routePart, startPos);

        if (token == null)
            token = parsePlainToken(routePart, startPos);

        if (token == null)
            token = parseSymbolToken(routePart, startPos);

        return token;
    }


    private static RouteToken parsePlainToken(String routePart, int startPos) {
        RouteToken token = null;

        // Firstly try to find text
        Matcher matcher = PLAIN_URI.matcher(routePart);
        matcher.region(startPos, routePart.length());

        if (matcher.find())
            token = new RouteToken(RouteToken.PLAIN, matcher.group(),
                    matcher.start(), matcher.end());

        return token;
    }


    private static RouteToken parseSymbolToken(String routePart, int startPos) {
        RouteToken token = null;

        Matcher matcher = PARAMETER.matcher(routePart);
        matcher.region(startPos, routePart.length());

        if (matcher.find())
            token = new RouteToken(RouteToken.PARAMETER, matcher.group(),
                    matcher.start(), matcher.end());

        return token;
    }


    private static RouteToken parseOptionalPart(String routePart, int startPos) {
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

        return new RouteToken(
                isBalanced ? RouteToken.OPTIONAL : RouteToken.PLAIN,
                routePart.substring(startPos, endPos), startPos, endPos);
    }
}
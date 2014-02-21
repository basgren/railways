package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    /**
     * Parses Rails route and returns array of tokens.
     *
     * @param route
     * @return
     */
    @NotNull
    public static RouteToken[] parseRoute(String route) {
        RouteToken token = null;
        ArrayList<RouteToken> tokens = new ArrayList<>();
        int pos = 0;

        // Try to find token
        while((token = parseToken(route, pos)) != null) {
            tokens.add(token);
            pos = token.endPos;
        }

        return tokens.toArray(new RouteToken[tokens.size()]);
    }


    private static RouteToken parseToken(String routePart, int startPos) {
        RouteToken token = null;

        Matcher matcher = PLAIN_URI.matcher(routePart);
        matcher.region(startPos, routePart.length());

        if (matcher.matches()) {
            token = new RouteToken(RouteToken.PLAIN, matcher.group());
            token.startPos = startPos;
            token.endPos = startPos + matcher.group().length();
        }

        return token;
    }

}
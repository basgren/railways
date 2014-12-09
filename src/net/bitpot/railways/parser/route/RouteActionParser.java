package net.bitpot.railways.parser.route;

import org.antlr.v4.runtime.misc.NotNull;

/**
 * Simple route action parser.
 *
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteActionParser {

    @NotNull
    public static RouteActionChunk[] parse(String subject) {
        if (subject.trim().equals(""))
            return new RouteActionChunk[] {};

        RouteActionChunk[] chunks;
        int pos = subject.indexOf("#");

        if (pos >= 0) {
            chunks = new RouteActionChunk[] {
                    new RouteActionChunk(subject.substring(0, pos),
                            RouteActionChunk.CONTAINER),
                    new RouteActionChunk(subject.substring(pos),
                            RouteActionChunk.ACTION)
            };
        } else
            chunks = new RouteActionChunk[] {
                    new RouteActionChunk(subject, RouteActionChunk.CONTAINER)
            };

        return chunks;
    }
}

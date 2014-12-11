package net.bitpot.railways.parser.route;

import org.antlr.v4.runtime.misc.NotNull;

/**
 * Simple route action parser.
 *
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteActionParser extends TextChunkHighlighter {

    @NotNull
    public static TextChunk[] parse(String subject) {
        if (subject.trim().equals(""))
            return new RouteActionChunk[] {};

        RouteActionChunk[] chunks;
        int pos = subject.indexOf("#");

        if (pos >= 0) {
            chunks = new RouteActionChunk[] {
                    new RouteActionChunk(subject.substring(0, pos),
                            RouteActionChunk.CONTAINER, 0),
                    new RouteActionChunk(subject.substring(pos),
                            RouteActionChunk.ACTION, pos)
            };
        } else
            chunks = new RouteActionChunk[] {
                    new RouteActionChunk(subject, RouteActionChunk.CONTAINER, 0)
            };

        return chunks;
    }
}

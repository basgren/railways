package net.bitpot.railways.parser.route;

import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple route action parser.
 *
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteActionParser extends TextChunkHighlighter {

    @NotNull
    public static List<TextChunk> parse(String subject) {
        List<TextChunk> chunks = new ArrayList<TextChunk>();

        if (subject.trim().equals(""))
            return chunks;

        int pos = subject.indexOf("#");

        if (pos >= 0) {
            chunks.add(new RouteActionChunk(subject.substring(0, pos),
                    RouteActionChunk.CONTAINER, 0));
            chunks.add(new RouteActionChunk(subject.substring(pos),
                    RouteActionChunk.ACTION, pos));

        } else
            chunks.add(new RouteActionChunk(subject,
                    RouteActionChunk.CONTAINER, 0));

        return chunks;
    }
}

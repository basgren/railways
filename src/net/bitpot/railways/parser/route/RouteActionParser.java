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

        String[] parts = subject.split("#", 2);

        if (parts.length > 1) {
            chunks.add(new RouteActionChunk(parts[0] + "#",
                    RouteActionChunk.CONTAINER, 0));
            chunks.add(new RouteActionChunk(parts[1],
                    RouteActionChunk.ACTION, parts[0].length()));

        } else
            chunks.add(new RouteActionChunk(subject,
                    RouteActionChunk.CONTAINER, 0));

        return chunks;
    }
}

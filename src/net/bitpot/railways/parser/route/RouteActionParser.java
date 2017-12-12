package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple route action parser.
 *
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteActionParser extends TextChunkHighlighter {

    private static RouteActionParser instance = null;

    public static RouteActionParser getInstance() {
        if (instance == null)
            instance = new RouteActionParser();

        return instance;
    }


    @NotNull
    public List<TextChunk> parse(String subject) {
        List<TextChunk> chunks = new ArrayList<>();

        if (subject.trim().equals(""))
            return chunks;

        String[] parts = subject.split("#", 2);

        if (parts.length > 1) {
            chunks.add(new RouteActionChunk(parts[0] + "#",
                    RouteActionChunk.CONTAINER, 0));
            chunks.add(new RouteActionChunk(parts[1],
                    RouteActionChunk.ACTION, parts[0].length() + 1));

        } else
            chunks.add(new RouteActionChunk(subject,
                    RouteActionChunk.CONTAINER, 0));

        return chunks;
    }


    @NotNull
    @Override
    protected TextChunk createChunk(@NotNull String text, int chunkType, int offsetAbs) {
        return new RouteActionChunk(text, chunkType, offsetAbs);
    }
}

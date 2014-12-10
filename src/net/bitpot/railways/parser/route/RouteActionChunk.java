package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
public class RouteActionChunk extends TextChunk {

    public static final int CONTAINER = 0; // Class or module
    public static final int ACTION = 1;


    public RouteActionChunk(@NotNull String text, int chunkType, int startPos) {
        super(text, chunkType, startPos);
    }
}

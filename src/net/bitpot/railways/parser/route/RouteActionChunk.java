package net.bitpot.railways.parser.route;

/**
 * @author Basil Gren
 *         on 09.12.2014.
 */
public class RouteActionChunk {

    public static final int CONTAINER = 0; // Class or module
    public static final int ACTION = 1;
    private final int chunkType;
    private final String text;

    private boolean isHighlighted = false;

    public RouteActionChunk(String textChunk, int chunkType) {
        this.text = textChunk;
        this.chunkType = chunkType;
    }


    public boolean isHighlighted() {
        return isHighlighted;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return chunkType;
    }

}

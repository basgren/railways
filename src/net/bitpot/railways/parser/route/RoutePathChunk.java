package net.bitpot.railways.parser.route;

/**
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RoutePathChunk extends TextChunk {
    public final static int PLAIN = 0;
    public final static int PARAMETER = 1;
    public final static int OPTIONAL = 2;


    public RoutePathChunk(String text, int chunkType, int offsetAbs) {
        super(text, chunkType, offsetAbs);
    }
}
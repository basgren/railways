package net.bitpot.railways.parser.route;

/**
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteToken {
    public final static int PLAIN = 0;
    public final static int PARAMETER = 1;
    public final static int OPTIONAL = 2;


    public String text;
    public int tokenType = PLAIN;
    public int startPos;
    public int endPos;
    public boolean isHighlighted = false;


    public RouteToken(int tokenType, String text) {
        this(tokenType, text, 0, 0);
    }

    public RouteToken(int tokenType, String text, int startPos, int endPos) {
        this.tokenType = tokenType;
        this.text = text;
        this.startPos = startPos;
        this.endPos = endPos;
    }
}
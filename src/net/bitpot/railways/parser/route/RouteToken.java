package net.bitpot.railways.parser.route;

import org.jetbrains.annotations.NotNull;

/**
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteToken {
    public final static int PLAIN = 0;
    public final static int PARAMETER = 1;
    public final static int OPTIONAL = 2;


    private String text;
    private int tokenType = PLAIN;
    private int startPos;
    public boolean isHighlighted = false;


    public RouteToken(int tokenType, @NotNull String text) {
        this(tokenType, text, 0);
    }

    public RouteToken(int tokenType, @NotNull String text, int startPos) {
        this.tokenType = tokenType;
        this.text = text;
        this.startPos = startPos;
    }

    public int getEndPos() {
        return startPos + text.length();
    }

    public int getStartPos() {
        return startPos;
    }

    public int getTokenType() {
        return tokenType;
    }

    @NotNull
    public String getText() {
        return text;
    }
}
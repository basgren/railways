package net.bitpot.railways.parser.route;

import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.gui.RailwaysColors;
import org.jetbrains.annotations.NotNull;

/**
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RoutePathChunk extends TextChunk {
    public final static int PLAIN = 0;
    public final static int PARAMETER = 1;
    public final static int OPTIONAL = 2;


    public RoutePathChunk(@NotNull String text, int chunkType, int offsetAbs) {
        super(text, chunkType, offsetAbs);
    }

    @Override
    public SimpleTextAttributes getTextAttrs() {
        switch(getType()) {
            case RoutePathChunk.PARAMETER:
                return isHighlighted() ? RailwaysColors.PARAM_TOKEN_HL_ATTR :
                        RailwaysColors.PARAM_TOKEN_ATTR;

            case RoutePathChunk.OPTIONAL:
                return isHighlighted() ? RailwaysColors.OPTIONAL_TOKEN_HL_ATTR :
                        RailwaysColors.OPTIONAL_TOKEN_ATTR;

            default:
                return isHighlighted() ? RailwaysColors.REGULAR_HL_ATTR :
                        SimpleTextAttributes.REGULAR_ATTRIBUTES;
        }
    }
}
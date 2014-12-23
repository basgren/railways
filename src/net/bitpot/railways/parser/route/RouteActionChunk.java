package net.bitpot.railways.parser.route;

import com.intellij.ui.SimpleTextAttributes;
import net.bitpot.railways.gui.RailwaysColors;
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

    @Override
    public SimpleTextAttributes getTextAttrs() {
        SimpleTextAttributes textAttrs;

        if (getType() == RouteActionChunk.ACTION)
            textAttrs = isHighlighted() ?
                    RailwaysColors.METHOD_HL_ATTR : RailwaysColors.METHOD_ATTR;
        else
            textAttrs = isHighlighted() ?
                RailwaysColors.REGULAR_HL_ATTR : SimpleTextAttributes.REGULAR_ATTRIBUTES;

        return textAttrs;
    }
}

package net.bitpot.railways.parser.route;

import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Basil Gren
 *         on 11.12.2014.
 */
public abstract class TextChunk {

    private String text;
    private int type;
    private int startPos;
    private boolean isHighlighted = false;


    public TextChunk(@NotNull String text, int chunkType, int startPos) {
        this.type = chunkType;
        this.text = text;
        this.startPos = startPos;
    }

    public int getEndOffset() {
        return startPos + text.length();
    }

    public int getBeginOffset() {
        return startPos;
    }

    public int getType() {
        return type;
    }

    @NotNull
    public String getText() {
        return text;
    }


    public boolean isHighlighted() {
        return isHighlighted;
    }


    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

    public abstract SimpleTextAttributes getTextAttrs();


}

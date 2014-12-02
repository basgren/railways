package net.bitpot.railways.gui;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

import java.awt.*;

/**
 *
 * @author Basil Gren
 *         on 23.02.14.
 */
public abstract class RailwaysColors {

    // Color of selection.
    public static Color HIGHLIGHT_BG_COLOR =
            EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES.getDefaultAttributes()
                    .getBackgroundColor();

    // Route token colors
    public static Color PARAM_TOKEN_COLOR = schemeColor("RUBY_SYMBOL");
    public static Color OPTIONAL_TOKEN_COLOR = JBColor.GRAY;

    // Color of the nonexistent action that's referenced by a route.
    public static Color MISSING_ACTION_COLOR = JBColor.RED;
    public static Color DISABLED_ITEM_COLOR = JBColor.GRAY;



    // Text attributes

    public static final SimpleTextAttributes REGULAR_HL_ATTR =
            plainTextAttr(null, HIGHLIGHT_BG_COLOR);

    public static final SimpleTextAttributes PARAM_TOKEN_ATTR =
            plainTextAttr(PARAM_TOKEN_COLOR, null);

    public static final SimpleTextAttributes PARAM_TOKEN_HL_ATTR =
            plainTextAttr(PARAM_TOKEN_COLOR, HIGHLIGHT_BG_COLOR);

    public static final SimpleTextAttributes OPTIONAL_TOKEN_ATTR =
            plainTextAttr(OPTIONAL_TOKEN_COLOR, null);

    public static final SimpleTextAttributes OPTIONAL_TOKEN_HL_ATTR =
            plainTextAttr(OPTIONAL_TOKEN_COLOR, HIGHLIGHT_BG_COLOR);

    public static final SimpleTextAttributes MISSING_ACTION_ATTR =
            plainTextAttr(MISSING_ACTION_COLOR, null);

    public static final SimpleTextAttributes DISABLED_ITEM_ATTR =
            plainTextAttr(DISABLED_ITEM_COLOR, null);


    private static Color schemeColor(String name) {
        return TextAttributesKey.find(name)
                .getDefaultAttributes().getForegroundColor();
    }


    private static SimpleTextAttributes plainTextAttr(Color fgColor, Color bgColor) {
        return new SimpleTextAttributes(bgColor, fgColor,
                null, SimpleTextAttributes.STYLE_PLAIN);
    }
}
package net.bitpot.railways.gui;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

import java.awt.*;

/**
 *
 * @author Basil Gren
 *         on 23.02.14.
 */
public abstract class RailwaysColors {

    public static Color HIGHLIGHT_BG_COLOR =
            EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES.getDefaultAttributes().getBackgroundColor();
    public static Color PARAM_TOKEN_COLOR = JBColor.MAGENTA;
    public static Color OPTIONAL_TOKEN_COLOR = JBColor.GRAY;

    public static final SimpleTextAttributes REGULAR_HL_ATTR =
            new SimpleTextAttributes(HIGHLIGHT_BG_COLOR, null,
                    null, SimpleTextAttributes.STYLE_PLAIN);


    public static final SimpleTextAttributes PARAM_TOKEN_ATTR =
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
                    RailwaysColors.PARAM_TOKEN_COLOR);

    public static final SimpleTextAttributes PARAM_TOKEN_HL_ATTR =
            new SimpleTextAttributes(RailwaysColors.HIGHLIGHT_BG_COLOR, RailwaysColors.PARAM_TOKEN_COLOR,
                    null, SimpleTextAttributes.STYLE_PLAIN);

    public static final SimpleTextAttributes OPTIONAL_TOKEN_ATTR =
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, RailwaysColors.OPTIONAL_TOKEN_COLOR);

    public static final SimpleTextAttributes OPTIONAL_TOKEN_HL_ATTR =
            new SimpleTextAttributes(RailwaysColors.HIGHLIGHT_BG_COLOR, RailwaysColors.OPTIONAL_TOKEN_COLOR,
                    null, SimpleTextAttributes.STYLE_PLAIN);
}
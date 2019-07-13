package net.bitpot.railways.navigation;

import com.intellij.ide.util.ModuleRendererFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.util.ui.UIUtil;
import net.bitpot.railways.models.Route;

import javax.swing.*;
import java.awt.*;

/**
 * Factory creates module renderer that renders Route module.
 */
public class RouteModuleRendererFactory extends ModuleRendererFactory {

    @Override
    protected boolean handles(Object o) {
        // Handle only Route objects.
        return o instanceof Route;
    }


    @Override
    public DefaultListCellRenderer getModuleRenderer() {
        return new RouteModuleRenderer();
    }


    /**
     * Provides a component for rendering Route module in Go To Route navigation
     * list.
     */
    private class RouteModuleRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

            // Set module name and icon
            Module module = ((Route)value).getModule();
            setText(module.getName());
            setIcon(ModuleType.get(module).getIcon());

            // Set additional rendering properties.
            // To preserve consistent look and feel, the implementation is taken from
            // com.intellij.ide.util.PsiElementModuleRenderer.customizeCellRenderer()
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
            setHorizontalTextPosition(SwingConstants.LEFT);
            setBackground(UIUtil.getListBackground(isSelected, cellHasFocus));
            setForeground(isSelected ? UIUtil.getListForeground(true, cellHasFocus) : UIUtil.getInactiveTextColor());

            return component;
        }
    }
}
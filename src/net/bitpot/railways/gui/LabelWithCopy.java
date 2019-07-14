package net.bitpot.railways.gui;

import com.intellij.ide.CopyProvider;
import com.intellij.ide.TextCopyProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class LabelWithCopy extends JBLabel implements DataProvider {

    private CopyProvider copyProvider = new MyCopyProvider(this);

    @Nullable
    private StringFormatter formatter;

    public LabelWithCopy() {
        addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(Component comp, int x, int y) {
                ActionManager actMgr = ActionManager.getInstance();
                ActionGroup group = (ActionGroup) ActionManager.getInstance()
                        .getAction("railways.CopyMenu");

                ActionPopupMenu popupMenu =
                        actMgr.createActionPopupMenu(ActionPlaces.UNKNOWN, group);
                popupMenu.getComponent().show(comp, x, y);
            }
        });
    }

    public void setCopyFormatter(StringFormatter formatter) {
        this.formatter = formatter;
    }

    @Nullable
    @Override
    public Object getData(@NotNull @NonNls String s) {
        if (PlatformDataKeys.COPY_PROVIDER.is(s))
            return copyProvider;

        return null;
    }


    private class MyCopyProvider extends TextCopyProvider {

        private LabelWithCopy label;

        public MyCopyProvider(LabelWithCopy label) {
            this.label = label;
        }

        @Nullable
        @Override
        public Collection<String> getTextLinesToCopy() {
            String text = label.getText();

            if (text.isEmpty() || text.equals("-"))
                return null;

            if (formatter != null)
                text = formatter.format(text);

            Collection<String> result = new ArrayList<>(1);
            result.add(text);

            return result;
        }
    }

}

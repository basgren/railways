package net.bitpot.railways.gui;

import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.StringSelection;
import java.util.Collection;

/**
 * Temporary class for Intellij IDEA v14 - copied from v15 to fix plugin crash.
 */
// TODO: remove this class and use build-in TextCopy in versions of plugin 0.9+ (with route tree)
public abstract class TextCopyProvider implements CopyProvider {

    @Nullable
    public abstract Collection<String> getTextLinesToCopy();

    public void performCopy(@NotNull DataContext dataContext) {
        Collection lines = this.getTextLinesToCopy();
        if(lines != null && !lines.isEmpty()) {
            String text = StringUtil.join(lines, this.getLinesSeparator());
            CopyPasteManager.getInstance().setContents(new StringSelection(text));
        }

    }

    public String getLinesSeparator() {
        return "\n";
    }

    public boolean isCopyEnabled(@NotNull DataContext dataContext) {
        return this.getTextLinesToCopy() != null;
    }

    public boolean isCopyVisible(@NotNull DataContext dataContext) {
        return true;
    }
}

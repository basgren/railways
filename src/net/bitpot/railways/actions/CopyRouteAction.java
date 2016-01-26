package net.bitpot.railways.actions;

import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

public class CopyRouteAction extends AnAction {

    // TODO: implement copying of route path

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // Context is a table
        DataContext ctx = anActionEvent.getDataContext();

        CopyProvider provider = PlatformDataKeys.COPY_PROVIDER.getData(ctx);
        if (provider != null) {
            provider.performCopy(ctx);
        }
    }
}

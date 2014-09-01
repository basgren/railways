package net.bitpot.railways.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;

/**
 * @author Basil Gren
 *         on 01.09.14.
 */
public class CollapseAllAction extends AnAction {

    private static Logger log = Logger.getInstance(CollapseAllAction.class.getName());


    @Override
    public void actionPerformed(AnActionEvent e) {
        log.debug("Collapse all action");
    }
}

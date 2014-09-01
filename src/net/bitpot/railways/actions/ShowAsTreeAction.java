package net.bitpot.railways.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;

/**
 * @author Basil Gren
 *         on 01.09.14.
 */
public class ShowAsTreeAction extends ToggleAction {

    private static Logger log = Logger.getInstance(ShowAsTreeAction.class.getName());


    /**
     * Returns the selected (checked, pressed) state of the action.
     *
     * @param e the action event representing the place and context in which the selected state is queried.
     * @return true if the action is selected, false otherwise
     */
    @Override
    public boolean isSelected(AnActionEvent e) {
        // Do nothing now
        return false;
    }


    /**
     * Sets the selected state of the action to the specified value.
     *
     * @param e     the action event which caused the state change.
     * @param state the new selected state of the action.
     */
    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        log.debug("Show routes tree (setSelected)");
    }
}

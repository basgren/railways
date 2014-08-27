package net.bitpot.railways.models;

/**
 * @author Basil Gren
 *         on 28.08.14.
 */
public interface RouteTreeItem {

    /**
     * Method should return true if RouteTreeItem can contain other items.
     * @return True if this is a container for other items.
     */
    public boolean isLeaf();

}

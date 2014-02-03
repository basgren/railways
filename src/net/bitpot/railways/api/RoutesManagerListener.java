package net.bitpot.railways.api;

/**
 *
 */
public interface RoutesManagerListener
{
    /**
     * Invoked when routes are updated.
     */
    public void routesUpdated();

    /**
     * Invoked before starting routes update.
     */
    public void beforeRoutesUpdate();


    /**
     * Called when routes update is failed.
     */
    public void routesUpdateError();
}

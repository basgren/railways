package net.bitpot.railways.routesView;

/**
 *
 */
public interface RoutesManagerListener {

    /**
     * Invoked when routes are updated.
     */
    public void routesUpdated(RoutesManager routesManager);

    /**
     * Invoked before starting routes update.
     */
    public void beforeRoutesUpdate(RoutesManager routesManager);


    /**
     * Called when routes update is failed.
     */
    public void routesUpdateError(RoutesManager routesManager);
}

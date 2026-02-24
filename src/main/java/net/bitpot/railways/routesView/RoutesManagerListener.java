package net.bitpot.railways.routesView;

public interface RoutesManagerListener {

    /**
     * Invoked when RoutesManager state is changed.
     *
     * @param routesManager RoutesManager that changed its state.
     */
    void stateChanged(RoutesManager routesManager);
}

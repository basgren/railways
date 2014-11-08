package net.bitpot.railways.models;

/**
 * @author Basil Gren
 *         on 08.11.2014.
 */
public class RailsEngine {

    private String myEngineClassName = "";
    private String myRouteNamespace = "";

    public RailsEngine(String engineClassName, String routeNamespace) {
        myEngineClassName = engineClassName;
        myRouteNamespace = routeNamespace;
    }


    public String getEngineClassName() {
        return myEngineClassName;
    }

    public String getRouteNamespace() {
        return myRouteNamespace;
    }
}

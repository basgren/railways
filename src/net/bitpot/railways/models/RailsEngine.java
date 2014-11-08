package net.bitpot.railways.models;

/**
 * @author Basil Gren
 *         on 08.11.2014.
 */
public class RailsEngine {

    private String myEngineClassName = "";
    private String myRootPath = "";


    public RailsEngine(String engineClassName, String rootPath) {
        myEngineClassName = engineClassName;
        myRootPath = rootPath;
    }


    public String getEngineClassName() {
        return myEngineClassName;
    }


    public String getRootPath() {
        return myRootPath;
    }
}

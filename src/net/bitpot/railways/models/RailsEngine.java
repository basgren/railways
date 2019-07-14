package net.bitpot.railways.models;

/**
 * @author Basil Gren
 *         on 08.11.2014.
 */
public class RailsEngine {

    private String myEngineClassName;
    private String myRootPath;
    private String myNamespace;


    public RailsEngine(String engineClassName, String rootPath, String namespace) {
        myEngineClassName = engineClassName;
        myRootPath = rootPath;
        myNamespace = namespace;
    }


    public String getRubyClassName() {
        return myEngineClassName;
    }


    public String getRootPath() {
        return myRootPath;
    }

    public String getNamespace() {
        return myNamespace;
    }
}

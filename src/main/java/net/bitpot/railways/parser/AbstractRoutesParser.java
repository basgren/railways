package net.bitpot.railways.parser;

import net.bitpot.railways.models.RouteList;

import java.io.*;

/**
 *
 */
public abstract class AbstractRoutesParser {


    /**
     * Parses routes data
     *
     * @param data Data with routes info
     * @return True on success parse, false otherwise.
     */
    abstract public RouteList parse(InputStream data);


    public RouteList parseFile(String fileName) throws FileNotFoundException {
        return parseFile(new File(fileName));
    }


    public RouteList parseFile(File f) throws FileNotFoundException {
        return parse(new FileInputStream(f));
    }
}
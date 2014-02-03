package net.bitpot.railways.parser;

import net.bitpot.railways.models.RouteList;

import java.io.*;

/**
 *
 */
public abstract class AbstractRoutesParser {


    /**
     * Parses routes data
     * @param data Data with routes info
     * @return True on success parse, false otherwise.
     */
    abstract public RouteList parse(InputStream data);



    public RouteList parseFile(String fileName) throws FileNotFoundException
    {
        File f = new File(fileName);

        return parseFile(f);
    }

    public RouteList parseFile(File f) throws FileNotFoundException
    {
        FileInputStream is = new FileInputStream(f);

        return parse(is);
    }
}
package net.bitpot.railways.parser;

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
    abstract public boolean parse(InputStream data);



    public boolean parseFile(String fileName) throws FileNotFoundException
    {
        File f = new File(fileName);

        return parseFile(f);
    }

    public boolean parseFile(File f) throws FileNotFoundException
    {
        FileInputStream is = new FileInputStream(f);

        return parse(is);
    }

}

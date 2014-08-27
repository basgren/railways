package net.bitpot.railways;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Basil Gren
 *         on 28.08.14.
 */
public class Utils {

    public static String readFile( String file ) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader(file));
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");
        String         line;

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }
}

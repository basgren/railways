package net.bitpot.railways.parser.route;

import java.util.ArrayList;

/**
 * Simple route action parser.
 *
 * @author Basil Gren
 *         on 21.02.14.
 */
public class RouteActionParser {

    public static RouteActionChunk[] parse(String subject) {
        ArrayList<RouteActionChunk> result = new ArrayList<RouteActionChunk>();
        if (!subject.trim().equals("")) {
            String[] chunks = subject.split("#", 2);
            result.add(new RouteActionChunk(chunks[0], RouteActionChunk.CONTAINER));

            if (chunks.length > 1) {
                result.add(new RouteActionChunk(chunks[1], RouteActionChunk.ACTION));
            }
        }

        return result.toArray(new RouteActionChunk[result.size()]);
    }
}

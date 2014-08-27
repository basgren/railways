package net.bitpot.railways.models;

import net.bitpot.railways.parser.RailsRoutesParser;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Testing RouteNode.
 */
public class RouteNodeTest
{
    private RouteNode buildRouteTreeFromFile(String filename) {
        try {
            RailsRoutesParser parser = new RailsRoutesParser();

            RouteList routes = parser.parseFile("test/data/treeview/" + filename);
            return RouteNode.buildTree(routes);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Test
    public void testOneNodeWithRoutes()
    {
        RouteNode node = buildRouteTreeFromFile("1_flat_list.txt");

        assertEquals("Root node has 3 routes",
                3, node.size());
    }
}
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


    @Test
    public void testNestedRoutes()
    {
        RouteNode root = buildRouteTreeFromFile("2_simple_nested_routes.txt");

        assertEquals("Root node has 2 child nodes", 2, root.size());

        // As we don't check sorting here, just iterate through nodes and check
        // if we have appropriate children.
        for(RouteNode node: root) {
            if (node.isContainer()) {
                assertEquals("clients", node.getTitle());
                assertEquals("Child should be a container", true, node.isContainer());
            } else {
                assertEquals("clients(.:format)", node.getTitle());
                assertEquals("Child should be a route", false, node.isContainer());
            }
        }
    }



    @Test
    public void testNestedRoutesSorting()
    {
        // /clients(.:format)
        // /clients/search(.:format)
        // /clients/new(.:format)
        // /clients/:id/edit(.:format)
        RouteNode root = buildRouteTreeFromFile("3_route_nodes_sorting.txt");
        RouteNode child;

        assertEquals("Root node has 2 child nodes", 2, root.size());

        // Check first child
        child = root.get(0);
        assertEquals("First child should be 'clients' container",
                "clients", child.getTitle());

        // Test sorting in child nodes
        assertEquals(":id",                 child.get(0).getTitle());
        assertEquals(":id(.:format)",       child.get(1).getTitle());
        assertEquals("new(.:format)",       child.get(2).getTitle());
        assertEquals("search(.:format)",    child.get(3).getTitle());


        // Check second child
        child = root.get(1);
        assertEquals("Second child should be 'clients(.:format)' route",
                "clients(.:format)", child.getTitle());
    }
}
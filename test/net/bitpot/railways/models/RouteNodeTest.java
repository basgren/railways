package net.bitpot.railways.models;

import net.bitpot.railways.parser.RailsRoutesParser;
import net.bitpot.railways.parser.RouteTreeBuilder;
import org.junit.Test;

import java.io.FileNotFoundException;

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

            return RouteTreeBuilder.buildTree(routes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Test
    public void testOneNodeWithRoutes()
    {
        RouteNode node = buildRouteTreeFromFile("1_flat_list.txt");

        assertEquals("Root node has 3 routes", 3, node.getChildCount());
    }


    @Test
    public void testNestedRoutes()
    {
        RouteNode root = buildRouteTreeFromFile("2_simple_nested_routes.txt");

        assertEquals("Root node has 2 child nodes", 2, root.getChildCount());

        // As we don't check sorting here, just iterate through nodes and check
        // if we have appropriate children.
        for(int i = 0; i < root.getChildCount(); i++) {
            RouteNode node = (RouteNode) root.getChildAt(i);

            if (node.isLeaf()) {
                assertEquals("clients(.:format)", node.getTitle());
                assertEquals("Child should be a route", true, node.isLeaf());
            } else {
                assertEquals("clients", node.getTitle());
                assertEquals("Child should be a container", false, node.isLeaf());
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

        assertEquals("Root node has 2 child nodes", 2, root.getChildCount());

        // Check first child
        child = (RouteNode) root.getChildAt(0);
        assertEquals("First child should be 'clients' container",
                "clients", child.getTitle());

        // Test sorting in child nodes
        assertEquals(":id",                 ((RouteNode)child.getChildAt(0)).getTitle());
        assertEquals(":id(.:format)",       ((RouteNode)child.getChildAt(1)).getTitle());
        assertEquals("new(.:format)",       ((RouteNode)child.getChildAt(2)).getTitle());
        assertEquals("search(.:format)",    ((RouteNode)child.getChildAt(3)).getTitle());


        // Check second child
        child = (RouteNode) root.getChildAt(1);
        assertEquals("Second child should be 'clients(.:format)' route",
                "clients(.:format)", child.getTitle());
    }
}
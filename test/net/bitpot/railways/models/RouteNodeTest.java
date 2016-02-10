package net.bitpot.railways.models;

import net.bitpot.railways.parser.RailsRoutesParser;
import net.bitpot.railways.parser.RouteTreeBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertNotNull(root.findNode("/"));

        RouteNode clientGroup = root.findGroup("clients");
        assertNotNull(clientGroup);

        assertEquals("Root node has 3 child nodes", 3, clientGroup.getChildCount());
    }


    @Test
    public void testNestedRoutesSorting()
    {
        // /clients(.:format)          clients#index
        // /clients/search(.:format)   clients#index
        // /clients/new(.:format)      clients#new
        // /clients/:id/edit(.:format) clients#edit
        // /clients/:id(.:format)      clients#show
        RouteNode root = buildRouteTreeFromFile("3_route_nodes_sorting.txt");
        assertNotNull(root);

        assertHasChild(root, "clients", 0, false);
        assertHasChild(root, "clients", 1, true);

        RouteNode clientsNode = (RouteNode)root.getChildAt(0);
        assertHasChild(clientsNode, ":id",      0, false);
        assertHasChild(clientsNode, ":id",      1, true);
        assertHasChild(clientsNode, "new",      2, true);
        assertHasChild(clientsNode, "search",   3, true);

        RouteNode idNode = (RouteNode)clientsNode.getChildAt(0);
        assertHasChild(idNode, "edit", 0, true);
    }

    private void assertHasChild(@NotNull RouteNode parent, String title,
                                int position, boolean isRoute) {
        assertTrue("No child node at position " + position, parent.getChildCount() > position);

        RouteNode child = (RouteNode)parent.getChildAt(position);
        assertNotNull("Expected child node at position" + position, child);
        assertEquals(String.format("Expected child node to have title \"%s\" at position %d",
                title, position), title, child.getTitle());

        String nodeType = isRoute ? "route" : "group";

        assertEquals(String.format("Expected child at position %d to be a %s", position, nodeType), child.isRoute(), isRoute);
    }
}
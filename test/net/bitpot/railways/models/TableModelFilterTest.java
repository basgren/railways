package net.bitpot.railways.models;

import net.bitpot.railways.parser.RailsRoutesParser;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;


/**
 * Testing table model filtration.
 */
public class TableModelFilterTest
{
    RailsRoutesParser parser = null;
    RouteTableModel model;


    @Before
    public void setUp() throws Exception
    {
        parser = new RailsRoutesParser();
        FileInputStream is = new FileInputStream("test/data/TableModelFilterTest.data.txt");
        RouteList routes = parser.parse(is);

        model = new RouteTableModel();
        model.setRoutes(routes);
    }


    @Test
    public void testSimpleFilter()
    {
        RoutesFilter filter = model.getFilter();
        filter.setFilterText("user");

        assertEquals(model.getRowCount(), 14);
    }


    @Test
    public void testMethodFilter()
    {
        RoutesFilter filter = model.getFilter();
        filter.setFilterText("#search");

        assertEquals(2, model.getRowCount());
    }


    @Test
    public void testCaseInsensitivity()
    {
        RoutesFilter filter = model.getFilter();
        filter.setFilterText("SEarcH");

        assertEquals(2, model.getRowCount());
    }

    @Test
    public void testWildcardSearch() {
        RoutesFilter filter = model.getFilter();
        filter.setFilterText("admin*:id");

        assertEquals(4, model.getRowCount());
    }

    @Test
    public void testFindMatchedString() {
        testFilterMatching("/books/:id/edit(.:format)", "boo*it", "books/:id/edit");
        testFilterMatching("/books/:id/edit(.:format)", "boo", "boo");
        testFilterMatching("/books/:id/edit(.:format)", "boo*", "boo");
        testFilterMatching("/books", "booo", "");
        testFilterMatching("/books(.:format)", "boo*(", "books(");
        testFilterMatching("/books/:id(.:format)", "boo*/:id(", "books/:id(");
        testFilterMatching("/boooks/:id(.:format)", "o.o", "");
        testFilterMatching("\\.[]{}()+-?^$|", "\\.[]{}()+-?^$|", "\\.[]{}()+-?^$|");
    }

    private void testFilterMatching(String subject, String filterText, String expected) {
        RoutesFilter filter = model.getFilter();
        filter.setFilterText(filterText);

        assertEquals(expected, filter.findMatchedString(subject));
    }

}
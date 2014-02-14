package net.bitpot.railways.models;

import net.bitpot.railways.parser.RailsRoutesParser;
import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;
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
        filter.setPathFilter("user");

        assertEquals(model.getRowCount(), 14);
    }


    @Test
    public void testMethodFilter()
    {
        RoutesFilter filter = model.getFilter();
        filter.setPathFilter("#search");

        assertEquals(model.getRowCount(), 2);
    }


    @Test
    public void testCaseInsensitivity()
    {
        RoutesFilter filter = model.getFilter();
        filter.setPathFilter("SEarcH");

        assertEquals(model.getRowCount(), 2);
    }
}
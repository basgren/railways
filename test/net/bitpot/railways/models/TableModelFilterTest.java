package net.bitpot.railways.models;

import net.bitpot.railways.parser.RailsRoutesParser;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileInputStream;

/**
 * Testing table model filtration.
 */
public class TableModelFilterTest extends Assert
{
    RailsRoutesParser parser = null;
    RouteTableModel model;


    @BeforeMethod
    public void setUp() throws Exception
    {
        parser = new RailsRoutesParser();
        FileInputStream is = new FileInputStream("test/data/TableModelFilterTest.data.txt");
        parser.parse(is);

        model = new RouteTableModel();
        model.setRoutes(parser.getRoutes());
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

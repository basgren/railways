package net.bitpot.railways.models;

import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;

/**
 * Table models.
 */
public class RouteTableModel extends AbstractTableModel {

    public final static int COL_PATH        = 0;
    public final static int COL_ACTION      = 1;
    public final static int COL_NAME        = 2;

    private RouteList myRouteList;
    private RouteList filteredRoutes;
    private RoutesFilter filter;


    public RouteTableModel() {
        myRouteList = new RouteList();
        filteredRoutes = new RouteList();

        filter = new RoutesFilter(this);
        filterChanged();
    }


    public void setRoutes(@Nullable RouteList routes) {
        if (routes == null)
            routes = new RouteList();

        this.myRouteList = routes;
        filterChanged();
    }


    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COL_PATH:
                return "Path";
            case COL_ACTION:
                return "Action";
            case COL_NAME:
                return "Name";
            default:
                return super.getColumnName(column);
        }
    }


    @Override
    public int getRowCount() {
        return filteredRoutes.size();
    }


    @Override
    public int getColumnCount() {
        return 3;
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Route route = filteredRoutes.get(rowIndex);

        switch (columnIndex) {
            case COL_PATH:
            case COL_ACTION:
                return route;

            case COL_NAME:
                return route.getRouteName();
        }

        return "";
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if ((columnIndex == COL_PATH) || (columnIndex == COL_ACTION))
            return Route.class;

        return super.getColumnClass(columnIndex);
    }


    public RoutesFilter getFilter() {
        return filter;
    }


    void filterChanged() {
        // Here we should update model.
        filteredRoutes.clear();

        if (!filter.isFilterActive()) {
            filteredRoutes.setSize(myRouteList.size());
            Collections.copy(filteredRoutes, myRouteList);
        } else {
            // Filter all elements
            for (Route route : myRouteList)
                if (filter.match(route))
                    filteredRoutes.add(route);
        }

        this.fireTableDataChanged();
    }

    /**
     * Returns route object associated with specified row.
     *
     * @param rowIndex Row index in model.
     */
    public Route getRoute(int rowIndex) {
        return filteredRoutes.get(rowIndex);
    }


    /**
     * Returns total number of routes that was successfully parsed.
     *
     * @return Number of parsed routes.
     */
    public int getTotalRoutesCount() {
        return myRouteList.size();
    }
}

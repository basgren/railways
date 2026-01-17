package net.bitpot.railways.actions;

import net.bitpot.railways.models.Route;
import net.bitpot.railways.utils.RailwaysUtils;

public class CopyRoutePathAction extends CopyRouteActionBase {

    @Override
    public String getRouteValue(Route route) {
        return RailwaysUtils.stripRequestFormat(route.getPath());
    }
}

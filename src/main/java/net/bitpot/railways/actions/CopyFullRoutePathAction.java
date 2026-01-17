package net.bitpot.railways.actions;

import net.bitpot.railways.models.Route;

public class CopyFullRoutePathAction extends CopyRouteActionBase {

    @Override
    public String getRouteValue(Route route) {
        return route.getPathWithMethod();
    }
}

package io.onemfive.data.route;

import io.onemfive.data.util.DequeStack;
import io.onemfive.data.util.Stack;

import java.util.Iterator;
import java.util.Map;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public final class DynamicRoutingSlip extends BaseRoute implements RoutingSlip {

    protected Stack<Route> routes = new DequeStack<>();
    private Boolean inProgress = false;

    public DynamicRoutingSlip() {}

    @Override
    public Integer numberRemainingRoutes() {
        return routes.numberRemainingRoutes();
    }

    @Override
    public Boolean inProgress() {
        return inProgress;
    }

    public void setInProgress(Boolean inProgress) {
        this.inProgress = inProgress;
    }

    @Override
    public void start() {
        inProgress = true;
    }

    @Override
    public Route nextRoute() {
        return routes.pop();
    }

    @Override
    public Route peekAtNextRoute() {
        return routes.peek();
    }

    public boolean addRoute(Route route) {
        route.setRouteId(getRouteId());
        routes.push(route);
        return true;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = super.toMap();
        if(routes!=null) {
            StringBuffer sb = new StringBuffer();
            Iterator<Route> i = routes.getIterator();
            Route r;
            while(i.hasNext()) {
                r = i.next();

            }
        }
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);

    }
}

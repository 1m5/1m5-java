/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.data.route;

import io.onemfive.data.Network;
import io.onemfive.data.NetworkPeer;
import io.onemfive.util.DequeStack;
import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;
import io.onemfive.util.Stack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public final class DynamicRoutingSlip extends BaseRoute implements RoutingSlip {

    private Logger LOG = Logger.getLogger(DynamicRoutingSlip.class.getName());

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
        if(inProgress!=null) m.put("inProgress",inProgress);
        if(routes!=null) {
            List<Map<String,Object>> rl = new ArrayList<>();
            StringBuffer sb = new StringBuffer();
            Iterator<Route> i = routes.getIterator();
            Route r;
            while(i.hasNext()) {
                r = i.next();
                rl.add(r.toMap());
            }
            m.put("routes", rl);
        }
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
        if(m.get("inProgress")!=null) inProgress = (Boolean)m.get("inProgress");
        if(m.get("routes")!=null) {
            // MUST iterate routes list backwards to ensure stack is built correctly
            routes = new DequeStack<>();
            List<Map<String,Object>> rl = (List<Map<String,Object>>)m.get("routes");
            Map<String,Object> rm;
            Route r;
            for( int i = rl.size() - 1 ; i >=0 ; i-- ){
                rm = rl.get(i);
                if(rm.get("type")!=null) {
                    String type = (String)rm.get("type");
                    try {
                        r = (Route)Class.forName(type).getConstructor().newInstance();
                        r.fromMap(rm);
                        routes.push(r);
                    } catch (Exception e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    @Override
    public String toJSON() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }

    @Override
    public void fromJSON(String json) {
        fromMap((Map<String,Object>)JSONParser.parse(json));
    }

    @Override
    public String toString() {
        return toJSON();
    }

//    public static void main(String[] args) {
//        NetworkPeer dest = new NetworkPeer(Network.I2P);
//        dest.getDid().getPublicKey().setAddress("1234");
//        NetworkPeer orig = new NetworkPeer(Network.I2P);
//        orig.getDid().getPublicKey().setAddress("1111");
//        DynamicRoutingSlip slip1 = new DynamicRoutingSlip();
//        SimpleExternalRoute r3 = new SimpleExternalRoute();
//        r3.setService("NetworkService3");
//        r3.setOperation("NetOp3");
//        r3.setDestination(dest);
//        r3.setOrigination(orig);
//        slip1.addRoute(r3);
//        SimpleRoute r2 = new SimpleRoute();
//        r2.setService("DIDService2");
//        r2.setOperation("DIDOp2");
//        slip1.addRoute(r2);
//        SimpleRoute r1 = new SimpleRoute();
//        r1.setService("KeyRingService1");
//        r1.setOperation("KeyOp1");
//        slip1.addRoute(r1);
//        String json = slip1.toJSON();
//        System.out.print(json);
//        DynamicRoutingSlip slip2 = new DynamicRoutingSlip();
//        slip2.fromJSON(json);
//        System.out.print(slip2);
//    }
}

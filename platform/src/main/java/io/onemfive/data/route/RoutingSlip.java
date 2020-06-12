package io.onemfive.data.route;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface RoutingSlip extends Route {

    Integer numberRemainingRoutes();

    Boolean inProgress();

    void start();

    Route nextRoute();

    Route peekAtNextRoute();
}

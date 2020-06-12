package io.onemfive.data;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
@FunctionalInterface
public interface Subscription {
    void notifyOfEvent(Envelope envelope);
}

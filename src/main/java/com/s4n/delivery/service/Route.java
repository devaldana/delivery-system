package com.s4n.delivery.service;

import com.google.common.base.Objects;

import static java.lang.String.format;

public class Route {

    private final String commands;
    private final Position pointOfDelivery;

    /**
     * Creates an instance of a Route with the given parameters.
     * @param commands the commands that, if followed starting in the origin (0, 0, N), will give
     *                 the point of delivery passed as second argument.
     * @param pointOfDelivery This is the point in which a package have to be delivered.
     *                        This point have to be reached if commands passed in the first argument
     *                        are followed from the origin (0, 0, N).
     */
    public Route(final String commands, final Position pointOfDelivery) {
        this.commands = commands;
        this.pointOfDelivery = pointOfDelivery;
    }

    public String getCommands() {
        return commands;
    }

    public Position getPointOfDelivery() {
        return pointOfDelivery;
    }

    @Override
    public String toString() {
        return format("Route {commands=%s, pointOfDelivery=%s}", commands, pointOfDelivery);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return Objects.equal(getCommands(), route.getCommands()) && Objects.equal(getPointOfDelivery(), route.getPointOfDelivery());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCommands(), getPointOfDelivery());
    }
}

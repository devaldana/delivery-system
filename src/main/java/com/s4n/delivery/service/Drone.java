package com.s4n.delivery.service;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;

import static com.s4n.delivery.service.Position.Orientation.N;
import static com.s4n.delivery.util.RouteUtils.calculateEndPosition;
import static java.lang.String.format;

public class Drone {

    public static final Position DEFAULT_START_POSITION = new Position(0, 0, N);
    private final List<Route> routes;
    private final List<Position> pointsOfDelivery = new LinkedList<>();
    private final String code;
    private Position position;

    /**
     * Creates an instance of a Drone with the given parameters.
     * @param code the code or identifier of the drone.
     * @param routes the routes to deliver each package as described in the loaded file.
     *               Note that the 'commands' of the routes passed here are NOT necessary the commands the
     *               drone will use to navigate to each package destination.
     *               All the 'commands' of the routes passed here are given for the scenario in which the
     *               drone have to deliver one package at a time returning to the origin even if the
     *               allowed load for the drone is superior to one (1) package. However, those 'commands'
     *               were used to calculate the point of delivery of each package, something that must NOT
     *               change regardless of the route taken to reach the destination. So, the most important
     *               part of each route in this list is the point of delivery set in the <strong>Route</strong>
     *               object. For more information read the documentation in <strong>Route</strong> class.
     */
    public Drone(final String code, final List<Route> routes) {
        this.code = code;
        this.routes = ImmutableList.copyOf(routes);
        this.position = DEFAULT_START_POSITION;
    }

    public Position getPosition() {
        return position;
    }

    /**
     * Method used to deliver a package using the given commands starting in the current position.
     * Each time the drone deliver a package, it saves the delivery position for the record.
     * @param commands the commands to be used in navigation.
     */
    public void deliver(final String commands) {
        position = calculateEndPosition(commands, position);
        pointsOfDelivery.add(position);
    }

    /**
     * Set the drone in the given position.
     * @param position to be reached by the drone.
     */
    public void navigate(final Position position) {
        this.position = position;
    }

    public List<Route> getRoutes() {
        return ImmutableList.copyOf(routes);
    }

    public List<Position> getPointsOfDelivery() {
        return ImmutableList.copyOf(pointsOfDelivery);
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return format("Drone {code='%s', pointsOfDelivery=%s, position=%s}", code, pointsOfDelivery, position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Drone)) return false;
        Drone drone = (Drone) o;
        return Objects.equal(code, drone.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}

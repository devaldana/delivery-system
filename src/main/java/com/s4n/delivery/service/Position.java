package com.s4n.delivery.service;

import com.google.common.base.Objects;

import static java.lang.String.format;

public class Position {

    private final int x;
    private final int y;
    private final Orientation orientation;

    public Position(final int x, final int y, final Orientation orientation) {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public enum Orientation {
        E(1, "East"), N(2, "North"), W(3, "West"), S(4, "South");
        private final int value;
        private final String name;

        Orientation(final int value, final String name) {
            this.value = value;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int difference(final Orientation orientation) {
            return orientation != null ? this.value - orientation.value : 0;
        }
    }

    public String getDescription() {
        return format("(%s, %s) %s orientation", x, y, orientation.getName());
    }

    @Override
    public String toString() {
        return format("Position {x=%s, y=%s, orientation=%s}", x, y, orientation);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        final Position position = (Position) o;
        return getX() == position.getX() && getY() == position.getY() && getOrientation() == position.getOrientation();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getX(), getY(), getOrientation());
    }
}
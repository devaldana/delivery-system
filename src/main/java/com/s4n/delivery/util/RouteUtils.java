package com.s4n.delivery.util;

import com.s4n.delivery.service.Position;
import com.s4n.delivery.service.Position.Orientation;

import static java.lang.Math.abs;
import static java.util.stream.IntStream.range;

public final class RouteUtils {

    private RouteUtils() {
        // As utility class no instances are required
    }

    /**
     * Method that calculates the series of commands that a Drone have to navigate to reach
     * a point 'B' starting from a point 'A'. This is very useful because it helps to reduce the need
     * for get back to the origin point to start delivering the next package. It improves the efficient
     * of the delivery process.
     *
     * For example: given the starting point (-2, 4, W) and the destination point (-1, -1, null), the
     *              returned command will be 'DDADAAAAA'. Now we have a commands that connect those two
     *              points.
     *
     * Note: A - represents a forward movement of one unit.
     *       D - represents a gyro to the right on its current position. So, no changes in the axes values.
     *       I - represents a gyro to the left on its current position. So, no changes in the axes values.
     *
     * @param start the point from which the route will be calculated.
     * @param end the destination point.
     * @return the commands that connect the start point with the end point.
     */
    public static String calculateRouteCommands(final Position start, final Position end) {
        final StringBuilder commandBuilder = new StringBuilder();
        final int x = start.getX() - end.getX();
        final int y = start.getY() - end.getY();
        int gyros = 0;
        Orientation currentOrientation = start.getOrientation();

        if (x > 0) {
            gyros = currentOrientation.difference(Orientation.W);
            currentOrientation = Orientation.W;
        } else if (x < 0) {
            gyros = currentOrientation.difference(Orientation.E);
            currentOrientation = Orientation.E;
        } else {
            if (y < 0 && currentOrientation == Orientation.S) {
                gyros = 2;
                currentOrientation = Orientation.N;
            } else if (y > 0 && currentOrientation == Orientation.N) {
                gyros = 2;
                currentOrientation = Orientation.S;
            }
        }

        if (gyros > 0) {
            range(0, abs(gyros)).forEach(n -> commandBuilder.append('D'));
        } else {
            range(0, abs(gyros)).forEach(n -> commandBuilder.append('I'));
        }

        range(0, abs(x)).forEach(n -> commandBuilder.append('A'));

        if (currentOrientation == Orientation.W) {
            if (y > 0) {
                commandBuilder.append('I');
            } else if (y < 0) {
                commandBuilder.append('D');
            }
        } else if(currentOrientation == Orientation.E) {
            if (y > 0) {
                commandBuilder.append('D');
            } else if (y < 0) {
                commandBuilder.append('I');
            }
        }

        range(0, abs(y)).forEach(n -> commandBuilder.append('A'));
        return commandBuilder.toString();
    }

    /**
     * Method that uses the given commands to calculate the end position starting from the given current position.
     * For example: given the commands 'AAADAA' and the start position '(0, 0, N)' the returned-calculated position
     *              will be '(2, 3, E)'.
     * @param commands the commands used to calculate the end position.
     * @param startPosition the point from which the end position will be calculated.
     * @return the calculated position.
     */
    public static Position calculateEndPosition(final String commands, final Position startPosition) {
        Position position = startPosition;
        final char[] commandsArr = commands.toCharArray();
        for (char command : commandsArr) {
            switch (command) {
                case 'A':
                    switch (position.getOrientation()) {
                        case N:
                            position = new Position(position.getX(), position.getY() + 1, position.getOrientation());
                            break;
                        case E:
                            position = new Position(position.getX() + 1, position.getY(), position.getOrientation());
                            break;
                        case W:
                            position = new Position(position.getX() - 1, position.getY(), position.getOrientation());
                            break;
                        case S:
                            position = new Position(position.getX(), position.getY() - 1, position.getOrientation());
                            break;
                    }
                    break;
                case 'I':
                    switch (position.getOrientation()) {
                        case N:
                            position = new Position(position.getX(), position.getY(), Orientation.W);
                            break;
                        case E:
                            position = new Position(position.getX(), position.getY(), Orientation.N);
                            break;
                        case W:
                            position = new Position(position.getX(), position.getY(), Orientation.S);
                            break;
                        case S:
                            position = new Position(position.getX(), position.getY(), Orientation.E);
                            break;
                    }
                    break;
                case 'D':
                    switch (position.getOrientation()) {
                        case N:
                            position = new Position(position.getX(), position.getY(), Orientation.E);
                            break;
                        case E:
                            position = new Position(position.getX(), position.getY(), Orientation.S);
                            break;
                        case W:
                            position = new Position(position.getX(), position.getY(), Orientation.N);
                            break;
                        case S:
                            position = new Position(position.getX(), position.getY(), Orientation.W);
                            break;
                    }
                    break;
            }
        }
        return position;
    }
}

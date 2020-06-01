package com.s4n.delivery.service;

import com.google.common.collect.Lists;
import com.s4n.delivery.app.Config;
import com.s4n.delivery.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.s4n.delivery.app.Constants.AVAILABLE_DRONES_PROPERTY_NAME;
import static com.s4n.delivery.app.Constants.INPUT_FILES_NAME_PREFIX_PROPERTY_NAME;
import static com.s4n.delivery.app.Constants.LOAD_PROPERTY_NAME;
import static com.s4n.delivery.app.Constants.MAX_OPERATION_RADIO_PROPERTY_NAME;
import static com.s4n.delivery.app.Constants.OUTPUT_FILES_EXTENSION_PROPERTY_NAME;
import static com.s4n.delivery.app.Constants.OUTPUT_FILES_NAME_PREFIX_PROPERTY_NAME;
import static com.s4n.delivery.app.Constants.OUTPUT_FOLDER_PATH_PROPERTY_NAME;
import static com.s4n.delivery.service.Drone.DEFAULT_START_POSITION;
import static com.s4n.delivery.util.FileUtils.getFileNameWithNoPrefixNoExtension;
import static com.s4n.delivery.util.RouteUtils.calculateEndPosition;
import static com.s4n.delivery.util.RouteUtils.calculateRouteCommands;
import static java.lang.Math.abs;
import static java.nio.file.Files.readAllLines;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger logger = LogManager.getLogger(DeliveryServiceImpl.class);
    private final Config config;
    private Set<Drone> drones;

    public DeliveryServiceImpl(final Config config) {
        this.config = config;
    }

    /**
     * Method that map each file to a drone and:
     *  - sets the drone code taking the name of the file and removing the extension and the configured prefix.
     *  - filter the empty files, so empty files will be ignored.
     *  - at the moment of map each line of a file to a <strong>Route</strong> it calculates the delivery point
     *    starting from the origin (0, 0, N) using the given commands.
     *
     * @param inputFiles the list of files previously filtered by the configured extensions.
     */
    @Override
    public void collect(final Set<File> inputFiles) {
        validateOperationCapacity(inputFiles);
        drones = inputFiles.parallelStream().filter(FileUtils::fileIsNotEmpty).map(this::loadDrone).collect(toSet());
    }

    /**
     * Method that start the process of delivering all the packages using all the loaded drones.
     * Some facts:
     *  - The delivery process start in parallel, that is, several drones start delivering at the same time.
     *  - The implemented mechanism take each drone, divide the number of packages that it have to deliver by the
     *    maximum allowed load, and that results is the number of tours the drone have to do in the day. So, the
     *    drone does the work in the most efficient way.
     *  - Each drone takes off with the maximum allowed/configured load and does NOT return to the delivery center
     *    without end first the delivery of all the loaded packages. To achieve that, uses an algorithm that calculates
     *    the commands to navigate from the point of the first delivery to the second delivery point, and from the
     *    second to the third and so on.
     */
    @Override
    public void deliver() {
        final Optional<Integer> maxLoadPerDrone = config.getPropertyAsInteger(LOAD_PROPERTY_NAME);
        if (maxLoadPerDrone.isPresent()) {
            drones.parallelStream().forEach(
                    drone -> Lists.partition(drone.getRoutes(), maxLoadPerDrone.get()).forEach(
                            routeWithMaxLoad -> {
                                routeWithMaxLoad.forEach(route -> {
                                    if (isThePositionWithinLimits(route.getPointOfDelivery())) {
                                        final String commands = calculateRouteCommands(drone.getPosition(), route.getPointOfDelivery());
                                        drone.deliver(commands);
                                    } else {
                                        logger.error("Position is out of the bounds: {}", route.getPointOfDelivery());
                                        throw new IllegalArgumentException("The drone position is out of the bounds");
                                    }
                                });
                                drone.navigate(DEFAULT_START_POSITION);
                            }
                    )
            );
        } else {
            throw new IllegalStateException("Basic properties missing in config, please validate.");
        }
    }

    /**
     * Method that generate all the report files and save them in the configured output folder.
     * Note that at the end of the delivery process all the drones have the list of the delivery
     * positions that will be used to generate each report.
     * Also note that each drone have a code that will be used to create the file name that keep
     * relation with the given input file for each specific drone.
     * This process is done in parallel trying to take advantage of all the available resources.
     */
    @Override
    public void generateReports() {
        logger.info("Generating reports for {} drones", drones.size());
        final Optional<String> outputFolderPath = config.getPropertyAsString(OUTPUT_FOLDER_PATH_PROPERTY_NAME);
        if (outputFolderPath.isPresent()) {
            final Path folderPath = Paths.get(outputFolderPath.get());
            drones.parallelStream().forEach(drone -> writeReportFile(drone, folderPath));
        } else {
            throw new IllegalStateException("Basic properties missing in config, please validate.");
        }
    }

    private void writeReportFile(final Drone drone, final Path folderPath) {
        try (final BufferedWriter writer = Files.newBufferedWriter(getFileForReport(drone, folderPath))) {
            writer.write("== Delivery Report ==");
            writer.newLine();
            for (final Position position : drone.getPointsOfDelivery()) {
                writer.write(position.getDescription());
                writer.newLine();
            }
        } catch (final IOException exception) {
            logger.warn("Error writing report to folder '{}' for drone with code={}", folderPath, drone.getCode());
            logger.error("Exception: ", exception);
        }
    }

    private Path getFileForReport(final Drone drone, final Path folderPath) {
        final String fileName = buildReportFileName(drone);
        return Paths.get(folderPath.toString() + File.separator + fileName);
    }

    private String buildReportFileName(final Drone drone) {
        final Optional<String> extension = config.getPropertyAsString(OUTPUT_FILES_EXTENSION_PROPERTY_NAME);
        final Optional<String> fileNamePrefix = config.getPropertyAsString(OUTPUT_FILES_NAME_PREFIX_PROPERTY_NAME);
        if (extension.isPresent() && fileNamePrefix.isPresent()) {
            return fileNamePrefix.get() + drone.getCode() + "." + extension.get();
        } else {
            throw new IllegalStateException("Basic properties missing in config, please validate.");
        }
    }

    private Drone loadDrone(final File file) {
        final String code = getCode(file);
        final List<Route> routes = getRoutes(file);
        return new Drone(code, routes);
    }

    private String getCode(final File file) {
        final Optional<String> fileNamePrefix = config.getPropertyAsString(INPUT_FILES_NAME_PREFIX_PROPERTY_NAME);
        if (fileNamePrefix.isPresent()) {
            return getFileNameWithNoPrefixNoExtension(file, fileNamePrefix.get());
        } else {
            throw new IllegalStateException("Basic properties missing in config, please validate.");
        }
    }

    private boolean isThePositionWithinLimits(final Position positionOfDelivery) {
        final Optional<Integer> maxOperationRadio = config.getPropertyAsInteger(MAX_OPERATION_RADIO_PROPERTY_NAME);
        if (maxOperationRadio.isPresent() && nonNull(positionOfDelivery)) {
            final int maxRadio = maxOperationRadio.get();
            return abs(positionOfDelivery.getX()) <= maxRadio || abs(positionOfDelivery.getY()) <= maxRadio;
        } else {
            return false;
        }
    }

    private List<Route> getRoutes(final File file) {
        try {
            return readAllLines(file.toPath()).stream().map(this::createRoute).collect(toList());
        } catch (final IOException e) {
            logger.error("Exception occurred while trying to read file: {}", file);
            return emptyList();
        }
    }

    private Route createRoute(final String commands) {
        final Position pointOfDelivery = calculateEndPosition(commands, DEFAULT_START_POSITION);
        return new Route(commands, pointOfDelivery);
    }

    private void validateOperationCapacity(final Set<File> inputFiles) {
        final Optional<Integer> numberOfAvailableDrones = config.getPropertyAsInteger(AVAILABLE_DRONES_PROPERTY_NAME);
        if (numberOfAvailableDrones.isPresent()) {
            if (isNotEmpty(inputFiles)) {
                if (inputFiles.size() > numberOfAvailableDrones.get()) {
                    throw new IllegalArgumentException("The load is more than expected for the current fleet of drones.");
                }
            } else {
                throw new IllegalArgumentException("No files in the specified folder, please validate.");
            }
        } else {
            throw new IllegalStateException("Basic properties missing in config, please validate.");
        }
    }
}

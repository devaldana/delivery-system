package com.s4n.delivery.app;

import com.s4n.delivery.service.DeliveryService;
import com.s4n.delivery.service.DeliveryServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import static com.s4n.delivery.app.Constants.DEFAULT_CONFIG_FILE;
import static com.s4n.delivery.util.FileUtils.getResourceAsInputStream;
import static com.s4n.delivery.util.FileUtils.loadAllInputFiles;

public class Application {

    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(final String[] args) {
        logger.info("Starting app with args={}", Arrays.toString(args));
        final Config config = getConfiguration();
        final Set<File> allInputFiles = loadAllInputFiles(config);
        final DeliveryService deliveryService = new DeliveryServiceImpl(config);

        deliveryService.collect(allInputFiles);
        deliveryService.deliver();
        deliveryService.generateReports();
    }

    private static Config getConfiguration() {
        logger.info("Loading configuration from properties file.");
        final InputStream configFile = getResourceAsInputStream(DEFAULT_CONFIG_FILE);
        return new Config(configFile);
    }
}

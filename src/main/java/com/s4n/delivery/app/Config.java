package com.s4n.delivery.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class Config {

    private static final Logger logger = LogManager.getLogger(Config.class);
    private final Properties properties = new Properties();

    public Config(final InputStream file) {
        init(file);
    }

    private void init(final InputStream propertiesFile) {
        try {
            properties.load(propertiesFile);
        } catch (final Exception exception) {
            logger.error("Error loading config properties file", exception);
        }
    }

    public Optional<String> getPropertyAsString(final String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    public int size() {
        return properties.size();
    }

    public Optional<Integer> getPropertyAsInteger(final String key) {
        try {
            return Optional.of(Integer.valueOf(properties.getProperty(key)));
        } catch (final Exception exception) {
            logger.warn("The '{}' property was NOT found or an exception was thrown " +
                        "while converting to Integer. Returning empty value.", key);
            return Optional.empty();
        }
    }
}

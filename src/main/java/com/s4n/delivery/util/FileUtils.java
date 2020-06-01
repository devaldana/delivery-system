package com.s4n.delivery.util;

import com.s4n.delivery.app.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Streams.stream;
import static com.s4n.delivery.app.Constants.INPUT_FILES_EXTENSION_PROPERTY_NAME;
import static com.s4n.delivery.app.Constants.INPUT_FOLDER_PATH_PROPERTY_NAME;
import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.apache.commons.io.FilenameUtils.getBaseName;

public final class FileUtils {

    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    private FileUtils() {
        // As utility class no instances are required
    }

    public static boolean fileIsNotEmpty(final File file) {
        return nonNull(file) && file.length() > 0;
    }

    public static String getFileNameWithNoPrefixNoExtension(final File file, final String fileNamePrefix) {
        final String fileName = getBaseName(file.getName());
        return fileName.substring(fileNamePrefix.length());
    }

    public static InputStream getResourceAsInputStream(final String path) {
        logger.debug("Trying to load resource from '{}'", path);
        return FileUtils.class.getClassLoader().getResourceAsStream(path);
    }

    public static Set<File> loadAllInputFiles(final Config config) {
        logger.debug("Loading all input files.");
        final Optional<String> inputFolderPath = config.getPropertyAsString(INPUT_FOLDER_PATH_PROPERTY_NAME);
        final Optional<String> inputFilesExtension = config.getPropertyAsString(INPUT_FILES_EXTENSION_PROPERTY_NAME);
        if (inputFolderPath.isPresent() && inputFilesExtension.isPresent()) {
            final File startDirectory = Paths.get(inputFolderPath.get()).toFile();
            return stream(iterateFiles(startDirectory, new String[]{inputFilesExtension.get()}, false)).collect(toSet());
        }
        return emptySet();
    }
}

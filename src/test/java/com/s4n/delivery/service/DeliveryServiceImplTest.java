package com.s4n.delivery.service;

import com.s4n.delivery.app.Config;
import com.s4n.delivery.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;

import static com.s4n.delivery.app.Constants.DEFAULT_CONFIG_FILE;
import static com.s4n.delivery.service.Position.Orientation.E;
import static com.s4n.delivery.service.Position.Orientation.N;
import static com.s4n.delivery.service.Position.Orientation.S;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeliveryServiceImplTest {

    private Config config;
    private Set<File> allInputFiles;
    private DeliveryService deliveryService;

    @Before
    public void setUp() {
        config = new Config(FileUtils.getResourceAsInputStream(DEFAULT_CONFIG_FILE));
        allInputFiles = FileUtils.loadAllInputFiles(config);
        deliveryService = new DeliveryServiceImpl(config);
        deliveryService.collect(allInputFiles);
        deliveryService.deliver();
        deliveryService.generateReports();
    }

    @Test
    public void validateLoadedConfiguration() {
        assertTrue(nonNull(config));
        assertEquals(10, config.size());
        assertTrue(config.getPropertyAsString("DEVELOPER").isPresent());
        assertEquals("devspods.com", config.getPropertyAsString("DEVELOPER").get());
    }

    @Test
    public void validateAllInputFiles() {
        assertTrue(isNotEmpty(allInputFiles));
        assertEquals(3, allInputFiles.size());

        File in03 = allInputFiles.stream().filter(file -> file.getName().startsWith("in03")).findFirst().get();
        assertFalse("It should be false as 'in03.txt' file is empty", FileUtils.fileIsNotEmpty(in03));
    }

    @Test
    public void validateDeliveryPositions() throws Exception{
        Set<Drone> drones = (Set<Drone>) getPrivateFieldValue("drones", deliveryService);
        assertEquals(2, drones.size());
        // ^^ Even though there are three files in 'inputs' folder, just two of them have with content
        // so, just two drones were loaded.

        // Validate delivery positions for Drone with code '01'
        Drone drone01 = drones.stream().filter(drone -> drone.getCode().equals("01")).findFirst().get();
        assertEquals(new Position(-2, 4, N), drone01.getPointsOfDelivery().get(0));
        assertEquals(new Position(-1, -1, S), drone01.getPointsOfDelivery().get(1));
        assertEquals(new Position(-1, 3, N), drone01.getPointsOfDelivery().get(2));
        assertEquals(new Position(0, 10, N), drone01.getPointsOfDelivery().get(3));
        assertEquals(new Position(0, 0, N), drone01.getPosition()); // End position of the drone should be origin

        // Validate delivery positions for Drone with code '02'
        Drone drone02 = drones.stream().filter(drone -> drone.getCode().equals("02")).findFirst().get();
        assertEquals(new Position(-4, -1, S), drone02.getPointsOfDelivery().get(0));
        assertEquals(new Position(-5, -3, S), drone02.getPointsOfDelivery().get(1));
        assertEquals(new Position(-3, -3, E), drone02.getPointsOfDelivery().get(2));
        assertEquals(new Position(0, 0, N), drone02.getPosition());  // End position of the drone should be origin
    }

    private static Object getPrivateFieldValue(final String fieldName, final Object object) throws Exception {
        final Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
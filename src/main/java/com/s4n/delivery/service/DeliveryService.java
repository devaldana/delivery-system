package com.s4n.delivery.service;

import java.io.File;
import java.util.Set;

public interface DeliveryService {
    void collect(Set<File> inputFiles);
    void deliver();
    void generateReports();
}

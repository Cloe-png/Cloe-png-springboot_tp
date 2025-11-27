package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import com.sun.management.OperatingSystemMXBean;
import com.example.demo.service.SystemMetricsService;
@RestController
public class SystemMetricsController {

    private final SystemMetricsService systemMetricsService;

    public SystemMetricsController(SystemMetricsService systemMetricsService) {
        this.systemMetricsService = systemMetricsService;
    }

    @GetMapping("/metrics")
    public Map<String, Object> getSystemMetrics() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Récupérer l'utilisation du CPU
        double cpuLoad = osBean.getSystemCpuLoad() * 100;

        // Récupérer l'utilisation de la mémoire
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        long freeMemory = osBean.getFreePhysicalMemorySize();
        long usedMemory = totalMemory - freeMemory;

        // Convertir la mémoire en mégaoctets (MO)
        long totalMemoryMB = totalMemory / (1024 * 1024);
        long usedMemoryMB = usedMemory / (1024 * 1024);
        long freeMemoryMB = freeMemory / (1024 * 1024);

        // Insérer les données CPU/RAM dans la base de données
        int cpuRamId = systemMetricsService.insertCpuRamMetrics(cpuLoad, totalMemoryMB, usedMemoryMB, freeMemoryMB);

        // Récupérer l'utilisation des disques durs
        File[] roots = File.listRoots();
        Map<String, Object> diskMetrics = new HashMap<>();
        for (File root : roots) {
            long totalDiskSpace = root.getTotalSpace() / (1024 * 1024);
            long freeDiskSpace = root.getFreeSpace() / (1024 * 1024);
            long usedDiskSpace = totalDiskSpace - freeDiskSpace;
            double diskUsagePercentage = (double) usedDiskSpace / totalDiskSpace * 100;

            Map<String, Object> diskInfo = new HashMap<>();
            diskInfo.put("totalDiskSpaceMB", totalDiskSpace);
            diskInfo.put("usedDiskSpaceMB", usedDiskSpace);
            diskInfo.put("freeDiskSpaceMB", freeDiskSpace);
            diskInfo.put("diskUsagePercentage", diskUsagePercentage);

            diskMetrics.put(root.getAbsolutePath(), diskInfo);

            // Insérer les données disque dans la base de données
            systemMetricsService.insertDiskMetrics(cpuRamId, root.getAbsolutePath(), totalDiskSpace, usedDiskSpace, freeDiskSpace, diskUsagePercentage);
        }

        // Stocker les résultats dans une Map
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpuUsage", cpuLoad);
        metrics.put("totalMemoryMB", totalMemoryMB);
        metrics.put("usedMemoryMB", usedMemoryMB);
        metrics.put("freeMemoryMB", freeMemoryMB);
        metrics.put("diskMetrics", diskMetrics);

        return metrics;
    }
}

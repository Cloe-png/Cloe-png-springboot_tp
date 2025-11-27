package com.example.demo.controller;
import com.example.demo.service.SystemMetricsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.sun.management.OperatingSystemMXBean;
@RestController
public class SystemMetricsController {

    private final SystemMetricsService systemMetricsService;

    public SystemMetricsController(SystemMetricsService systemMetricsService) {
        this.systemMetricsService = systemMetricsService;
    }

    @GetMapping("/metrics")
    public Map<String, Object> getSystemMetrics() {
        // Récupérer les métriques système
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemCpuLoad() * 100;
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        long freeMemory = osBean.getFreePhysicalMemorySize();
        long usedMemory = totalMemory - freeMemory;

        // Convertir en Mo
        long totalMemoryMB = totalMemory / (1024 * 1024);
        long usedMemoryMB = usedMemory / (1024 * 1024);
        long freeMemoryMB = freeMemory / (1024 * 1024);

        // Enregistrer les métriques CPU/RAM en base
        int cpuRamId = systemMetricsService.insertCpuRamMetrics(cpuLoad, totalMemoryMB, usedMemoryMB, freeMemoryMB);

        // Récupérer les métriques disques
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

            // Enregistrer les métriques disque en base
            systemMetricsService.insertDiskMetrics(cpuRamId, root.getAbsolutePath(), totalDiskSpace, usedDiskSpace, freeDiskSpace, diskUsagePercentage);
        }

        // Retourner les métriques
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpuUsage", cpuLoad);
        metrics.put("totalMemoryMB", totalMemoryMB);
        metrics.put("usedMemoryMB", usedMemoryMB);
        metrics.put("freeMemoryMB", freeMemoryMB);
        metrics.put("diskMetrics", diskMetrics);

        return metrics;
    }
}

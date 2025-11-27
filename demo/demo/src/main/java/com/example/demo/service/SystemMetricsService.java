package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.sql.*;

@Service
public class SystemMetricsService {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    public int insertCpuRamMetrics(double cpuUsage, long totalMemoryMB, long usedMemoryMB, long freeMemoryMB) {
        String query = "INSERT INTO system_metrics_cpu_ram (cpu_usage, total_memory_mb, used_memory_mb, free_memory_mb) VALUES (?, ?, ?, ?)";
        int id = -1;
        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
             PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, cpuUsage);
            pstmt.setLong(2, totalMemoryMB);
            pstmt.setLong(3, usedMemoryMB);
            pstmt.setLong(4, freeMemoryMB);
            pstmt.executeUpdate();

            try (var rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void insertDiskMetrics(int cpuRamId, String diskPath, long totalDiskSpaceMB, long usedDiskSpaceMB, long freeDiskSpaceMB, double diskUsagePercentage) {
        String query = "INSERT INTO system_metrics_disk (disk_path, total_disk_space_mb, used_disk_space_mb, free_disk_space_mb, disk_usage_percentage, cpu_ram_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, diskPath);
            pstmt.setLong(2, totalDiskSpaceMB);
            pstmt.setLong(3, usedDiskSpaceMB);
            pstmt.setLong(4, freeDiskSpaceMB);
            pstmt.setDouble(5, diskUsagePercentage);
            pstmt.setInt(6, cpuRamId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

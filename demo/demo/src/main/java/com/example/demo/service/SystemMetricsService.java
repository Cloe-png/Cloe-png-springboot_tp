package com.example.demo.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


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

    /**
     * Insère les métriques CPU/RAM dans la base de données.
     * @param cpuUsage Pourcentage d'utilisation du CPU.
     * @param totalMemoryMB Mémoire totale en Mo.
     * @param usedMemoryMB Mémoire utilisée en Mo.
     * @param freeMemoryMB Mémoire libre en Mo.
     * @return L'ID généré pour l'enregistrement CPU/RAM.
     */
    public int insertCpuRamMetrics(double cpuUsage, long totalMemoryMB, long usedMemoryMB, long freeMemoryMB) {
        String query = "INSERT INTO system_metrics_cpu_ram (cpu_usage, total_memory_mb, used_memory_mb, free_memory_mb) VALUES (?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, cpuUsage);
            pstmt.setLong(2, totalMemoryMB);
            pstmt.setLong(3, usedMemoryMB);
            pstmt.setLong(4, freeMemoryMB);

            pstmt.executeUpdate();

            // Récupère l'ID généré automatiquement
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion des métriques CPU/RAM : " + e.getMessage());
            e.printStackTrace();
        }

        return generatedId;
    }

    /**
     * Insère les métriques de disque dans la base de données.
     * @param cpuRamId ID de l'enregistrement CPU/RAM associé.
     * @param diskPath Chemin du disque.
     * @param totalDiskSpaceMB Espace total du disque en Mo.
     * @param usedDiskSpaceMB Espace utilisé du disque en Mo.
     * @param freeDiskSpaceMB Espace libre du disque en Mo.
     * @param diskUsagePercentage Pourcentage d'utilisation du disque.
     */
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
            System.err.println("Erreur lors de l'insertion des métriques disque : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

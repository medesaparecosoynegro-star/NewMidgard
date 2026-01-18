package me.ray.midgard.core.database;

import java.io.File;

public record DatabaseCredentials(
    String type,
    String host,
    int port,
    String database,
    String username,
    String password,
    boolean useSsl
) {
    public String toJdbcUrl(File dataFolder) {
        if (type.equalsIgnoreCase("sqlite")) {
            return "jdbc:sqlite:" + new File(dataFolder, database + ".db").getAbsolutePath();
        }
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=true&characterEncoding=UTF-8",
            host, port, database, useSsl);
    }
}

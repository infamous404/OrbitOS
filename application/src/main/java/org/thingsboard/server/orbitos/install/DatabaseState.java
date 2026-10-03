// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.install;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Used by the container entrypoint before starting the server: is the database already installed?
 * Exit code 0 = installed, 10 = empty (run the installer), 11 = unfinished install, 1 = not reachable.
 * Connection settings come from SPRING_DATASOURCE_URL, _USERNAME and _PASSWORD.
 */
public final class DatabaseState {

    public static final int INSTALLED = 0;
    public static final int EMPTY = 10;
    public static final int PARTIAL = 11;
    public static final int UNREACHABLE = 1;

    private DatabaseState() {
    }

    public static void main(String[] args) throws Exception {
        String url = System.getenv("SPRING_DATASOURCE_URL");
        String user = System.getenv("SPRING_DATASOURCE_USERNAME");
        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        Class.forName("org.postgresql.Driver");
        String lastError = "";
        for (int attempt = 0; attempt < 60; attempt++) {
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT to_regclass('public.tb_schema_settings') IS NOT NULL")) {
                rs.next();
                if (!rs.getBoolean(1)) {
                    System.out.println("Database: empty");
                    System.exit(EMPTY);
                }
                try (ResultSet admins = statement.executeQuery("SELECT count(*) FROM tb_user WHERE authority = 'SYS_ADMIN'")) {
                    admins.next();
                    if (admins.getLong(1) == 0) {
                        System.out.println("Database: an earlier installation did not finish. To start again from an empty database, run: docker compose down -v  then: docker compose up -d  (this deletes the unfinished database only).");
                        System.exit(PARTIAL);
                    }
                }
                System.out.println("Database: installed");
                System.exit(INSTALLED);
            } catch (SQLException e) {
                lastError = e.getMessage();
                Thread.sleep(2000);
            }
        }
        System.out.println("Database not reachable: " + lastError);
        System.exit(UNREACHABLE);
    }

}

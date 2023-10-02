package com.scrapium;

//import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DataSource dataSource;

    /*
    static {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/scrapium_proxies");
        ds.setUsername("scrapium_user");
        ds.setPassword("6F3dNfvz3eL3Vb3ol");
        ds.setInitialSize(5); // Set the initial number of connections in the pool
        ds.setMaxTotal(10); // Set the maximum number of connections in the pool
        dataSource = ds;
    } */

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
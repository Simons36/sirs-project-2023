package GrooveServer.service.connection_pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionPool {
    
    private BasicDataSource ds = new BasicDataSource();

    public ConnectionPool(String url, String username, String password, int connectionTimeout){
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMinIdle(10);
        ds.setMaxIdle(20);
        ds.setMaxOpenPreparedStatements(100);
        ds.setMaxWait(Duration.ofSeconds(5));
        ds.setRemoveAbandonedOnBorrow(true);
        ds.setRemoveAbandonedOnMaintenance(true);
        ds.setLogAbandoned(false);
        ds.setRemoveAbandonedTimeout(Duration.ofMillis(10));
        ds.setConnectionProperties("connectTimeout=" + connectionTimeout);
    }
    
    public void checkConnection() throws SQLException {
        ds.getConnection().close();
    }
    
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void closeAllConnections(){
        try {
            ds.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    
}
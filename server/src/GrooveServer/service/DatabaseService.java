package GrooveServer.service;
import java.io.IOException;
import java.sql.*;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import GrooveServer.service.connection_pool.ConnectionPool;
import GrooveServer.service.exceptions.AudioTransferConnectionException;
import GrooveServer.service.exceptions.AudioTransferErrorException;
import GrooveServer.service.exceptions.DatabaseConnectionException;
import GrooveServer.service.exceptions.DatabaseExecStatementException;
import GrooveServer.objects.DatabaseServiceConfigStruct;
import GrooveServer.service.audio_transfer.AudioTransfer;

public class DatabaseService {

    private static final String URL_BEGINNING = "jdbc:mysql://";
    private static final int MY_SQL_PORT = 3306;
    private static final int CONNECTION_TIMEOUT = 5;

    private ConnectionPool _connectionPool;
    private AudioTransfer _audioTransfer;


    /**
     * Creates a DatabaseService object, which will be used to communicate with the database
     * @param ip
     * @param databaseName
     * @param databaseUsername
     * @param password
     * @param databaseServerUsername
     * @param privateKeyPath
     * @throws SQLException
     * @throws AudioTransferConnectionException
     * @throws ClassNotFoundException
     * @throws DatabaseConnectionException
     */
    public DatabaseService(DatabaseServiceConfigStruct config) throws SQLException, AudioTransferConnectionException, 
                                                                      ClassNotFoundException, DatabaseConnectionException{

        String url = URL_BEGINNING + config.getIP() + ":" + MY_SQL_PORT + "/" + config.getDATABASE_NAME();

        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create connection pool
            System.out.println("Connecting to database...");
            System.out.println(config.getUSERNAME());
            _connectionPool = initConnectionDB(url, config.getUSERNAME(), config.getPASSWORD());
            
            System.out.println("Connection established! Logged in as " + config.getUSERNAME() + 
                            " to database " + config.getDATABASE_NAME() + " on " + config.getIP() + ":" + MY_SQL_PORT + ".");
            
            System.out.println();

            System.out.println("Setting up SFTP channel for audio file transfer to database server...");

            //root directory of remote server where audio will be is /home/<databaseServerUsername>/<audioFilesDirectoryInDatabase>
            _audioTransfer = new AudioTransfer(config.getIP(), 
                                               config.getDATABASE_SERVER_USERNAME(), 
                                               config.getAUDIO_FILES_DIRECTORY_IN_DATABASE(), 
                                               config.getPRIVATE_KEY_PATH());

            System.out.println("SFTP channel: [OK]");
            System.out.println();

        } catch (Exception e){
            throw e;
        }
    }

    private ConnectionPool initConnectionDB(String url, String username, String password) throws DatabaseConnectionException{
        try {

            ConnectionPool connectionPool = new ConnectionPool(url, username, password, CONNECTION_TIMEOUT);
            connectionPool.checkConnection();
            return connectionPool;

        } catch (SQLException e) {
            throw new DatabaseConnectionException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    public void execStatement(String statement) throws DatabaseExecStatementException{
        try {
            Connection connection = _connectionPool.getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(statement);
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    public void execPreparedStatement(String statement, String[] values) throws DatabaseExecStatementException{
        try {
            Connection connection = _connectionPool.getConnection();
            PreparedStatement stmt = connection.prepareStatement(statement);
            for(int i = 0; i < values.length; i++){
                stmt.setString(i + 1, values[i]);
            }
            stmt.executeUpdate();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    public ResultSet execQuery(String query) throws DatabaseExecStatementException{
        try {
            Connection connection = _connectionPool.getConnection();
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    public ResultSet execPreparedQuery(String query, String[] values) throws DatabaseExecStatementException{
        try {
            Connection connection = _connectionPool.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            for(int i = 0; i < values.length; i++){
                stmt.setString(i + 1, values[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new DatabaseExecStatementException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    /**
     * Transfers a file from the local server to the database server
     * @param filePathLocal
     * @param filePathAtDatabase
     * @throws JSchException
     * @throws SftpException
     * @throws IOException
     */
    public void transferFileToDBServer(String filePathLocal, String filePathAtDatabase)  throws AudioTransferErrorException, IOException{

        try {
            _audioTransfer.UploadAudioFile(filePathLocal, filePathAtDatabase);
        } catch (JSchException | SftpException e) {
            throw new AudioTransferErrorException(e.getMessage(), filePathLocal);
        } catch (IOException e) {
            throw e;
        }
    }

    public void Shutdown(){
        _connectionPool.closeAllConnections();
        _audioTransfer.Shutdown();
    }

    public DatabaseMetaData getDatabaseMetadata() throws DatabaseConnectionException{
        try {
            return _connectionPool.getConnection().getMetaData();
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e.getMessage(), e.getCause(), e.getErrorCode(), e.getSQLState());
        }
    }

    public byte[] getAudioFile(String filename) throws AudioTransferErrorException{
        try {
            return _audioTransfer.GetAudioFile(filename);
        } catch (JSchException | SftpException | IOException e) {
            throw new AudioTransferErrorException(e.getMessage(), filename);
        }
    }

    
}
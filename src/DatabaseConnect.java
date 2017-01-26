import java.sql.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class DatabaseConnect {
    private Connection connection;
    private Statement statement;

    void addEmailsToDatabase(Set<String> emails, String db_connect_string,
                             String db_userid, String db_password) {

        try {
            insertEmailsInDatabase(emails, db_connect_string, db_userid, db_password);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                disconnectFromDatabase();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void insertEmailsInDatabase(Set<String> emails, String databaseUrl, String databaseUserId, String databasePassword) throws Exception {
        connectToDatabase(databaseUrl, databaseUserId, databasePassword);

        for (String email : emails) {
            insertEmail(email);
        }
    }

    private void connectToDatabase(String databaseUrl, String databaseUserId, String databasePassword) throws Exception {
        Class.forName("net.sourceforge.jtds.jdbc.Driver"); // loads driver
        connection = DriverManager.getConnection(databaseUrl, databaseUserId, databasePassword);
        System.out.println("connected");
    }

    private void insertEmail(String email) throws SQLException {
        statement = connection.createStatement();
        String insertQuery = String.format("INSERT INTO EmailAddresses VALUES ('%s')", email);
        try {
            statement.executeUpdate(insertQuery);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void disconnectFromDatabase() throws SQLException {
        statement.close();
        connection.close();
    }

}


package modules.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Postgres database is used to efficiently access the Azure Blob Storage file when information on a term needs to
 * be retrieved. The database contains two attributes; a string term and a long byte position. In this way, each term
 * and its associated starting byte position (in the Azure Blob file) can be stored. It's important to note here that
 * two types of databases can be created (one for the default corpus, and one for a user-uploaded corpus).
 */
public class PostgresDB {

    private static Connection conn;
    public String directory;
    public String tableName;

    public PostgresDB(String databaseName) {
        directory = databaseName;

        conn = connect(directory); // Pass directory to the connect method
        if (conn != null) {
            System.out.println("Connected to Postgres database.");
        } else {
            System.out.println("Failed to connect to Postgres database.");
        }
    }

    public void setTableName(String tableName) {
        this.tableName = "\"" + tableName + "\""; // Quote the table name
    }

    /*
    This method obtains the DB connection string from an environment variable (which is set locally and through Azure's
    portal).
    */
    private Connection connect(String databaseName) {
        String envConnectionString = System.getenv("DB_CONNECTION_STRING");
        System.out.println("DB_CONNECTION_STRING: " + envConnectionString);

        Connection conn = null;

        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(envConnectionString);
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to PostgreSQL database: " + e.getMessage());
        }
        return conn;
    }

    // Issues a select statement to obtain the byte position of the term passed
    public Long selectTerm(String term) {
        final String SQL = "SELECT term, byte_position FROM " + tableName + " WHERE term = ?";
        try (PreparedStatement c = conn.prepareStatement(SQL)) {
            c.setString(1, term);
            ResultSet rs = c.executeQuery();
            if (rs.next()) {
                return rs.getLong("byte_position");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Retrieves a list of all unique terms stored in the database (representing the corpus vocabulary)
    public List<String> retrieveVocabulary() {
        List<String> vocabularyList = new ArrayList<>();
        final String SQL = "SELECT term FROM " + tableName;
        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                vocabularyList.add(resultSet.getString("term"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vocabularyList;
    }

    /*
    This method efficiently inserts terms into the database in batches of 2500.
    */
    public void insertTermsBatch(List<String> terms, List<Long> bytePositions) {
        final String SQL = "INSERT INTO " + tableName + " (term, byte_position) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batchSize = 2500; // Adjust this based on performance
            int count = 0;

            for (int i = 0; i < terms.size(); i++) {
                String term = terms.get(i);

                // Truncates terms over 255 to 254 chars
                if (term.length() >= 254) {
                    term = term.substring(0, 254);
                }

                long bytePosition = bytePositions.get(i);
                ps.setString(1, term);
                ps.setLong(2, bytePosition);
                ps.addBatch();

                if (++count % batchSize == 0) {
                    System.out.println("Committing batch of 2500 terms to DB.");
                    ps.executeBatch();
                    conn.commit(); // Commit the transaction
                    ps.clearBatch();
                }
            }

            // Execute the remaining batch
            ps.executeBatch();

            // Commit the final batch
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Commits terms to the database
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Drops the byte_positions table if it exists
    public void dropTable() {
        if (conn == null) {
            System.err.println("Connection is null.");
            return;
        }
        final String SQL = "DROP TABLE IF EXISTS " + tableName;
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(SQL);
            conn.commit();
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("VendorError: " + e.getErrorCode());
            e.printStackTrace();
        }
    }

    // Creates the byte_positions table
    public void createTable() {
        final String SQL = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (term VARCHAR(255) NOT NULL, " +
                " byte_position BIGINT, " +
                " PRIMARY KEY (term))";
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(SQL);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
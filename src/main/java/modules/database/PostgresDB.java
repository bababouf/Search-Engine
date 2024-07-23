package modules.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Postgres database is used to efficiently access the Azure Blob Storage file when information on a term needs to
 * be retrieved. The database contains two attributes; a string term and a long byte position. In this way, each term
 * and its associated starting byte position (in the Azure Blob file) can be stored. It's important to note here that
 * two databases can be created (one for the default corpus, and one for a user-uploaded corpus).
 */
public class PostgresDB {

    private static Connection conn;
    public String directory;

    public PostgresDB(String databaseName) {
        directory = databaseName;

        conn = connect(directory); // Pass directory to the connect method
        if (conn != null)
        {
            System.out.println("Connected to Postgres database.");

        }
        else
        {
            System.out.println("Failed to connect to Postgres database.");
        }
    }

    /**
     * This method connects to the application's Postgres database
     */
    private Connection connect(String databaseName) {
        String envConnectionString = System.getenv("DB_CONNECTION_STRING");

        //String envString = "jdbc:postgresql://bababouf-postgre-server.postgres.database.azure.com:5432/default_directory?user=bababouf&password=d65Gqpa2%3F";
        //String serverName = "bababouf-postgre-server.postgres.database.azure.com:5432";
        //String username = "bababouf";
        //String password = "d65Gqpa2?"; // Replace this with your actual password

        //jdbc:postgresql://bababouf-postgre-server.postgres.database.azure.com:5432/default_directory
        //String URL = "jdbc:postgresql://" + serverName + "/" + databaseName;
        Connection conn = null;

        try
        {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(envConnectionString);
            conn.setAutoCommit(false);
        }
        catch (ClassNotFoundException | SQLException e)
        {
            System.out.println("Failed to connect to PostgreSQL database: " + e.getMessage());
        }
        return conn;
    }

    public Long selectTerm(String term) {

        final String SQL = "SELECT term, byte_position FROM byte_positions WHERE term = ?";
        try (PreparedStatement c = conn.prepareStatement(SQL))
        {
            c.setString(1, term);
            ResultSet rs = c.executeQuery();
            if (rs.next())
            {
                return rs.getLong("byte_position");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> retrieveVocabulary()
    {
        List<String> vocabularyList = new ArrayList<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT term FROM byte_positions");
             ResultSet resultSet = preparedStatement.executeQuery())
        {
            while (resultSet.next())
            {
                vocabularyList.add(resultSet.getString("term"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return vocabularyList;
    }

    public void insertTerm(String term, long bytePosition)
    {
        final String SQL = "INSERT INTO byte_positions VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(SQL))
        {
            ps.setString(1, term);
            ps.setLong(2, bytePosition);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void commit()
    {
        try
        {
            conn.commit();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void dropTable() {
        if (conn == null)
        {
            System.err.println("Connection is null.");
            return;
        }
        final String SQL = "DROP TABLE IF EXISTS byte_positions";
        try (Statement statement = conn.createStatement())
        {
            statement.executeUpdate(SQL);
            conn.commit();
        }
        catch (SQLException e)
        {
            System.err.println("SQLException: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("VendorError: " + e.getErrorCode());
            e.printStackTrace();
        }
    }

    public void createTable()
    {
        final String SQL = "CREATE TABLE IF NOT EXISTS byte_positions " +
                "(term VARCHAR(255) NOT NULL, " +
                " byte_position BIGINT, " +
                " PRIMARY KEY (term))";
        try (Statement statement = conn.createStatement())
        {
            statement.executeUpdate(SQL);
            conn.commit();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
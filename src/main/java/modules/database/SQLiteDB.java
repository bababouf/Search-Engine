package modules.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDB {

    private static Connection conn;
    public SQLiteDB(){
        conn = connect();

    }

    /**
     * Retrieves the long byte position for specific term in the DB
     */
    public Long selectTerm(String term) {
        final String SQL = "SELECT term, byte_position FROM byte_positions WHERE term = ?";
        try (Connection con = this.connect();
             PreparedStatement c = con.prepareStatement(SQL)) {

            c.setString(1, term);
            ResultSet rs = c.executeQuery();

            if (rs.next()) { // Check if there is a result
                Long bytePos = rs.getLong("byte_position");
                return bytePos;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no result is found
    }

    /**
     * This function returns all of the rows for the term column in the byte_positions table (the
     * entire list of vocabulary).
     */
    public List<String> retrieveVocabulary(){
        List<String> vocabularyList = new ArrayList<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT term FROM byte_positions");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String term = resultSet.getString("term");
                vocabularyList.add(term);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's requirements
        }

        return vocabularyList;
    }


    /**
     * Inserts term and long byte position (one row) into the DB
     */
    public void insertTerm(String term, long bytePosition) {
        final String SQL = "INSERT or IGNORE INTO byte_positions VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(SQL);) {
            ps.setString(1, term); // First question mark will be replaced by name variable - String;
            ps.setLong(2, bytePosition); // Second question mark will be replaced by name variable - Integer;
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void dropTable() {
        final String SQL = "DROP TABLE IF EXISTS byte_positions";
        try (Statement statement = conn.createStatement();) {
            statement.executeUpdate(SQL);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {

        final String SQL = "CREATE TABLE byte_positions " +
                "(term text NOT NULL, " +
                " byte_position INTEGER, " +
                " PRIMARY KEY ( term ))";

        try (Statement statement = conn.createStatement();) {
            statement.executeUpdate(SQL);
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Connection connect() {
        // SQLite connection string
        String URL = "jdbc:sqlite:sample.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;

    }
}
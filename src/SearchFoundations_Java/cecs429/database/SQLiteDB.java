package SearchFoundations_Java.cecs429.database;

import java.sql.*;

public class SQLiteDB {

    private static Connection conn;
    public SQLiteDB(){
        conn = connect();
        //this.dropTable();
        //this.createTable();
    }

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

        // This SQL Query is not "dynamic". Columns are static, so no need to use
        // PreparedStatement.
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
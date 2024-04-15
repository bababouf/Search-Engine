package modules.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDB {

    private static Connection conn;

    public MySQLDB() {
        conn = connect();
        System.out.println("Connected to MySQL database.");
    }

    private Connection connect() {
        System.out.println("Connecting to MySQL database...");
        String URL = "jdbc:mysql://localhost:3306/searchengine";
        String username = "root";
        String password = "admin";
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, username, password);
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Failed to connect to MySQL database: " + e.getMessage());
        }
        return conn;
    }

    public Long selectTerm(String term) {
        final String SQL = "SELECT term, byte_position FROM byte_positions WHERE term = ?";
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

    public List<String> retrieveVocabulary() {
        List<String> vocabularyList = new ArrayList<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT term FROM byte_positions");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                vocabularyList.add(resultSet.getString("term"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vocabularyList;
    }

    public void insertTerm(String term, long bytePosition) {
        final String SQL = "INSERT IGNORE INTO byte_positions VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, term);
            ps.setLong(2, bytePosition);
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
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(SQL);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        final String SQL = "CREATE TABLE IF NOT EXISTS byte_positions " +
                "(term VARCHAR(255) NOT NULL, " +
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
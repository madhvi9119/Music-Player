package Music;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// ---------------- Database Connection -----------------
class DatabaseConnection {
    public static Connection getConnection() {
        Connection connection = null;
        try {
            String url = "jdbc:mysql://localhost:3306/music_player_db";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
/* class DatabaseConnection {
   private static final String URL = "jdbc:mysql://localhost:3306/music_player_db";
   private static final String USER = "root"; // Replace with your username
   private static final String PASSWORD = ""; // Replace with your password

   public static Connection getConnection() throws SQLException {
       // The DriverManager.getConnection() method already throws SQLException.
       return DriverManager.getConnection(URL, USER, PASSWORD);
   }
}
*/
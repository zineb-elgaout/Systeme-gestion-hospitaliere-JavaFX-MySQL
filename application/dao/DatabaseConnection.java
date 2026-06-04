package application.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/hopital_db" +
        "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String USER     = "root";
    private static final String PASSWORD = "";       // ← votre mot de passe MySQL

    private static Connection instance;

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion MySQL établie.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable : " + e.getMessage());
            }
        }
        return instance;
    }

    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("🔌 Connexion MySQL fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture connexion : " + e.getMessage());
        }
    }
}
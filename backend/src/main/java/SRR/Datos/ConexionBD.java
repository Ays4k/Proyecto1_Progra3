package SRR.Datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/srr"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";

    private static final String user = "admin";
    private static final String pass = "admin";

    private ConexionBD(){}

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, user, pass);
    }

}

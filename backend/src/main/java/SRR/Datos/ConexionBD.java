package SRR.Datos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {

    private static final String ARCHIVO = "/conexion.properties";
    private static final String URL_POR_DEFECTO = "jdbc:mysql://localhost:3306/srr"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";
    private static final String USUARIO_POR_DEFECTO = "admin";
    private static final String CLAVE_POR_DEFECTO = "admin";

    private static final String URL;
    private static final String USUARIO;
    private static final String CLAVE;

    static {
        Properties archivo = cargarArchivo();
        URL = leer("srr.db.url", "db.url", archivo, URL_POR_DEFECTO);
        USUARIO = leer("srr.db.user", "db.user", archivo, USUARIO_POR_DEFECTO);
        CLAVE = leer("srr.db.pass", "db.pass", archivo, CLAVE_POR_DEFECTO);
    }

    private ConexionBD(){}

    private static Properties cargarArchivo() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConexionBD.class.getResourceAsStream(ARCHIVO)) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer " + ARCHIVO + ", se usan los valores por defecto: " + e.getMessage());
        }
        return propiedades;
    }

    private static String leer(String claveSistema, String claveArchivo, Properties archivo, String porDefecto) {
        String delSistema = System.getProperty(claveSistema);
        if (delSistema != null && !delSistema.isBlank()) {
            return delSistema;
        }
        return archivo.getProperty(claveArchivo, porDefecto).trim();
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

}

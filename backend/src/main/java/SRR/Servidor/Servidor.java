package SRR.Servidor;

import SRR.DTO.Protocolo;
import SRR.Servidor.Enrutadores.Enrutador;
import SRR.Servidor.Enrutadores.EnrutadorPrincipal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static void main(String[] args) {

        ServidorNotificaciones notificador = new ServidorNotificaciones();
        Enrutador enrutador = new EnrutadorPrincipal(notificador);

        Thread servicioNotificador = new Thread(()->{

            try(ServerSocket notificaciones = new ServerSocket(Protocolo.PUERTO_NOTIFICACIONES)){
                System.out.println("Servidor de notificaciones escuchando en el puerto: "+Protocolo.PUERTO_NOTIFICACIONES);
                while (true){
                    Socket cliente = notificaciones.accept();
                    Thread saludo = new Thread(() -> notificador.registrar(cliente));
                    saludo.setDaemon(true);
                    saludo.start();
                }
            }catch (IOException e){
                System.err.println("No se ha podido iniciar el servicio de notificaciones "+e.getMessage());
            }catch (Exception e){
                System.err.println(e.getMessage());
            }
        });
        servicioNotificador.setDaemon(true);
        servicioNotificador.start();

        try (ServerSocket server = new ServerSocket(Protocolo.PUERTO_PETICIONES)) {
            System.out.println("Servidor escuchando en el puerto " + Protocolo.PUERTO_PETICIONES);

            while (true) {
                Socket cliente = server.accept();
                new Thread(new AtencionCliente(cliente, enrutador)).start();
            }
        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }
}

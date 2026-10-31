package SRR.Servidor;

import SRR.DTO.AlertaReservaDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ServidorNotificaciones implements Consumer<AlertaReservaDTO> {

    private static final int TIEMPO_SALUDO_MS = 5000;

    private final List<ClienteAlerta> clientesAlerta = new CopyOnWriteArrayList<>();

    private final ExecutorService envio = Executors.newSingleThreadExecutor(tarea -> {
        Thread hilo = new Thread(tarea, "envio-alertas");
        hilo.setDaemon(true);
        return hilo;
    });

    public void registrar(Socket cliente) {
        try {
            cliente.setSoTimeout(TIEMPO_SALUDO_MS);
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
            Object saludo = entrada.readObject();
            if (!(saludo instanceof String) || ((String) saludo).isBlank()) {
                throw new IOException("Saludo inválido");
            }
            cliente.setSoTimeout(0);
            clientesAlerta.add(new ClienteAlerta((String) saludo, salida));
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Problema al registrar cliente de alertas: " + e.getMessage());
            cerrar(cliente);
        }
    }

    @Override
    public void accept(AlertaReservaDTO alerta) {
        if (alerta == null) {
            return;
        }
        envio.submit(() -> enviarATodos(alerta));
    }

    private void enviarATodos(AlertaReservaDTO alerta) {
        for (ClienteAlerta cliente : clientesAlerta) {
            if (cliente.getIdUsuario().equals(alerta.getIdFuncionario())) {
                continue;
            }
            try {
                cliente.enviar(alerta);
            } catch (Exception e) {
                System.out.println("Error al notificar a un cliente: " + e.getMessage());
                quitar(cliente);
            }
        }
    }

    private void quitar(ClienteAlerta cliente) {
        clientesAlerta.remove(cliente);
        cliente.cerrar();
    }

    private void cerrar(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignorada) {
        }
    }
}

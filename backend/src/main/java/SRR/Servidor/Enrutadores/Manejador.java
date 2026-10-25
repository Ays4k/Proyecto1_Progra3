package SRR.Servidor.Enrutadores;

@FunctionalInterface
public interface Manejador {
    Object ejecutar(Object[] parametros) throws Exception;
}

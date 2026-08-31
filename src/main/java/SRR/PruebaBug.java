package SRR;

import SRR.DTO.UsuarioDTO;
import SRR.Datos.UsuarioDatos;

public class PruebaBug {
    public static void main(String[] args) {
        UsuarioDatos datos = new UsuarioDatos();

        System.out.println("1. Antes de listar:");
        System.out.println("   clave de 111 = " + datos.buscarPorId("111").getContrasena());

        datos.listar();   // solo listamos, sin tocar nada mas

        System.out.println("2. Despues de listar:");
        System.out.println("   clave de 111 = " + datos.buscarPorId("111").getContrasena());

        // simulamos que el admin edita el telefono de alguien
        UsuarioDTO usuario = datos.buscarPorId("111");
        usuario.setTelefono("9999");
        datos.modificar(usuario);

        System.out.println("3. Revisa datos/usuarios.json");
    }
}
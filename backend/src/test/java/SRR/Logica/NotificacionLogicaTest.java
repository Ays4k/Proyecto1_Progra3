package SRR.Logica;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificacionLogicaTest {

    @Test
    void noAceptaUnObservadorNulo() {
        assertThrows(NullPointerException.class, () -> new NotificacionLogica(null));
    }

    @Test
    void reservaLogicaNoAceptaUnObservadorNulo() {
        assertThrows(NullPointerException.class, () -> new ReservaLogica(null));
    }
}

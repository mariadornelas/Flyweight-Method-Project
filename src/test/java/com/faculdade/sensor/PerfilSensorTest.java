package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class PerfilSensorTest {

    private final FabricaPerfis fabrica = new FabricaPerfis();
    private final PerfilSensor termopar = fabrica.obter("Termopar K", "°C", -50, 1000, 80, 90);

    @Test
    void deveClassificarComLimitesInclusivos() {
        assertEquals(Nivel.NORMAL, termopar.classificar(79.9));
        assertEquals(Nivel.ALERTA, termopar.classificar(80.0));
        assertEquals(Nivel.ALERTA, termopar.classificar(89.9));
        assertEquals(Nivel.CRITICO, termopar.classificar(90.0));
        assertEquals(Nivel.CRITICO, termopar.classificar(1000.0));
    }

    @Test
    void deveAceitarOsExtremosDaFaixaFisicaERejeitarOQueEstiverFora() {
        assertTrue(termopar.valido(-50));
        assertTrue(termopar.valido(1000));
        assertFalse(termopar.valido(-50.1));
        assertFalse(termopar.valido(1000.1));
        assertFalse(termopar.valido(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> termopar.classificar(1001));
        assertThrows(IllegalArgumentException.class, () -> termopar.classificar(Double.NaN));
    }

    @Test
    void deveExporSeusDadosIntrinsecos() {
        assertEquals("Termopar K", termopar.getNome());
        assertEquals("°C", termopar.getUnidade());
        assertEquals(-50, termopar.getMinimo());
        assertEquals(1000, termopar.getMaximo());
        assertEquals(80, termopar.getLimiteAlerta());
        assertEquals(90, termopar.getLimiteCritico());
        assertEquals("Termopar K (°C)", termopar.toString());
    }

    @Test
    void validacoesDeConstrucaoPelaFabrica() {
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("  ", "u", 0, 10, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", 10, 10, 1, 2));        // min == max
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", 0, 10, 2, 2));         // alerta == crítico
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", 0, 10, 3, 2));         // invertidos
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", 5, 10, 4, 8));         // alerta < mínimo
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", 0, 10, 2, 11));        // crítico > máximo
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", Double.NaN, 10, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", 0, Double.POSITIVE_INFINITY, 2, 3));
    }

    @Test
    void limitesIguaisAosExtremosDaFaixaDevemSerPermitidos() {
        assertDoesNotThrow(() -> fabrica.obter("X", "u", 0, 10, 0, 10));
    }

    @Test
    void todosOsCamposDevemSerFinais() {
        for (Field f : PerfilSensor.class.getDeclaredFields()) {
            assertTrue(Modifier.isFinal(f.getModifiers()), f.getName() + " é mutável");
        }
        assertTrue(Modifier.isFinal(PerfilSensor.class.getModifiers()));
    }

    @Test
    void perfilNaoDeveGuardarEstadoExtrinseco() {
        for (Field f : PerfilSensor.class.getDeclaredFields()) {
            String n = f.getName().toLowerCase();
            assertFalse(n.contains("leitura") || n.equals("id") || n.contains("local") || n.contains("nivel"),
                    "campo extrínseco no flyweight: " + f.getName());
        }
    }

    @Test
    void classificarNaoDeveGuardarMemoriaEntreChamadas() {
        assertEquals(Nivel.CRITICO, termopar.classificar(95));
        assertEquals(Nivel.NORMAL, termopar.classificar(20));
        assertEquals(Nivel.CRITICO, termopar.classificar(95));
    }

    @Test
    void construtorNaoDeveSerPublico() {
        assertEquals(0, PerfilSensor.class.getConstructors().length);
    }
}

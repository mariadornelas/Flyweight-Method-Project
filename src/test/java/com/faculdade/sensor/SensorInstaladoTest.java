package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** O contexto: estado extrínseco próprio de cada sensor, perfil compartilhado. */
class SensorInstaladoTest {

    private final FabricaPerfis fabrica = new FabricaPerfis();
    private final PerfilSensor termopar = fabrica.obter("Termopar K", "°C", -50, 1000, 80, 90);

    @Test
    void sensorNovoNaoDeveTerLeituraNemNivel() {
        SensorInstalado s = new SensorInstalado("T-01", "caldeira 1", termopar);

        assertTrue(s.getUltimaLeitura().isEmpty());
        assertTrue(s.getNivel().isEmpty());
    }

    @Test
    void registrarDeveGuardarALeituraEODevolverONivel() {
        SensorInstalado s = new SensorInstalado("T-01", "caldeira 1", termopar);

        Nivel nivel = s.registrar(85);

        assertEquals(Nivel.ALERTA, nivel);
        assertEquals(85.0, s.getUltimaLeitura().getAsDouble());
        assertEquals(Nivel.ALERTA, s.getNivel().orElseThrow());
    }

    @Test
    void sensoresDoMesmoPerfilDevemTerEstadosIndependentes() {
        SensorInstalado a = new SensorInstalado("T-01", "caldeira 1", termopar);
        SensorInstalado b = new SensorInstalado("T-02", "caldeira 2", termopar);

        a.registrar(95);
        b.registrar(20);

        assertSame(a.getPerfil(), b.getPerfil());
        assertEquals(Nivel.CRITICO, a.getNivel().orElseThrow());
        assertEquals(Nivel.NORMAL, b.getNivel().orElseThrow());
        assertEquals(95.0, a.getUltimaLeitura().getAsDouble());
        assertEquals(20.0, b.getUltimaLeitura().getAsDouble());
        assertEquals("caldeira 1", a.getLocal());
        assertEquals("caldeira 2", b.getLocal());
    }

    @Test
    void mesmaLeituraEmPerfisDiferentesDeveDarNiveisDiferentes() {
        PerfilSensor pressao = fabrica.obter("Transdutor", "bar", 0, 100, 8, 10);
        PerfilSensor vibracao = fabrica.obter("Acelerômetro", "mm/s", 0, 100, 4.5, 7.1);

        assertEquals(Nivel.NORMAL, new SensorInstalado("T", "x", termopar).registrar(8));
        assertEquals(Nivel.ALERTA, new SensorInstalado("P", "x", pressao).registrar(8));
        assertEquals(Nivel.CRITICO, new SensorInstalado("V", "x", vibracao).registrar(8));
    }

    @Test
    void leituraInvalidaNaoDeveAlterarOEstadoAnterior() {
        SensorInstalado s = new SensorInstalado("T-01", "caldeira 1", termopar);
        s.registrar(85);

        assertThrows(IllegalArgumentException.class, () -> s.registrar(5000));
        assertThrows(IllegalArgumentException.class, () -> s.registrar(Double.NaN));

        assertEquals(85.0, s.getUltimaLeitura().getAsDouble());
        assertEquals(Nivel.ALERTA, s.getNivel().orElseThrow());
    }

    @Test
    void leituraInvalidaEmSensorNovoDeveManterOSensorSemLeitura() {
        SensorInstalado s = new SensorInstalado("T-01", "caldeira 1", termopar);

        assertThrows(IllegalArgumentException.class, () -> s.registrar(5000));

        assertTrue(s.getUltimaLeitura().isEmpty());
        assertTrue(s.getNivel().isEmpty());
    }

    @Test
    void sensorNaoDeveGuardarLimitesNemFaixa() {
        // O estado intrínseco mora só no perfil; o sensor guarda apenas o que é só dele.
        List<String> campos = java.util.Arrays.stream(SensorInstalado.class.getDeclaredFields())
                .map(Field::getName).toList();

        assertEquals(List.of("id", "local", "perfil", "ultimaLeitura", "nivel", "temLeitura"), campos);
    }

    @Test
    void deveValidarArgumentosDoConstrutor() {
        assertThrows(NullPointerException.class, () -> new SensorInstalado(null, "x", termopar));
        assertThrows(IllegalArgumentException.class, () -> new SensorInstalado("  ", "x", termopar));
        assertThrows(NullPointerException.class, () -> new SensorInstalado("T", null, termopar));
        assertThrows(NullPointerException.class, () -> new SensorInstalado("T", "x", null));
    }
}

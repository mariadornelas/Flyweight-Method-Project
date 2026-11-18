package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlyweightIntegracaoTest {

    private static final int POR_TIPO = 3000;

    private final FabricaPerfis fabrica = new FabricaPerfis();
    private final ParqueSensores parque = new ParqueSensores();

    private void montar() {
        for (int i = 0; i < POR_TIPO; i++) {
            PerfilSensor termopar = fabrica.obter("Termopar K", "°C", -50, 1000, 80, 90);
            PerfilSensor transdutor = fabrica.obter("Transdutor de pressão", "bar", 0, 100, 8, 10);
            PerfilSensor acelerometro = fabrica.obter("Acelerômetro", "mm/s", 0, 100, 4.5, 7.1);
            parque.adicionar(String.format("T-%04d", i), "caldeira " + (i % 10), termopar).registrar(60 + (i % 40));
            parque.adicionar(String.format("P-%04d", i), "linha " + (i % 10), transdutor).registrar(i % 12);
            parque.adicionar(String.format("V-%04d", i), "motor " + (i % 10), acelerometro).registrar(i % 10);
        }
    }

    @Test
    void noveMilSensoresDevemCompartilharApenasTresPerfis() {
        montar();

        assertEquals(9000, parque.quantidade());
        assertEquals(3, fabrica.quantidade());
        assertEquals(3, parque.perfisDistintos());
    }

    @Test
    void todosOsSensoresDeUmModeloDevemApontarParaOMesmoObjeto() {
        montar();
        PerfilSensor referencia = parque.buscar("T-0000").orElseThrow().getPerfil();

        for (int i = 0; i < POR_TIPO; i++) {
            assertSame(referencia, parque.buscar(String.format("T-%04d", i)).orElseThrow().getPerfil());
        }
    }

    @Test
    void estadoExtrinsecoDeveContinuarIndividualApesarDoCompartilhamento() {
        montar();

        SensorInstalado primeiro = parque.buscar("T-0000").orElseThrow();
        SensorInstalado ultimo = parque.buscar("T-2999").orElseThrow();

        assertSame(primeiro.getPerfil(), ultimo.getPerfil());
        assertEquals(60.0, primeiro.getUltimaLeitura().getAsDouble());
        assertEquals(99.0, ultimo.getUltimaLeitura().getAsDouble());
        assertEquals(Nivel.NORMAL, primeiro.getNivel().orElseThrow());
        assertEquals(Nivel.CRITICO, ultimo.getNivel().orElseThrow());
        assertEquals("caldeira 0", primeiro.getLocal());
        assertEquals("caldeira 9", ultimo.getLocal());
    }

    @Test
    void contagemPorNivelDeveBaterComOsCalculosEsperados() {
        montar();

        Map<Nivel, Long> c = parque.contarPorNivel();

        assertEquals(1500L + 2000L + 1500L, c.get(Nivel.NORMAL));
        assertEquals(750L + 500L + 900L, c.get(Nivel.ALERTA));
        assertEquals(750L + 500L + 600L, c.get(Nivel.CRITICO));
        assertEquals(9000L, c.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void semFlyweightHaveriaUmPerfilPorSensorEComFlyweightHaUmPorModelo() {
        montar();

        int sensores = parque.quantidade();
        int perfis = fabrica.quantidade();

        assertTrue(perfis * 1000 <= sensores, "esperava ao menos 1000 sensores por perfil");
    }
}

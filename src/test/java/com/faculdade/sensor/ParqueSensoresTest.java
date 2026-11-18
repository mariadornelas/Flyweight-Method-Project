package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParqueSensoresTest {

    private final FabricaPerfis fabrica = new FabricaPerfis();
    private final PerfilSensor termopar = fabrica.obter("Termopar K", "°C", -50, 1000, 80, 90);
    private final PerfilSensor pressao = fabrica.obter("Transdutor", "bar", 0, 100, 8, 10);
    private final ParqueSensores parque = new ParqueSensores();

    @Test
    void deveAdicionarEBuscarPorId() {
        SensorInstalado s = parque.adicionar("T-01", "caldeira 1", termopar);

        assertSame(s, parque.buscar("T-01").orElseThrow());
        assertTrue(parque.buscar("INEXISTENTE").isEmpty());
        assertEquals(1, parque.quantidade());
    }

    @Test
    void deveRecusarIdRepetidoSemSubstituirOOriginal() {
        SensorInstalado primeiro = parque.adicionar("T-01", "caldeira 1", termopar);

        assertThrows(IllegalStateException.class, () -> parque.adicionar("T-01", "outro", pressao));

        assertEquals(1, parque.quantidade());
        assertSame(primeiro, parque.buscar("T-01").orElseThrow());
    }

    @Test
    void contagemPorNivelDeveIncluirTodosOsNiveisInclusiveComZero() {
        parque.adicionar("T-01", "x", termopar).registrar(20);
        parque.adicionar("T-02", "x", termopar).registrar(85);

        Map<Nivel, Long> c = parque.contarPorNivel();

        assertEquals(1L, c.get(Nivel.NORMAL));
        assertEquals(1L, c.get(Nivel.ALERTA));
        assertEquals(0L, c.get(Nivel.CRITICO));
    }

    @Test
    void sensoresSemLeituraNaoDevemEntrarNaContagem() {
        parque.adicionar("T-01", "x", termopar);
        parque.adicionar("T-02", "x", termopar).registrar(95);

        Map<Nivel, Long> c = parque.contarPorNivel();

        assertEquals(1L, c.get(Nivel.CRITICO));
        assertEquals(0L, c.get(Nivel.NORMAL));
        assertEquals(2, parque.quantidade());
    }

    @Test
    void perfisDistintosDeveContarObjetosDiferentesPorIdentidade() {
        parque.adicionar("T-01", "x", termopar);
        parque.adicionar("T-02", "x", termopar);
        parque.adicionar("P-01", "x", pressao);

        assertEquals(2, parque.perfisDistintos());
        assertEquals(3, parque.quantidade());
    }

    @Test
    void perfisIguaisEmValorMasCriadosForaDaFabricaNaoSaoCompartilhados() {
        // Dois objetos com os mesmos valores, mas de pools diferentes, contam como dois.
        PerfilSensor deOutraFabrica = new FabricaPerfis().obter("Termopar K", "°C", -50, 1000, 80, 90);
        parque.adicionar("T-01", "x", termopar);
        parque.adicionar("T-02", "x", deOutraFabrica);

        assertEquals(2, parque.perfisDistintos());
    }

    @Test
    void parqueVazioNaoDeveTerPerfisNemContagens() {
        assertEquals(0, parque.perfisDistintos());
        assertEquals(0, parque.quantidade());
        assertEquals(0L, parque.contarPorNivel().get(Nivel.CRITICO));
    }

    @Test
    void colecaoExpostaDeveSerSomenteLeitura() {
        parque.adicionar("T-01", "x", termopar);

        assertThrows(UnsupportedOperationException.class, () -> parque.todos().clear());
    }

    @Test
    void deveRejeitarIdNulo() {
        assertThrows(NullPointerException.class, () -> parque.adicionar(null, "x", termopar));
    }
}

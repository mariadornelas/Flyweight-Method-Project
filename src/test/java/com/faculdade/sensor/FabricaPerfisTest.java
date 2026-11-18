package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FabricaPerfisTest {

    private final FabricaPerfis fabrica = new FabricaPerfis();

    private PerfilSensor termopar() {
        return fabrica.obter("Termopar K", "°C", -50, 1000, 80, 90);
    }

    @Test
    void mesmosParametrosDevemDevolverOMesmoObjeto() {
        PerfilSensor a = termopar();
        PerfilSensor b = termopar();

        assertSame(a, b);
        assertEquals(1, fabrica.quantidade());
    }

    @Test
    void chavePorValorNaoPorReferenciaDasStrings() {
        PerfilSensor a = fabrica.obter(new String("Termopar K"), new String("°C"), -50, 1000, 80, 90);
        PerfilSensor b = fabrica.obter(new String("Termopar K"), new String("°C"), -50, 1000, 80, 90);

        assertSame(a, b);
    }

    @Test
    void qualquerParametroDiferenteDeveGerarOutroPerfil() {
        PerfilSensor base = termopar();

        assertNotSame(base, fabrica.obter("Termopar J", "°C", -50, 1000, 80, 90));
        assertNotSame(base, fabrica.obter("Termopar K", "K", -50, 1000, 80, 90));
        assertNotSame(base, fabrica.obter("Termopar K", "°C", -40, 1000, 80, 90));
        assertNotSame(base, fabrica.obter("Termopar K", "°C", -50, 900, 80, 90));
        assertNotSame(base, fabrica.obter("Termopar K", "°C", -50, 1000, 70, 90));
        assertNotSame(base, fabrica.obter("Termopar K", "°C", -50, 1000, 80, 95));
        assertEquals(7, fabrica.quantidade());
    }

    @Test
    void mesmoNomeComLimitesDiferentesNaoDeveSerFundido() {
        PerfilSensor rigido = fabrica.obter("Termopar K", "°C", -50, 1000, 60, 70);
        PerfilSensor padrao = termopar();

        assertNotSame(rigido, padrao);
        assertEquals(Nivel.CRITICO, rigido.classificar(75));
        assertEquals(Nivel.NORMAL, padrao.classificar(75));
    }

    @Test
    void muitasChamadasNaoDevemCriarMuitosObjetos() {
        Set<PerfilSensor> vistos = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int i = 0; i < 10_000; i++) {
            vistos.add(termopar());
        }

        assertEquals(1, vistos.size());
        assertEquals(1, fabrica.quantidade());
    }

    @Test
    void chamadasConcorrentesDevemCompartilharUmUnicoObjeto() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<PerfilSensor>> futuros = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                futuros.add(pool.submit(this::termopar));
            }
            Set<PerfilSensor> distintos = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Future<PerfilSensor> f : futuros) {
                distintos.add(f.get(5, TimeUnit.SECONDS));
            }
            assertEquals(1, distintos.size());
            assertEquals(1, fabrica.quantidade());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void largadaSimultaneaComVariasChavesDeveManterUmObjetoPorChave() throws Exception {
        int threads = 16;
        int chaves = 300;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<PerfilSensor[]>> futuros = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                futuros.add(pool.submit(() -> {
                    largada.await();   // todas as threads começam ao mesmo tempo
                    PerfilSensor[] obtidos = new PerfilSensor[chaves];
                    for (int k = 0; k < chaves; k++) {
                        obtidos[k] = fabrica.obter("M" + k, "u", 0, 100, 10, 20);
                    }
                    return obtidos;
                }));
            }
            largada.countDown();

            List<PerfilSensor[]> resultados = new ArrayList<>();
            for (Future<PerfilSensor[]> f : futuros) {
                resultados.add(f.get(10, TimeUnit.SECONDS));
            }
            for (int k = 0; k < chaves; k++) {
                for (int t = 1; t < threads; t++) {
                    assertSame(resultados.get(0)[k], resultados.get(t)[k], "chave " + k + ", thread " + t);
                }
            }
            assertEquals(chaves, fabrica.quantidade());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void parametrosInvalidosNaoDevemEntrarNoPool() {
        assertThrows(IllegalArgumentException.class, () -> fabrica.obter("X", "u", 10, 0, 1, 2));   // min >= max
        assertEquals(0, fabrica.quantidade());

        PerfilSensor valido = fabrica.obter("X", "u", 0, 10, 1, 2);   // a chave correta continua funcionando

        assertEquals(1, fabrica.quantidade());
        assertNotNull(valido);
    }

    @Test
    void fabricasDiferentesDevemTerPoolsIndependentes() {
        FabricaPerfis outra = new FabricaPerfis();

        PerfilSensor daPrimeira = termopar();
        PerfilSensor daOutra = outra.obter("Termopar K", "°C", -50, 1000, 80, 90);

        assertNotSame(daPrimeira, daOutra);
        assertEquals(1, outra.quantidade());
    }

    @Test
    void deveRejeitarNomeEUnidadeNulos() {
        assertThrows(NullPointerException.class, () -> fabrica.obter(null, "u", 0, 10, 1, 2));
        assertThrows(NullPointerException.class, () -> fabrica.obter("X", null, 0, 10, 1, 2));
        assertEquals(0, fabrica.quantidade());
    }
}

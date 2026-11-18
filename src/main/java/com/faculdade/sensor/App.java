package com.faculdade.sensor;

import java.util.Map;

public class App {

    public static void main(String[] args) {
        FabricaPerfis fabrica = new FabricaPerfis();
        ParqueSensores parque = new ParqueSensores();

        int porTipo = 3000;
        for (int i = 0; i < porTipo; i++) {
            // Cada chamada a obter() devolve o MESMO objeto depois da primeira.
            PerfilSensor termopar = fabrica.obter("Termopar K", "°C", -50, 1000, 80, 90);
            PerfilSensor transdutor = fabrica.obter("Transdutor de pressão", "bar", 0, 100, 8, 10);
            PerfilSensor acelerometro = fabrica.obter("Acelerômetro", "mm/s", 0, 100, 4.5, 7.1);

            parque.adicionar(String.format("T-%04d", i), "caldeira " + (i % 10), termopar).registrar(60 + (i % 40));
            parque.adicionar(String.format("P-%04d", i), "linha " + (i % 10), transdutor).registrar(i % 12);
            parque.adicionar(String.format("V-%04d", i), "motor " + (i % 10), acelerometro).registrar(i % 10);
        }

        int total = parque.quantidade();
        System.out.println("Sensores cadastrados:              " + total);
        System.out.println("Perfis criados pela fábrica:       " + fabrica.quantidade());
        System.out.println("Perfis distintos referenciados:    " + parque.perfisDistintos());
        System.out.println("Perfis se não houvesse Flyweight:  " + total + " (um por sensor)");

        SensorInstalado a = parque.buscar("T-0000").orElseThrow();
        SensorInstalado b = parque.buscar("T-2999").orElseThrow();
        System.out.println();
        System.out.println("T-0000 e T-2999 usam o mesmo objeto de perfil? " + (a.getPerfil() == b.getPerfil()));
        System.out.println("Mas cada um guarda o seu estado:");
        imprimir(a);
        imprimir(b);

        System.out.println();
        System.out.println("=== Sensores por nível ===");
        for (Map.Entry<Nivel, Long> e : parque.contarPorNivel().entrySet()) {
            System.out.printf("  %-8s %d%n", e.getKey(), e.getValue());
        }
    }

    private static void imprimir(SensorInstalado s) {
        System.out.printf("  %s  local=%s  leitura=%.1f %s  nível=%s%n", s.getId(), s.getLocal(),
                s.getUltimaLeitura().getAsDouble(), s.getPerfil().getUnidade(), s.getNivel().orElseThrow());
    }
}

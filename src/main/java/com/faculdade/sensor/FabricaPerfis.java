package com.faculdade.sensor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FabricaPerfis {

    private record Chave(String nome, String unidade, double minimo, double maximo,
                         double limiteAlerta, double limiteCritico) {
    }

    private final Map<Chave, PerfilSensor> pool = new ConcurrentHashMap<>();

    public PerfilSensor obter(String nome, String unidade, double minimo, double maximo,
                              double limiteAlerta, double limiteCritico) {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        Objects.requireNonNull(unidade, "unidade não pode ser nula");
        Chave chave = new Chave(nome, unidade, minimo, maximo, limiteAlerta, limiteCritico);

        return pool.computeIfAbsent(chave,
                c -> new PerfilSensor(nome, unidade, minimo, maximo, limiteAlerta, limiteCritico));
    }

    /** Quantos perfis distintos existem no pool. */
    public int quantidade() {
        return pool.size();
    }
}

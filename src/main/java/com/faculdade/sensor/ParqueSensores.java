package com.faculdade.sensor;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ParqueSensores {

    private final Map<String, SensorInstalado> sensores = new LinkedHashMap<>();

    public SensorInstalado adicionar(String id, String local, PerfilSensor perfil) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (sensores.containsKey(id)) {
            throw new IllegalStateException("já existe um sensor com o id " + id);
        }
        SensorInstalado sensor = new SensorInstalado(id, local, perfil);
        sensores.put(id, sensor);
        return sensor;
    }

    public Optional<SensorInstalado> buscar(String id) {
        return Optional.ofNullable(sensores.get(id));
    }

    public int quantidade() {
        return sensores.size();
    }

    public Collection<SensorInstalado> todos() {
        return Collections.unmodifiableCollection(sensores.values());
    }

    public Map<Nivel, Long> contarPorNivel() {
        Map<Nivel, Long> contagem = new EnumMap<>(Nivel.class);
        for (Nivel n : Nivel.values()) {
            contagem.put(n, 0L);
        }
        for (SensorInstalado s : sensores.values()) {
            s.getNivel().ifPresent(n -> contagem.merge(n, 1L, Long::sum));
        }
        return contagem;
    }

    public int perfisDistintos() {
        Set<PerfilSensor> perfis = Collections.newSetFromMap(new IdentityHashMap<>());
        for (SensorInstalado s : sensores.values()) {
            perfis.add(s.getPerfil());
        }
        return perfis.size();
    }
}

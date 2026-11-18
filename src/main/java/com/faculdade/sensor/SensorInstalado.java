package com.faculdade.sensor;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public class SensorInstalado {

    private final String id;
    private final String local;
    private final PerfilSensor perfil;
    private double ultimaLeitura;
    private Nivel nivel;
    private boolean temLeitura;

    public SensorInstalado(String id, String local, PerfilSensor perfil) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        this.id = id;
        this.local = Objects.requireNonNull(local, "local não pode ser nulo");
        this.perfil = Objects.requireNonNull(perfil, "perfil não pode ser nulo");
    }

    /** Registra a leitura, passando-a ao perfil como argumento. Leitura inválida não altera o estado. */
    public Nivel registrar(double leitura) {
        Nivel novo = perfil.classificar(leitura);
        this.ultimaLeitura = leitura;
        this.nivel = novo;
        this.temLeitura = true;
        return novo;
    }

    public String getId() {
        return id;
    }

    public String getLocal() {
        return local;
    }

    public PerfilSensor getPerfil() {
        return perfil;
    }

    public OptionalDouble getUltimaLeitura() {
        return temLeitura ? OptionalDouble.of(ultimaLeitura) : OptionalDouble.empty();
    }

    public Optional<Nivel> getNivel() {
        return Optional.ofNullable(nivel);
    }
}

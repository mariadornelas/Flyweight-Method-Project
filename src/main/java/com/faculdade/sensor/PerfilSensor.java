package com.faculdade.sensor;

import java.util.Locale;
import java.util.Objects;

public final class PerfilSensor {

    private final String nome;
    private final String unidade;
    private final double minimo;
    private final double maximo;
    private final double limiteAlerta;
    private final double limiteCritico;

    PerfilSensor(String nome, String unidade, double minimo, double maximo,
                 double limiteAlerta, double limiteCritico) {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        Objects.requireNonNull(unidade, "unidade não pode ser nula");
        if (nome.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser vazio");
        }
        for (double v : new double[]{minimo, maximo, limiteAlerta, limiteCritico}) {
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                throw new IllegalArgumentException("valores numéricos devem ser finitos");
            }
        }
        if (!(minimo < maximo)) {
            throw new IllegalArgumentException("o mínimo deve ser menor que o máximo");
        }
        if (!(limiteAlerta < limiteCritico)) {
            throw new IllegalArgumentException("o limite de alerta deve ser menor que o crítico");
        }
        if (limiteAlerta < minimo || limiteCritico > maximo) {
            throw new IllegalArgumentException("os limites devem estar dentro da faixa [mínimo, máximo]");
        }
        this.nome = nome;
        this.unidade = unidade;
        this.minimo = minimo;
        this.maximo = maximo;
        this.limiteAlerta = limiteAlerta;
        this.limiteCritico = limiteCritico;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMinimo() {
        return minimo;
    }

    public double getMaximo() {
        return maximo;
    }

    public double getLimiteAlerta() {
        return limiteAlerta;
    }

    public double getLimiteCritico() {
        return limiteCritico;
    }

    public boolean valido(double leitura) {
        return leitura >= minimo && leitura <= maximo;   // NaN dá false
    }

    public Nivel classificar(double leitura) {
        if (!valido(leitura)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "leitura %s fora da faixa física de %s: %.1f a %.1f %s",
                    leitura, nome, minimo, maximo, unidade));
        }
        if (leitura >= limiteCritico) return Nivel.CRITICO;
        if (leitura >= limiteAlerta) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }

    @Override
    public String toString() {
        return nome + " (" + unidade + ")";
    }
}

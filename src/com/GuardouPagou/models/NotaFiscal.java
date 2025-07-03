package com.GuardouPagou.models;

import java.time.LocalDate;
import java.util.List;

public class NotaFiscal {

    private int id; // Garanta que este campo existe
    private String numeroNota;
    private LocalDate dataEmissao;
    private String marca;
    private List<Fatura> faturas;
    private boolean arquivada; // Garanta que este campo existe
    private LocalDate dataArquivamento; // Garanta que este campo existe

    // Construtor padrão
    public NotaFiscal() {
    }

    // Construtor com parâmetros (para criação de novas notas)
    public NotaFiscal(String numeroNota, LocalDate dataEmissao, String marca, List<Fatura> faturas) {
        this.numeroNota = numeroNota;
        this.dataEmissao = dataEmissao;
        this.marca = marca;
        this.faturas = faturas;
        this.arquivada = false; // Padrão para uma nova nota, não arquivada
    }

    // Getters e Setters para 'id'
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getters e Setters para os campos existentes
    public String getNumeroNota() {
        return numeroNota;
    }

    public void setNumeroNota(String numeroNota) {
        this.numeroNota = numeroNota;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public List<Fatura> getFaturas() {
        return faturas;
    }

    public void setFaturas(List<Fatura> faturas) {
        this.faturas = faturas;
    }

    // Getters e Setters para 'arquivada'
    public boolean isArquivada() { // Este método é crucial para os erros
        return arquivada;
    }

    public void setArquivada(boolean arquivada) {
        this.arquivada = arquivada;
    }

    // Getters e Setters para 'dataArquivamento'
    public LocalDate getDataArquivamento() { // Este método é crucial para os erros
        return dataArquivamento;
    }

    public void setDataArquivamento(LocalDate dataArquivamento) {
        this.dataArquivamento = dataArquivamento;
    }
}

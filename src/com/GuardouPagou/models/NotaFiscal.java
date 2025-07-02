package com.GuardouPagou.models;

import java.time.LocalDate;
import java.util.List;

public class NotaFiscal {

    private int id;
    private String numeroNota;
    private LocalDate dataEmissao;
    private String marca;
    private List<Fatura> faturas;
    private boolean arquivada; // ADICIONE ESTA LINHA
    private LocalDate dataArquivamento; // ADICIONE ESTA LINHA SE EXISTIR NO BD E FOR USADO

    // Construtor padrão
    public NotaFiscal() {
    }

    // Construtor com parâmetros (ajuste se for usado)
    public NotaFiscal(String numeroNota, LocalDate dataEmissao, String marca, List<Fatura> faturas) {
        this.numeroNota = numeroNota;
        this.dataEmissao = dataEmissao;
        this.marca = marca;
        this.faturas = faturas;
        this.arquivada = false; // Padrão para nova NF
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    // NOVOS GETTERS E SETTERS PARA O CAMPO ARQUIVADA
    public boolean isArquivada() {
        return arquivada;
    }

    public void setArquivada(boolean arquivada) {
        this.arquivada = arquivada;
    }

    // NOVOS GETTERS E SETTERS PARA O CAMPO DATA_ARQUIVAMENTO
    public LocalDate getDataArquivamento() {
        return dataArquivamento;
    }

    public void setDataArquivamento(LocalDate dataArquivamento) {
        this.dataArquivamento = dataArquivamento;
    }
}

package com.GuardouPagou.dao;

import com.GuardouPagou.models.DatabaseConnection;
import com.GuardouPagou.models.Fatura;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FaturaDAO {

    public void inserirFaturas(List<Fatura> faturas, int notaFiscalId) throws SQLException {
        String sql = "INSERT INTO faturas (nota_fiscal_id, numero_fatura, vencimento, valor, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Fatura fatura : faturas) {
                stmt.setInt(1, notaFiscalId);
                stmt.setInt(2, fatura.getNumeroFatura());
                stmt.setDate(3, Date.valueOf(fatura.getVencimento()));
                stmt.setDouble(4, fatura.getValor());
                stmt.setString(5, "Não Emitida");
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    public boolean marcarFaturaIndividualComoEmitida(int faturaId) throws SQLException {
        String sql = "UPDATE faturas SET status = 'Emitida' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, faturaId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean todasFaturasDaNotaEmitidas(int notaFiscalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM faturas WHERE nota_fiscal_id = ? AND status != 'Emitida'";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaFiscalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    // MÉTODO REINTRODUZIDO (CAUSAVA O ERRO DE COMPILAÇÃO)
    public Fatura obterFaturaMaisProximaDoVencimentoNaoEmitida() throws SQLException {
        Fatura faturaMaisProxima = null;
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca, m.cor AS marca_cor " // Inclui a cor da marca
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
                + "WHERE f.status = 'Não Emitida' AND n.arquivada = FALSE " // Apenas faturas de notas não arquivadas
                + "ORDER BY f.vencimento ASC, f.id ASC "
                + "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                faturaMaisProxima = new Fatura();
                faturaMaisProxima.setId(rs.getInt("id"));
                faturaMaisProxima.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                faturaMaisProxima.setNumeroNota(rs.getString("numero_nota"));
                faturaMaisProxima.setNumeroFatura(rs.getInt("numero_fatura"));
                faturaMaisProxima.setVencimento(rs.getDate("vencimento").toLocalDate());
                faturaMaisProxima.setValor(rs.getDouble("valor"));
                faturaMaisProxima.setStatus(rs.getString("status"));
                faturaMaisProxima.setMarca(rs.getString("marca"));
                faturaMaisProxima.setMarcaColor(rs.getString("marca_cor")); // Popula a cor da marca
            }
        }
        return faturaMaisProxima;
    }

    // MÉTODO REINTRODUZIDO
    public Fatura obterProximaFaturaNaoEmitidaDaMesmaNota(int notaFiscalId) throws SQLException {
        Fatura proximaFatura = null;
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca, m.cor AS marca_cor " // Inclui a cor da marca
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
                + "WHERE f.nota_fiscal_id = ? AND f.status = 'Não Emitida' "
                + "ORDER BY f.vencimento ASC, f.id ASC "
                + "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaFiscalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    proximaFatura = new Fatura();
                    proximaFatura.setId(rs.getInt("id"));
                    proximaFatura.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                    proximaFatura.setNumeroNota(rs.getString("numero_nota"));
                    proximaFatura.setNumeroFatura(rs.getInt("numero_fatura"));
                    proximaFatura.setVencimento(rs.getDate("vencimento").toLocalDate());
                    proximaFatura.setValor(rs.getDouble("valor"));
                    proximaFatura.setStatus(rs.getString("status"));
                    proximaFatura.setMarca(rs.getString("marca"));
                    proximaFatura.setMarcaColor(rs.getString("marca_cor")); // Popula a cor da marca
                }
            }
        }
        return proximaFatura;
    }

    public ObservableList<Fatura> listarFaturasPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, "
                + "f.vencimento, f.valor, f.status, m.nome AS marca, m.cor AS marca_cor "
                + "FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY nota_fiscal_id ORDER BY vencimento, numero_fatura) AS rn "
                + "      FROM faturas WHERE status = 'Não Emitida') f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
                + "WHERE n.arquivada = FALSE AND f.rn = 1 AND f.vencimento BETWEEN ? AND ? "
                + "ORDER BY f.vencimento";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(dataInicial));
            stmt.setDate(2, Date.valueOf(dataFinal));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Fatura fatura = new Fatura();
                fatura.setId(rs.getInt("id"));
                fatura.setNumeroNota(rs.getString("numero_nota"));
                fatura.setNumeroFatura(rs.getInt("numero_fatura"));
                fatura.setVencimento(rs.getDate("vencimento").toLocalDate());
                fatura.setValor(rs.getDouble("valor"));
                fatura.setStatus(rs.getString("status"));
                fatura.setMarca(rs.getString("marca"));
                fatura.setMarcaColor(rs.getString("marca_cor"));
                faturas.add(fatura);
            }
        }
        return faturas;
    }

    public ObservableList<Fatura> listarFaturasPorMarcas(List<String> nomesMarcas) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();
        if (nomesMarcas == null || nomesMarcas.isEmpty()) {
            return faturas;
        }

        String placeholders = String.join(
                ",",
                Collections.nCopies(nomesMarcas.size(), "?")
        );

        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, "
                + "       f.vencimento, f.valor, f.status, "
                + "       m.nome AS marca, m.cor AS marca_cor "
                + "FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY nota_fiscal_id ORDER BY vencimento, numero_fatura) AS rn "
                + "      FROM faturas WHERE status = 'Não Emitida') f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "JOIN marcas m       ON n.marca_id = m.id "
                + "WHERE n.arquivada = FALSE "
                + "  AND f.rn = 1 "
                + "  AND m.nome IN (" + placeholders + ") "
                + "ORDER BY n.numero_nota, f.numero_fatura";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < nomesMarcas.size(); i++) {
                stmt.setString(i + 1, nomesMarcas.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fatura f = new Fatura();
                    f.setId(rs.getInt("id"));
                    f.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                    f.setNumeroNota(rs.getString("numero_nota"));
                    f.setNumeroFatura(rs.getInt("numero_fatura"));
                    f.setVencimento(rs.getDate("vencimento").toLocalDate());
                    f.setValor(rs.getDouble("valor"));
                    f.setStatus(rs.getString("status"));
                    f.setMarca(rs.getString("marca"));
                    f.setMarcaColor(rs.getString("marca_cor"));
                    faturas.add(f);
                }
            }
        }
        return faturas;
    }

    public ObservableList<Fatura> listarFaturasPorPeriodoEMarcas(
            LocalDate dataInicial,
            LocalDate dataFinal,
            List<String> nomesMarcas
    ) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();

        if (dataInicial == null || dataFinal == null
                || nomesMarcas == null || nomesMarcas.isEmpty()) {
            return faturas;
        }

        String placeholders = String.join(",", Collections.nCopies(nomesMarcas.size(), "?"));

        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, "
                + "       f.vencimento, f.valor, f.status, "
                + "       m.nome AS marca, m.cor AS marca_cor "
                + "FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY nota_fiscal_id ORDER BY vencimento, numero_fatura) AS rn "
                + "      FROM faturas WHERE status = 'Não Emitida') f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "JOIN marcas m       ON n.marca_id = m.id "
                + "WHERE n.arquivada = FALSE "
                + "  AND f.rn = 1 "
                + "  AND f.vencimento BETWEEN ? AND ? "
                + "  AND m.nome IN (" + placeholders + ") "
                + "ORDER BY n.numero_nota, f.numero_fatura";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            stmt.setDate(index++, Date.valueOf(dataInicial));
            stmt.setDate(index++, Date.valueOf(dataFinal));

            for (String nome : nomesMarcas) {
                stmt.setString(index++, nome);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fatura f = new Fatura();
                    f.setId(rs.getInt("id"));
                    f.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                    f.setNumeroNota(rs.getString("numero_nota"));
                    f.setNumeroFatura(rs.getInt("numero_fatura"));
                    f.setVencimento(rs.getDate("vencimento").toLocalDate());
                    f.setValor(rs.getDouble("valor"));
                    f.setStatus(rs.getString("status"));
                    f.setMarca(rs.getString("marca"));
                    f.setMarcaColor(rs.getString("marca_cor"));
                    faturas.add(f);
                }
            }
        }
        return faturas;
    }

    public ObservableList<Fatura> listarFaturas(boolean exibirSomenteArquivadas) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder()
                .append("SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, ")
                .append("f.vencimento, f.valor, f.status, ")
                .append("m.nome      AS marca, ")
                .append("m.cor       AS marca_cor, ")
                .append("n.arquivada ")
                .append("FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY nota_fiscal_id ORDER BY vencimento, numero_fatura) AS rn FROM faturas WHERE status = 'Não Emitida') f ")
                .append("JOIN notas_fiscais n ON f.nota_fiscal_id = n.id ")
                .append("LEFT JOIN marcas m ON n.marca_id = m.id ");

        if (exibirSomenteArquivadas) {
            sql.append("WHERE n.arquivada = TRUE ");
        } else {
            sql.append("WHERE n.arquivada = FALSE ");
        }

        sql.append("  AND f.rn = 1 ORDER BY n.numero_nota ASC");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString()); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Fatura f = new Fatura();
                f.setId(rs.getInt("id"));
                f.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                f.setNumeroNota(rs.getString("numero_nota"));
                f.setNumeroFatura(rs.getInt("numero_fatura"));
                f.setVencimento(rs.getDate("vencimento").toLocalDate());
                f.setValor(rs.getDouble("valor"));
                f.setStatus(rs.getString("status"));
                f.setMarca(rs.getString("marca"));
                f.setMarcaColor(rs.getString("marca_cor"));
                faturas.add(f);
            }
        }

        return faturas;
    }

    public List<Fatura> listarFaturasDaNota(int notaFiscalId) throws SQLException {
        List<Fatura> faturas = new ArrayList<>();
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca, m.cor AS marca_cor "
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
                + "WHERE f.nota_fiscal_id = ? ORDER BY f.numero_fatura";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaFiscalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fatura f = new Fatura();
                    f.setId(rs.getInt("id"));
                    f.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                    f.setNumeroNota(rs.getString("numero_nota"));
                    f.setNumeroFatura(rs.getInt("numero_fatura"));
                    f.setVencimento(rs.getDate("vencimento").toLocalDate());
                    f.setValor(rs.getDouble("valor"));
                    f.setStatus(rs.getString("status"));
                    f.setMarca(rs.getString("marca"));
                    f.setMarcaColor(rs.getString("marca_cor"));
                    faturas.add(f);
                }
            }
        }
        return faturas;
    }

    public void inserirFatura(Fatura fatura) throws SQLException {
        String sql = "INSERT INTO faturas (nota_fiscal_id, numero_fatura, vencimento, valor, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fatura.getNotaFiscalId());
            stmt.setInt(2, fatura.getNumeroFatura());
            stmt.setDate(3, Date.valueOf(fatura.getVencimento()));
            stmt.setDouble(4, fatura.getValor());
            stmt.setString(5, fatura.getStatus());
            stmt.executeUpdate();
        }
    }

    public boolean atualizarFatura(Fatura fatura) throws SQLException {
        String sql = "UPDATE faturas SET numero_fatura = ?, vencimento = ?, valor = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fatura.getNumeroFatura());
            stmt.setDate(2, Date.valueOf(fatura.getVencimento()));
            stmt.setDouble(3, fatura.getValor());
            stmt.setString(4, fatura.getStatus());
            stmt.setInt(5, fatura.getId());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean excluirFaturasPorNotaFiscalId(int notaFiscalId) throws SQLException {
        String sql = "DELETE FROM faturas WHERE nota_fiscal_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaFiscalId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}

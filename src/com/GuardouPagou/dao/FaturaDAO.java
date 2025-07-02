package com.GuardouPagou.dao;

import com.GuardouPagou.models.DatabaseConnection;
import com.GuardouPagou.models.Fatura;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList; // Adicionado para ArrayList
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

    // NOVO MÉTODO: Inserir uma única fatura (útil para adicionar novas durante a edição)
    public void inserirFatura(Fatura fatura) throws SQLException {
        String sql = "INSERT INTO faturas (nota_fiscal_id, numero_fatura, vencimento, valor, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fatura.getNotaFiscalId());
            stmt.setInt(2, fatura.getNumeroFatura());
            stmt.setDate(3, Date.valueOf(fatura.getVencimento()));
            stmt.setDouble(4, fatura.getValor());
            stmt.setString(5, fatura.getStatus()); // Mantém o status original da fatura
            stmt.executeUpdate();
        }
    }

    // Adicione este novo método para marcar uma fatura específica como emitida
    public boolean marcarFaturaIndividualComoEmitida(int faturaId) throws SQLException {
        String sql = "UPDATE faturas SET status = 'Emitida' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, faturaId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // NOVO MÉTODO: Atualizar uma fatura existente
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

    // NOVO MÉTODO: Excluir faturas de uma Nota Fiscal específica
    public boolean excluirFaturasPorNotaFiscalId(int notaFiscalId) throws SQLException {
        String sql = "DELETE FROM faturas WHERE nota_fiscal_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaFiscalId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

// Adicione este novo método para verificar se todas as faturas de uma nota fiscal foram emitidas
    public boolean todasFaturasDaNotaEmitidas(int notaFiscalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM faturas WHERE nota_fiscal_id = ? AND status != 'Emitida'";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaFiscalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Retorna true se a contagem de 'Não Emitida' for zero
                }
            }
        }
        return false;
    }

    public ObservableList<Fatura> listarFaturas() throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca "
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
                + "ORDER BY n.numero_nota ASC, f.numero_fatura ASC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Fatura fatura = new Fatura();
                fatura.setId(rs.getInt("id"));
                fatura.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                fatura.setNumeroNota(rs.getString("numero_nota"));
                fatura.setNumeroFatura(rs.getInt("numero_fatura"));
                fatura.setVencimento(rs.getDate("vencimento").toLocalDate());
                fatura.setValor(rs.getDouble("valor"));
                fatura.setStatus(rs.getString("status")); // Usa o status original da fatura
                fatura.setMarca(rs.getString("marca"));
                faturas.add(fatura);
            }
        }
        return faturas;
    }

    // NOVO MÉTODO: Listar faturas por ID da Nota Fiscal
    public ObservableList<Fatura> listarFaturasPorNotaFiscalId(int notaFiscalId) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca "
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
                + "WHERE f.nota_fiscal_id = ? "
                + "ORDER BY f.numero_fatura ASC"; // Ordena por número da fatura

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notaFiscalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fatura fatura = new Fatura();
                    fatura.setId(rs.getInt("id"));
                    fatura.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                    fatura.setNumeroNota(rs.getString("numero_nota"));
                    fatura.setNumeroFatura(rs.getInt("numero_fatura"));
                    fatura.setVencimento(rs.getDate("vencimento").toLocalDate());
                    fatura.setValor(rs.getDouble("valor"));
                    fatura.setStatus(rs.getString("status"));
                    fatura.setMarca(rs.getString("marca"));
                    faturas.add(fatura);
                }
            }
        }
        return faturas;
    }

    public ObservableList<Fatura> listarFaturasPorPeriodo(LocalDate dataInicial) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status "
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "WHERE f.vencimento >= ? "
                + "ORDER BY f.vencimento";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(dataInicial));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Fatura fatura = new Fatura();
                fatura.setId(rs.getInt("id"));
                fatura.setNumeroNota(rs.getString("numero_nota"));
                fatura.setNumeroFatura(rs.getInt("numero_fatura"));
                fatura.setVencimento(rs.getDate("vencimento").toLocalDate());
                fatura.setValor(rs.getDouble("valor"));
                fatura.setStatus(rs.getString("status"));
                faturas.add(fatura);
            }
        }

        return faturas;
    }

    public ObservableList<Fatura> listarFaturasPorMarca(String nomeMarca) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status "
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "JOIN marcas m ON n.marca_id = m.id "
                + "WHERE m.nome ILIKE ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Permitir busca parcial ignorando maiúsculas/minúsculas
            stmt.setString(1, "%" + nomeMarca + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Fatura fatura = new Fatura();
                fatura.setId(rs.getInt("id"));
                fatura.setNumeroNota(rs.getString("numero_nota"));
                fatura.setNumeroFatura(rs.getInt("numero_fatura"));
                fatura.setVencimento(rs.getDate("vencimento").toLocalDate());
                fatura.setValor(rs.getDouble("valor"));
                fatura.setStatus(rs.getString("status"));
                faturas.add(fatura);
            }
        }
        return faturas;
    }

    public ObservableList<Fatura> listarFaturas(boolean exibirSomenteArquivadas) throws SQLException {
        ObservableList<Fatura> faturas = FXCollections.observableArrayList();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca, n.arquivada "
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
        );

        if (exibirSomenteArquivadas) {
            sqlBuilder.append("WHERE n.arquivada = TRUE ");
        } else {
            sqlBuilder.append("WHERE n.arquivada = FALSE ");
        }

        sqlBuilder.append("ORDER BY n.numero_nota ASC, f.numero_fatura ASC");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString()); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Fatura fatura = new Fatura();
                fatura.setId(rs.getInt("id"));
                fatura.setNotaFiscalId(rs.getInt("nota_fiscal_id"));
                fatura.setNumeroNota(rs.getString("numero_nota"));
                fatura.setNumeroFatura(rs.getInt("numero_fatura"));
                fatura.setVencimento(rs.getDate("vencimento").toLocalDate());
                fatura.setValor(rs.getDouble("valor"));
                fatura.setStatus(rs.getString("status")); // Usa o status original da fatura
                fatura.setMarca(rs.getString("marca"));
                faturas.add(fatura);
            }
        }
        return faturas;
    }

    public Fatura obterFaturaMaisProximaDoVencimentoNaoEmitida() throws SQLException {
        Fatura faturaMaisProxima = null;
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca "
                + "FROM faturas f "
                + "JOIN notas_fiscais n ON f.nota_fiscal_id = n.id "
                + "LEFT JOIN marcas m ON n.marca_id = m.id "
                + "WHERE f.status = 'Não Emitida' "
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
            }
        }
        return faturaMaisProxima;
    }

    public Fatura obterProximaFaturaNaoEmitidaDaMesmaNota(int notaFiscalId) throws SQLException {
        Fatura proximaFatura = null;
        String sql = "SELECT f.id, f.nota_fiscal_id, n.numero_nota, f.numero_fatura, f.vencimento, f.valor, f.status, "
                + "m.nome AS marca "
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
                }
            }
        }
        return proximaFatura;
    }
}

package com.GuardouPagou.dao;

import com.GuardouPagou.models.DatabaseConnection;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.dao.MarcaDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class NotaFiscalDAO {

    public boolean existeNotaFiscal(String numeroNota) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notas_fiscais WHERE numero_nota = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroNota);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existeNotaFiscalComOutroId(String numeroNota, int idAtual) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notas_fiscais WHERE numero_nota = ? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroNota);
            stmt.setInt(2, idAtual);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int inserirNotaFiscal(NotaFiscal nota) throws SQLException {
        String sql = "INSERT INTO notas_fiscais (numero_nota, data_emissao, marca_id) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, nota.getNumeroNota());
            stmt.setDate(2, Date.valueOf(nota.getDataEmissao()));
            Integer marcaId = new MarcaDAO().obterIdPorNome(nota.getMarca());
            if (marcaId != null) {
                stmt.setInt(3, marcaId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Retorna o ID gerado
                    }
                }
            }
            return -1;
        }
    }

    // MÉTODO: Obter Nota Fiscal por ID (Completo, substitui o antigo buscarNotaFiscalPorId)
    public NotaFiscal obterNotaFiscalPorId(int id) throws SQLException {
        NotaFiscal nota = null;
        String sql = "SELECT nf.id, nf.numero_nota, nf.data_emissao, m.nome AS marca, nf.arquivada, nf.data_arquivamento " +
                     "FROM notas_fiscais nf LEFT JOIN marcas m ON nf.marca_id = m.id WHERE nf.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nota = new NotaFiscal();
                    nota.setId(rs.getInt("id"));
                    nota.setNumeroNota(rs.getString("numero_nota"));
                    nota.setDataEmissao(rs.getDate("data_emissao").toLocalDate());
                    nota.setMarca(rs.getString("marca"));
                    nota.setArquivada(rs.getBoolean("arquivada"));
                    Date dataArqSql = rs.getDate("data_arquivamento");
                    nota.setDataArquivamento(dataArqSql != null ? dataArqSql.toLocalDate() : null);
                }
            }
        }
        return nota;
    }

    public boolean atualizarNotaFiscal(NotaFiscal nota) throws SQLException {
        String sql = "UPDATE notas_fiscais SET numero_nota = ?, data_emissao = ?, marca_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nota.getNumeroNota());
            stmt.setDate(2, Date.valueOf(nota.getDataEmissao()));
            Integer marcaId = new MarcaDAO().obterIdPorNome(nota.getMarca());
            if (marcaId != null) {
                stmt.setInt(3, marcaId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setInt(4, nota.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean marcarComoArquivada(int notaFiscalId, LocalDate dataArquivamento) throws SQLException {
        String sql = "UPDATE notas_fiscais SET arquivada = TRUE, data_arquivamento = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataArquivamento));
            stmt.setInt(2, notaFiscalId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<NotaFiscalArquivadaDAO> listarNotasFiscaisArquivadasComContagem(Map<String, Object> filtros) throws SQLException {
        List<NotaFiscalArquivadaDAO> notasArquivadas = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT nf.id, nf.numero_nota, m.nome AS marca, m.cor AS marca_cor, " +
                        "       nf.data_arquivamento, " +
                        "       (SELECT COUNT(*) FROM faturas f WHERE f.nota_fiscal_id = nf.id) AS quantidade_faturas " +
                        "  FROM notas_fiscais nf " +
                        "  LEFT JOIN marcas m ON nf.marca_id = m.id " +
                        " WHERE nf.arquivada = TRUE "
        );

        if (filtros != null && !filtros.isEmpty()) {
            for (Map.Entry<String, Object> filtro : filtros.entrySet()) {
                String coluna = filtro.getKey();
                Object valor = filtro.getValue();
                if (valor == null) continue;

                sqlBuilder.append("AND ");
                switch (coluna) {
                    case "numero_nota":
                        sqlBuilder.append("LOWER(nf.numero_nota) LIKE LOWER(?) ");
                        parametros.add("%" + valor + "%");
                        break;
                    case "marca":
                        sqlBuilder.append("LOWER(m.nome) LIKE LOWER(?) ");
                        parametros.add("%" + valor + "%");
                        break;
                    case "data_arquivamento":
                        sqlBuilder.append("nf.").append(coluna).append(" = ? ");
                        parametros.add(Date.valueOf((LocalDate) valor));
                        break;
                }
            }
        }

        sqlBuilder.append("ORDER BY nf.data_arquivamento DESC, nf.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaFiscalArquivadaDAO nota = new NotaFiscalArquivadaDAO(
                            rs.getString("numero_nota"),
                            rs.getInt   ("quantidade_faturas"),
                            rs.getString("marca"),
                            rs.getDate  ("data_arquivamento").toLocalDate()
                    );
                    nota.setMarcaColor(rs.getString("marca_cor"));
                    notasArquivadas.add(nota);
                }
            }
        }

        return notasArquivadas;
    }
}
package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import model.Arquivo;
import model.Evidencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

@Repository
public class EvidenciaDAO {
    @Autowired
    private ConnectionFactory connectionFactory;

    public void inserir(Evidencia evidencia) throws SQLException {
        String sql = "INSERT INTO T_ECMV_EVIDENCIA (TP_EVIDENCIA, REFERENCIA_ARQUIVO_EVIDENCIA, DT_CAPTURA_EVIDENCIA, "
                + "ID_SUBMISSAO, ARQUIVO_EVIDENCIA, TP_ARQUIVO_EVIDENCIA, DURACAO_SEG_EVIDENCIA) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, evidencia.getTipo());
            stmt.setString(2, evidencia.getReferenciaArquivo());
            stmt.setTimestamp(3, Timestamp.valueOf(evidencia.getDataCaptura()));
            stmt.setLong(4, evidencia.getSubmissaoId());
            stmt.setBytes(5, evidencia.getArquivo());
            stmt.setString(6, evidencia.getTipoArquivo());
            if (evidencia.getDuracaoSegundos() != null) {
                stmt.setInt(7, evidencia.getDuracaoSegundos());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    public Arquivo buscarArquivoDaSubmissao(Long submissaoId) throws SQLException {
        String sql = "SELECT ARQUIVO_EVIDENCIA, TP_ARQUIVO_EVIDENCIA FROM T_ECMV_EVIDENCIA "
                + "WHERE ID_SUBMISSAO = ? AND ARQUIVO_EVIDENCIA IS NOT NULL";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, submissaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Arquivo(rs.getBytes("ARQUIVO_EVIDENCIA"), rs.getString("TP_ARQUIVO_EVIDENCIA"));
                }
            }
        }
        return null;
    }
}

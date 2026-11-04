package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import model.Submissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SubmissaoDAO {
    private static final String SELECT = "SELECT s.ID_SUBMISSAO, s.ID_USUARIO, s.ID_MISSAO, s.STATUS_SUBMISSAO, "
            + "s.DT_ENVIO_SUBMISSAO, m.NM_MISSAO, m.PONTOS_MISSAO, m.TP_EVIDENCIA_MISSAO, "
            + "e.TP_EVIDENCIA, e.DURACAO_SEG_EVIDENCIA, "
            + "CASE WHEN e.ARQUIVO_EVIDENCIA IS NULL THEN 0 ELSE 1 END AS TEM_ARQUIVO, "
            + "(SELECT MAX(v.DT_VALIDACAO_ECOGUARD) FROM T_ECMV_VALIDACAO_ECOGUARD v "
            + "  WHERE v.ID_SUBMISSAO = s.ID_SUBMISSAO) AS DT_CONCLUSAO "
            + "FROM T_ECMV_SUBMISSAO s "
            + "JOIN T_ECMV_MISSAO m ON m.ID_MISSAO = s.ID_MISSAO "
            + "LEFT JOIN T_ECMV_EVIDENCIA e ON e.ID_SUBMISSAO = s.ID_SUBMISSAO ";

    @Autowired
    private ConnectionFactory connectionFactory;

    public List<Submissao> listarPorUsuario(Long usuarioId) throws SQLException {
        return listar(SELECT + "WHERE s.ID_USUARIO = ? ORDER BY s.DT_ENVIO_SUBMISSAO", usuarioId);
    }

    public List<Submissao> listarPorStatus(String status) throws SQLException {
        return listar(SELECT + "WHERE s.STATUS_SUBMISSAO = ? ORDER BY s.DT_ENVIO_SUBMISSAO DESC", status);
    }

    public Submissao buscarPorId(Long id) throws SQLException {
        List<Submissao> lista = listar(SELECT + "WHERE s.ID_SUBMISSAO = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public Long inserir(Submissao submissao) throws SQLException {
        String sql = "INSERT INTO T_ECMV_SUBMISSAO (STATUS_SUBMISSAO, DT_ENVIO_SUBMISSAO, ID_USUARIO, ID_MISSAO) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_SUBMISSAO"})) {
            stmt.setString(1, submissao.getStatus());
            stmt.setTimestamp(2, Timestamp.valueOf(submissao.getEnviadaEm()));
            stmt.setLong(3, submissao.getUsuarioId());
            stmt.setLong(4, submissao.getMissaoId());
            stmt.executeUpdate();
            try (ResultSet chave = stmt.getGeneratedKeys()) {
                chave.next();
                return chave.getLong(1);
            }
        }
    }

    public void atualizarStatus(Long id, String status) throws SQLException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE T_ECMV_SUBMISSAO SET STATUS_SUBMISSAO = ? WHERE ID_SUBMISSAO = ?")) {
            stmt.setString(1, status);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    public void excluir(Long id) throws SQLException {
        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                executar(conn, "DELETE FROM T_ECMV_VERIFICACAO_SUBMISSAO WHERE ID_VALIDACAO_ECOGUARD IN "
                        + "(SELECT ID_VALIDACAO_ECOGUARD FROM T_ECMV_VALIDACAO_ECOGUARD WHERE ID_SUBMISSAO = ?)", id);
                executar(conn, "DELETE FROM T_ECMV_VALIDACAO_ECOGUARD WHERE ID_SUBMISSAO = ?", id);
                executar(conn, "DELETE FROM T_ECMV_ANALISE_IA WHERE ID_EVIDENCIA IN "
                        + "(SELECT ID_EVIDENCIA FROM T_ECMV_EVIDENCIA WHERE ID_SUBMISSAO = ?)", id);
                executar(conn, "DELETE FROM T_ECMV_EVIDENCIA WHERE ID_SUBMISSAO = ?", id);
                executar(conn, "DELETE FROM T_ECMV_MOVIMENTO_ECOPOINTS WHERE ID_SUBMISSAO = ?", id);
                executar(conn, "DELETE FROM T_ECMV_ECOSCORE WHERE ID_SUBMISSAO = ?", id);
                executar(conn, "DELETE FROM T_ECMV_SUBMISSAO WHERE ID_SUBMISSAO = ?", id);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private List<Submissao> listar(String sql, Object parametro) throws SQLException {
        List<Submissao> lista = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        }
        return lista;
    }

    private Submissao montar(ResultSet rs) throws SQLException {
        Submissao s = new Submissao();
        s.setId(rs.getLong("ID_SUBMISSAO"));
        s.setUsuarioId(rs.getLong("ID_USUARIO"));
        s.setMissaoId(rs.getLong("ID_MISSAO"));
        s.setStatus(rs.getString("STATUS_SUBMISSAO"));
        s.setEnviadaEm(rs.getTimestamp("DT_ENVIO_SUBMISSAO").toLocalDateTime());
        s.setTitulo(rs.getString("NM_MISSAO"));
        s.setPontos(rs.getInt("PONTOS_MISSAO"));

        String tipoEvidencia = rs.getString("TP_EVIDENCIA");
        s.setTipoEvidencia(tipoEvidencia != null ? tipoEvidencia : rs.getString("TP_EVIDENCIA_MISSAO"));

        int duracao = rs.getInt("DURACAO_SEG_EVIDENCIA");
        s.setDuracaoSegundos(rs.wasNull() ? null : duracao);

        Timestamp conclusao = rs.getTimestamp("DT_CONCLUSAO");
        s.setConcluidaEm(conclusao == null ? null : conclusao.toLocalDateTime());

        s.setEvidenciaUrl(rs.getInt("TEM_ARQUIVO") == 1 ? "sim" : null);
        return s;
    }

    private void executar(Connection conn, String sql, Long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}

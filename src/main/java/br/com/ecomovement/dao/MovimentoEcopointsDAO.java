package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import model.MovimentoEcopoints;
import model.PosicaoRanking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MovimentoEcopointsDAO {
    @Autowired
    private ConnectionFactory connectionFactory;

    public void inserir(MovimentoEcopoints movimento) throws SQLException {
        String sql = "INSERT INTO T_ECMV_MOVIMENTO_ECOPOINTS (ID_SUBMISSAO, TP_MOVIMENTO_ECOPOINTS, "
                + "QUANTIDADE_MOVIMENTO_ECOPOINTS) VALUES (?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, movimento.getSubmissaoId());
            stmt.setString(2, movimento.getTipo());
            stmt.setInt(3, movimento.getQuantidade());
            stmt.executeUpdate();
        }
    }

    public int somarPontosDoUsuario(Long usuarioId) throws SQLException {
        String sql = "SELECT NVL(SUM(CASE WHEN m.TP_MOVIMENTO_ECOPOINTS = 'DEBITO' "
                + "THEN -m.QUANTIDADE_MOVIMENTO_ECOPOINTS ELSE m.QUANTIDADE_MOVIMENTO_ECOPOINTS END), 0) "
                + "FROM T_ECMV_MOVIMENTO_ECOPOINTS m "
                + "JOIN T_ECMV_SUBMISSAO s ON s.ID_SUBMISSAO = m.ID_SUBMISSAO "
                + "WHERE s.ID_USUARIO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<PosicaoRanking> listarPontosPorUsuario() throws SQLException {
        String sql = "SELECT u.ID_USUARIO, u.NM_USUARIO, NVL(SUM(CASE WHEN m.TP_MOVIMENTO_ECOPOINTS = 'DEBITO' "
                + "THEN -m.QUANTIDADE_MOVIMENTO_ECOPOINTS ELSE m.QUANTIDADE_MOVIMENTO_ECOPOINTS END), 0) AS PONTOS "
                + "FROM T_ECMV_USUARIO u "
                + "JOIN T_ECMV_ROLE r ON r.ID_ROLE = u.ID_ROLE "
                + "LEFT JOIN T_ECMV_SUBMISSAO s ON s.ID_USUARIO = u.ID_USUARIO "
                + "LEFT JOIN T_ECMV_MOVIMENTO_ECOPOINTS m ON m.ID_SUBMISSAO = s.ID_SUBMISSAO "
                + "WHERE r.NM_ROLE <> 'ADMIN' "
                + "GROUP BY u.ID_USUARIO, u.NM_USUARIO";
        List<PosicaoRanking> lista = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new PosicaoRanking(rs.getLong("ID_USUARIO"), rs.getString("NM_USUARIO"), rs.getInt("PONTOS")));
            }
        }
        return lista;
    }
}

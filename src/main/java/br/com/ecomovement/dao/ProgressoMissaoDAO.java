package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import br.com.ecomovement.model.ProgressoMissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProgressoMissaoDAO {
    @Autowired
    private ConnectionFactory connectionFactory;

    public List<ProgressoMissao> listarPorUsuario(Long usuarioId) throws SQLException {
        String sql = "SELECT ID_USUARIO, ID_MISSAO, QT_PROGRESSO_MISSAO FROM T_ECMV_PROGRESSO_MISSAO WHERE ID_USUARIO = ?";
        List<ProgressoMissao> lista = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ProgressoMissao(rs.getLong("ID_USUARIO"), rs.getLong("ID_MISSAO"),
                            rs.getInt("QT_PROGRESSO_MISSAO")));
                }
            }
        }
        return lista;
    }
}

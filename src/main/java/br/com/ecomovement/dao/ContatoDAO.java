package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import model.Contato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContatoDAO {
    @Autowired
    private ConnectionFactory connectionFactory;

    public void inserir(Contato contato) throws SQLException {
        String sql = "INSERT INTO T_ECMV_CONTATO (NM_CONTATO, EMAIL_CONTATO, MENSAGEM_CONTATO) VALUES (?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, contato.getNome());
            stmt.setString(2, contato.getEmail());
            stmt.setString(3, contato.getMensagem());
            stmt.executeUpdate();
        }
    }

    public List<Contato> listar() throws SQLException {
        String sql = "SELECT ID_CONTATO, NM_CONTATO, EMAIL_CONTATO, MENSAGEM_CONTATO FROM T_ECMV_CONTATO "
                + "ORDER BY DT_ENVIO_CONTATO DESC";
        List<Contato> lista = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Contato contato = new Contato(rs.getString("NM_CONTATO"), rs.getString("EMAIL_CONTATO"),
                        rs.getString("MENSAGEM_CONTATO"));
                contato.setId(rs.getLong("ID_CONTATO"));
                lista.add(contato);
            }
        }
        return lista;
    }

    public Contato buscarPorId(Long id) throws SQLException {
        String sql = "SELECT ID_CONTATO, NM_CONTATO, EMAIL_CONTATO, MENSAGEM_CONTATO FROM T_ECMV_CONTATO "
                + "WHERE ID_CONTATO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Contato contato = new Contato(rs.getString("NM_CONTATO"), rs.getString("EMAIL_CONTATO"),
                        rs.getString("MENSAGEM_CONTATO"));
                contato.setId(rs.getLong("ID_CONTATO"));
                return contato;
            }
        }
    }

    public int excluir(Long id) throws SQLException {
        String sql = "DELETE FROM T_ECMV_CONTATO WHERE ID_CONTATO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate();
        }
    }
}

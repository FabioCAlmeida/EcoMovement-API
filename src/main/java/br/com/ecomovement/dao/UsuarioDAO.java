package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import br.com.ecomovement.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UsuarioDAO {
    private static final String SELECT = "SELECT u.ID_USUARIO, u.NM_USUARIO, u.EMAIL_USUARIO, u.SENHA_HASH_USUARIO, "
            + "u.DT_CADASTRO_USUARIO, u.ID_ROLE, r.NM_ROLE "
            + "FROM T_ECMV_USUARIO u JOIN T_ECMV_ROLE r ON r.ID_ROLE = u.ID_ROLE ";

    @Autowired
    private ConnectionFactory connectionFactory;

    public Usuario buscarPorId(Long id) throws SQLException {
        return buscar(SELECT + "WHERE u.ID_USUARIO = ?", id);
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        return buscar(SELECT + "WHERE LOWER(u.EMAIL_USUARIO) = LOWER(?)", email);
    }

    private Usuario buscar(String sql, Object parametro) throws SQLException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Usuario usuario = new Usuario();
                usuario.setId(rs.getLong("ID_USUARIO"));
                usuario.setNome(rs.getString("NM_USUARIO"));
                usuario.setEmail(rs.getString("EMAIL_USUARIO"));
                usuario.setSenhaHash(rs.getString("SENHA_HASH_USUARIO"));
                usuario.setDataCadastro(rs.getTimestamp("DT_CADASTRO_USUARIO").toLocalDateTime());
                usuario.setRoleId(rs.getLong("ID_ROLE"));
                usuario.setRole(rs.getString("NM_ROLE"));
                return usuario;
            }
        }
    }
}

package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import br.com.ecomovement.model.ValidacaoEcoguard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
public class ValidacaoEcoguardDAO {
    @Autowired
    private ConnectionFactory connectionFactory;

    public void inserir(ValidacaoEcoguard validacao) throws SQLException {
        String sql = "INSERT INTO T_ECMV_VALIDACAO_ECOGUARD (RESULTADO_VALIDACAO_ECOGUARD, "
                + "SCORE_RISCO_VALIDACAO_ECOGUARD, ID_SUBMISSAO, DT_VALIDACAO_ECOGUARD) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, validacao.getResultado());
            stmt.setInt(2, validacao.getScoreRisco());
            stmt.setLong(3, validacao.getSubmissaoId());
            stmt.setTimestamp(4, Timestamp.valueOf(validacao.getData()));
            stmt.executeUpdate();
        }
    }
}

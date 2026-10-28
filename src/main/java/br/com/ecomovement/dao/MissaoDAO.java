package br.com.ecomovement.dao;

import br.com.ecomovement.factory.ConnectionFactory;
import br.com.ecomovement.model.Arquivo;
import br.com.ecomovement.model.Missao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MissaoDAO {
    private static final String SELECT = "SELECT ID_MISSAO, NM_MISSAO, CATEGORIA_MISSAO, DESCRICAO_MISSAO, "
            + "DIFICULDADE_MISSAO, ATIVA_MISSAO, INSTRUCOES_MISSAO, PONTOS_MISSAO, TP_EVIDENCIA_MISSAO, "
            + "TEMPO_MAXIMO_SEG_MISSAO, META_MISSAO, "
            + "CASE WHEN IMAGEM_MISSAO IS NULL THEN 0 ELSE 1 END AS TEM_IMAGEM "
            + "FROM T_ECMV_MISSAO ";

    @Autowired
    private ConnectionFactory connectionFactory;

    public List<Missao> listar(boolean somenteAtivas) throws SQLException {
        String sql = SELECT + (somenteAtivas ? "WHERE ATIVA_MISSAO = 'S' " : "") + "ORDER BY ID_MISSAO";
        List<Missao> missoes = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                missoes.add(montar(rs));
            }
        }
        return missoes;
    }

    public Missao buscarPorId(Long id) throws SQLException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE ID_MISSAO = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    public Long inserir(Missao missao) throws SQLException {
        String sql = "INSERT INTO T_ECMV_MISSAO (NM_MISSAO, CATEGORIA_MISSAO, DESCRICAO_MISSAO, DIFICULDADE_MISSAO, "
                + "ATIVA_MISSAO, INSTRUCOES_MISSAO, PONTOS_MISSAO, TP_EVIDENCIA_MISSAO, TEMPO_MAXIMO_SEG_MISSAO, "
                + "META_MISSAO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_MISSAO"})) {
            preencher(stmt, missao);
            stmt.executeUpdate();

            try (ResultSet chave = stmt.getGeneratedKeys()) {
                chave.next();
                return chave.getLong(1);
            }
        }
    }

    public void atualizar(Missao missao) throws SQLException {
        String sql = "UPDATE T_ECMV_MISSAO SET NM_MISSAO = ?, CATEGORIA_MISSAO = ?, DESCRICAO_MISSAO = ?, "
                + "DIFICULDADE_MISSAO = ?, ATIVA_MISSAO = ?, INSTRUCOES_MISSAO = ?, PONTOS_MISSAO = ?, "
                + "TP_EVIDENCIA_MISSAO = ?, TEMPO_MAXIMO_SEG_MISSAO = ?, META_MISSAO = ? WHERE ID_MISSAO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencher(stmt, missao);
            stmt.setLong(11, missao.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(Long id) throws SQLException {
        try (Connection conn = connectionFactory.getConnection()) {
            executar(conn, "DELETE FROM T_ECMV_QRCODE WHERE ID_MISSAO = ?", id);
            executar(conn, "DELETE FROM T_ECMV_PROGRESSO_MISSAO WHERE ID_MISSAO = ?", id);
            executar(conn, "DELETE FROM T_ECMV_MISSAO WHERE ID_MISSAO = ?", id);
        }
    }

    public boolean possuiSubmissoes(Long id) throws SQLException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM T_ECMV_SUBMISSAO WHERE ID_MISSAO = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Arquivo buscarImagem(Long id) throws SQLException {
        String sql = "SELECT IMAGEM_MISSAO, TP_IMAGEM_MISSAO FROM T_ECMV_MISSAO WHERE ID_MISSAO = ? AND IMAGEM_MISSAO IS NOT NULL";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Arquivo(rs.getBytes("IMAGEM_MISSAO"), rs.getString("TP_IMAGEM_MISSAO"));
                }
            }
        }
        return null;
    }

    public void salvarImagem(Long id, Arquivo imagem) throws SQLException {
        String sql = "UPDATE T_ECMV_MISSAO SET IMAGEM_MISSAO = ?, TP_IMAGEM_MISSAO = ? WHERE ID_MISSAO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, imagem.getDados());
            stmt.setString(2, imagem.getTipo());
            stmt.setLong(3, id);
            stmt.executeUpdate();
        }
    }

    private void preencher(PreparedStatement stmt, Missao missao) throws SQLException {
        stmt.setString(1, missao.getTitulo());
        stmt.setString(2, missao.getCategoria());
        stmt.setString(3, missao.getDescricao());
        stmt.setString(4, missao.getDificuldade());
        stmt.setString(5, missao.isAtiva() ? "S" : "N");
        stmt.setString(6, missao.getInstrucoes());
        stmt.setInt(7, missao.getPontos());
        stmt.setString(8, missao.getTipoEvidencia());
        stmt.setInt(9, missao.getTempoMaximoSegundos());
        stmt.setInt(10, missao.getMeta());
    }

    private Missao montar(ResultSet rs) throws SQLException {
        Missao missao = new Missao();
        missao.setId(rs.getLong("ID_MISSAO"));
        missao.setTitulo(rs.getString("NM_MISSAO"));
        missao.setCategoria(rs.getString("CATEGORIA_MISSAO"));
        missao.setDescricao(rs.getString("DESCRICAO_MISSAO"));
        missao.setDificuldade(rs.getString("DIFICULDADE_MISSAO"));
        missao.setAtiva("S".equals(rs.getString("ATIVA_MISSAO")));
        missao.setInstrucoes(rs.getString("INSTRUCOES_MISSAO"));
        missao.setPontos(rs.getInt("PONTOS_MISSAO"));
        missao.setTipoEvidencia(rs.getString("TP_EVIDENCIA_MISSAO"));
        missao.setTempoMaximoSegundos(rs.getInt("TEMPO_MAXIMO_SEG_MISSAO"));
        missao.setMeta(rs.getInt("META_MISSAO"));

        missao.setImagemIlustrativa(rs.getInt("TEM_IMAGEM") == 1 ? "sim" : null);
        return missao;
    }

    private void executar(Connection conn, String sql, Long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}

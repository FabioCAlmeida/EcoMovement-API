package br.com.ecomovement.bo;

import br.com.ecomovement.dao.MovimentoEcopointsDAO;
import br.com.ecomovement.dao.UsuarioDAO;
import br.com.ecomovement.exception.NaoAutorizadoException;
import br.com.ecomovement.exception.NaoEncontradoException;
import br.com.ecomovement.exception.RegraNegocioException;
import br.com.ecomovement.model.LoginAdmin;
import br.com.ecomovement.model.PosicaoRanking;
import br.com.ecomovement.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

@Service
public class UsuarioBO {
    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private MovimentoEcopointsDAO movimentoDAO;

    public Usuario buscar(Long id) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            throw new NaoEncontradoException("Usuário " + id + " não encontrado.");
        }
        return usuario;
    }

    public int somarPontos(Long usuarioId) throws SQLException {
        buscar(usuarioId);
        return movimentoDAO.somarPontosDoUsuario(usuarioId);
    }

    public List<PosicaoRanking> ranking() throws SQLException {
        List<PosicaoRanking> ranking = movimentoDAO.listarPontosPorUsuario();
        ranking.sort(Comparator.comparingInt(PosicaoRanking::getPontos).reversed()
                .thenComparing(PosicaoRanking::getNome));
        for (int i = 0; i < ranking.size(); i++) {
            PosicaoRanking atual = ranking.get(i);
            boolean empatado = i > 0 && ranking.get(i - 1).getPontos() == atual.getPontos();
            atual.setPosicao(empatado ? ranking.get(i - 1).getPosicao() : i + 1);
        }
        return ranking;
    }

    public Usuario loginAdmin(LoginAdmin login) throws SQLException {
        if (login == null || vazio(login.getEmail()) || vazio(login.getSenha())) {
            throw new RegraNegocioException("Informe o e-mail e a senha.");
        }
        Usuario usuario = usuarioDAO.buscarPorEmail(login.getEmail().trim());
        if (usuario == null || !usuario.getSenhaHash().equals(gerarHash(login.getSenha()))) {
            throw new NaoAutorizadoException("E-mail ou senha incorretos.");
        }
        if (!usuario.isAdmin()) {
            throw new NaoAutorizadoException("Este usuário não tem acesso ao Painel EcoGuard.");
        }
        return usuario;
    }

    private String gerarHash(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder texto = new StringBuilder();
            for (byte b : hash) {
                texto.append(String.format("%02x", b));
            }
            return texto.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}

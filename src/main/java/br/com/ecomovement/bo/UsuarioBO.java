package br.com.ecomovement.bo;

import br.com.ecomovement.dao.MovimentoEcopointsDAO;
import br.com.ecomovement.dao.UsuarioDAO;
import br.com.ecomovement.exception.ConflitoException;
import br.com.ecomovement.exception.NaoAutorizadoException;
import br.com.ecomovement.exception.NaoEncontradoException;
import br.com.ecomovement.exception.RegraNegocioException;
import br.com.ecomovement.model.LoginAdmin;
import br.com.ecomovement.model.NovoUsuario;
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

    public Usuario cadastrar(NovoUsuario novo) throws SQLException {
        if (novo == null || vazio(novo.nome()) || vazio(novo.email()) || vazio(novo.senha())) {
            throw new RegraNegocioException("Preencha nome, e-mail e senha.");
        }
        String nome = novo.nome().trim();
        String email = novo.email().trim();
        if (nome.length() < 2 || nome.length() > 150) {
            throw new RegraNegocioException("O nome deve ter entre 2 e 150 caracteres.");
        }
        if (email.length() > 150 || !email.matches("\\S+@\\S+\\.\\S+")) {
            throw new RegraNegocioException("Informe um e-mail válido.");
        }
        if (novo.senha().length() < 6 || novo.senha().length() > 72) {
            throw new RegraNegocioException("A senha deve ter entre 6 e 72 caracteres.");
        }
        if (usuarioDAO.buscarPorEmail(email) != null) {
            throw new ConflitoException("Já existe um usuário cadastrado com este e-mail.");
        }
        try {
            Long id = usuarioDAO.inserir(nome, email, gerarHash(novo.senha()));
            return buscar(id);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) { // ORA-00001: e-mail cadastrado por outra requisição ao mesmo tempo
                throw new ConflitoException("Já existe um usuário cadastrado com este e-mail.");
            }
            throw e;
        }
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

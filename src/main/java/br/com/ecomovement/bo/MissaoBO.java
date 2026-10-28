package br.com.ecomovement.bo;

import br.com.ecomovement.dao.MissaoDAO;
import br.com.ecomovement.dao.ProgressoMissaoDAO;
import br.com.ecomovement.dao.SubmissaoDAO;
import br.com.ecomovement.exception.NaoEncontradoException;
import br.com.ecomovement.exception.RegraNegocioException;
import br.com.ecomovement.model.Arquivo;
import br.com.ecomovement.model.Missao;
import br.com.ecomovement.model.ProgressoMissao;
import br.com.ecomovement.model.Submissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class MissaoBO {
    private static final List<String> DIFICULDADES = List.of("FACIL", "MEDIA", "DIFICIL");
    private static final List<String> TIPOS_EVIDENCIA = List.of("VIDEO", "FOTO", "AUTOMATICA");

    @Autowired
    private MissaoDAO missaoDAO;

    @Autowired
    private SubmissaoDAO submissaoDAO;

    @Autowired
    private ProgressoMissaoDAO progressoDAO;

    @Autowired
    private UsuarioBO usuarioBO;

    @Value("${api.url}")
    private String urlApi;

    public List<Missao> listar(Long usuarioId) throws SQLException {
        if (usuarioId == null) {
            List<Missao> todas = missaoDAO.listar(false);
            todas.forEach(this::completarImagem);
            return todas;
        }
        usuarioBO.buscar(usuarioId);
        List<Missao> missoes = missaoDAO.listar(true);
        List<Submissao> submissoes = submissaoDAO.listarPorUsuario(usuarioId);
        List<ProgressoMissao> progressos = progressoDAO.listarPorUsuario(usuarioId);
        for (Missao missao : missoes) {
            calcularStatus(missao, submissoes, progressos);
            completarImagem(missao);
        }
        return missoes;
    }

    public Missao buscar(Long id, Long usuarioId) throws SQLException {
        Missao missao = buscarMissao(id);
        if (usuarioId != null) {
            usuarioBO.buscar(usuarioId);
            calcularStatus(missao, submissaoDAO.listarPorUsuario(usuarioId), progressoDAO.listarPorUsuario(usuarioId));
        }
        completarImagem(missao);
        return missao;
    }

    public Missao cadastrar(Missao missao) throws SQLException {
        validar(missao);
        Long id = missaoDAO.inserir(missao);
        return buscar(id, null);
    }

    public Missao atualizar(Long id, Missao missao) throws SQLException {
        buscarMissao(id);
        missao.setId(id);
        validar(missao);
        missaoDAO.atualizar(missao);
        return buscar(id, null);
    }

    public void excluir(Long id) throws SQLException {
        buscarMissao(id);
        if (missaoDAO.possuiSubmissoes(id)) {
            throw new RegraNegocioException("Esta missão já tem envios de usuários. Desative a missão em vez de excluir.");
        }
        missaoDAO.excluir(id);
    }

    public Arquivo buscarImagem(Long id) throws SQLException {
        Arquivo imagem = missaoDAO.buscarImagem(id);
        if (imagem == null) {
            throw new NaoEncontradoException("A missão " + id + " não tem imagem.");
        }
        return imagem;
    }

    public void salvarImagem(Long id, byte[] dados, String tipo) throws SQLException {
        buscarMissao(id);
        if (dados == null || dados.length == 0 || tipo == null || !tipo.startsWith("image/")) {
            throw new RegraNegocioException("Envie uma imagem (PNG, JPEG, WEBP ou GIF).");
        }
        if (dados.length > 5 * 1024 * 1024) {
            throw new RegraNegocioException("A imagem deve ter no máximo 5 MB.");
        }
        missaoDAO.salvarImagem(id, new Arquivo(dados, tipo));
    }

    Missao buscarMissao(Long id) throws SQLException {
        Missao missao = missaoDAO.buscarPorId(id);
        if (missao == null) {
            throw new NaoEncontradoException("Missão " + id + " não encontrada.");
        }
        return missao;
    }

    private void calcularStatus(Missao missao, List<Submissao> submissoes, List<ProgressoMissao> progressos) {
        boolean aprovada = false;
        boolean emAnalise = false;
        for (Submissao s : submissoes) {
            if (s.getMissaoId().equals(missao.getId())) {
                if (s.getStatus().equals("APROVADA")) aprovada = true;
                if (s.getStatus().equals("EM_ANALISE")) emAnalise = true;
            }
        }
        int progressoAutomatico = 0;
        for (ProgressoMissao p : progressos) {
            if (p.getMissaoId().equals(missao.getId())) progressoAutomatico = p.getQuantidade();
        }

        if (aprovada) {
            missao.setStatus("CONCLUIDA");
            missao.setProgresso(missao.getMeta());
        } else if (missao.isAutomatica()) {
            int progresso = Math.min(progressoAutomatico, missao.getMeta());
            missao.setProgresso(progresso);
            if (progresso >= missao.getMeta()) {
                missao.setStatus("CONCLUIDA");
            } else if (progresso > 0) {
                missao.setStatus("EM_PROGRESSO");
            } else {
                missao.setStatus("DISPONIVEL");
            }
        } else if (emAnalise) {
            missao.setStatus("EM_ANALISE");
            missao.setProgresso(missao.getMeta());
        } else {
            missao.setStatus("DISPONIVEL");
            missao.setProgresso(0);
        }
    }

    private void validar(Missao missao) {
        if (missao == null) {
            throw new RegraNegocioException("Envie os dados da missão.");
        }
        if (vazio(missao.getTitulo()) || missao.getTitulo().length() > 150) {
            throw new RegraNegocioException("O título é obrigatório e deve ter no máximo 150 caracteres.");
        }
        if (vazio(missao.getCategoria()) || missao.getCategoria().length() > 50) {
            throw new RegraNegocioException("A categoria é obrigatória e deve ter no máximo 50 caracteres.");
        }
        if (missao.getDescricao() != null && missao.getDescricao().length() > 500) {
            throw new RegraNegocioException("A descrição deve ter no máximo 500 caracteres.");
        }
        if (missao.getDificuldade() == null || !DIFICULDADES.contains(missao.getDificuldade())) {
            throw new RegraNegocioException("A dificuldade deve ser FACIL, MEDIA ou DIFICIL.");
        }
        if (missao.getTipoEvidencia() == null || !TIPOS_EVIDENCIA.contains(missao.getTipoEvidencia())) {
            throw new RegraNegocioException("O tipo de evidência deve ser VIDEO, FOTO ou AUTOMATICA.");
        }
        if (missao.getPontos() < 1 || missao.getPontos() > 1000) {
            throw new RegraNegocioException("Os pontos devem estar entre 1 e 1000.");
        }
        if (missao.getTempoMaximoSegundos() < 0) {
            throw new RegraNegocioException("O tempo máximo não pode ser negativo.");
        }
        if (missao.getMeta() < 1) {
            missao.setMeta(1);
        }
        if (!missao.isAutomatica() && missao.getMeta() != 1) {
            throw new RegraNegocioException("Missões de foto ou vídeo têm meta 1 (um envio).");
        }
    }

    private void completarImagem(Missao missao) {
        if (missao.getImagemIlustrativa() != null) {
            missao.setImagemIlustrativa(urlApi + "/missoes/" + missao.getId() + "/imagem");
        }
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}

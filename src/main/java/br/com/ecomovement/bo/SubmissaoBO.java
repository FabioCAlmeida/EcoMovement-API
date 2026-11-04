package br.com.ecomovement.bo;

import br.com.ecomovement.dao.EvidenciaDAO;
import br.com.ecomovement.dao.MovimentoEcopointsDAO;
import br.com.ecomovement.dao.SubmissaoDAO;
import br.com.ecomovement.dao.ValidacaoEcoguardDAO;
import br.com.ecomovement.exception.NaoEncontradoException;
import br.com.ecomovement.exception.RegraNegocioException;
import br.com.ecomovement.model.Arquivo;
import br.com.ecomovement.model.Evidencia;
import br.com.ecomovement.model.Missao;
import br.com.ecomovement.model.MovimentoEcopoints;
import br.com.ecomovement.model.Submissao;
import br.com.ecomovement.model.ValidacaoEcoguard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissaoBO {
    private static final int TAMANHO_MAXIMO = 50 * 1024 * 1024;
    private static final List<String> STATUS = List.of("EM_ANALISE", "APROVADA", "REJEITADA");

    @Autowired
    private SubmissaoDAO submissaoDAO;

    @Autowired
    private EvidenciaDAO evidenciaDAO;

    @Autowired
    private ValidacaoEcoguardDAO validacaoDAO;

    @Autowired
    private MovimentoEcopointsDAO movimentoDAO;

    @Autowired
    private MissaoBO missaoBO;

    @Autowired
    private UsuarioBO usuarioBO;

    @Value("${api.url}")
    private String urlApi;

    public Submissao enviar(Long usuarioId, Long missaoId, String tipoEvidencia, Integer duracaoSegundos,
                            byte[] arquivo, String tipoArquivo) throws SQLException {
        if (usuarioId == null || missaoId == null) {
            throw new RegraNegocioException("Informe usuarioId e missaoId.");
        }
        usuarioBO.buscar(usuarioId);
        Missao missao = missaoBO.buscarMissao(missaoId);
        validarEvidencia(missao, tipoEvidencia, duracaoSegundos, arquivo, tipoArquivo);

        for (Submissao s : submissaoDAO.listarPorUsuario(usuarioId)) {
            if (s.getMissaoId().equals(missaoId) && !s.getStatus().equals("REJEITADA")) {
                throw new RegraNegocioException("Você já enviou esta missão. Aguarde a análise.");
            }
        }

        Long id = submissaoDAO.inserir(new Submissao(usuarioId, missaoId, "EM_ANALISE", LocalDateTime.now()));

        Evidencia evidencia = new Evidencia(id, tipoEvidencia, arquivo, tipoArquivo, duracaoSegundos);
        evidencia.setReferenciaArquivo("/submissoes/" + id + "/evidencia");
        evidenciaDAO.inserir(evidencia);

        return buscar(id);
    }

    public List<Submissao> listarDoUsuario(Long usuarioId) throws SQLException {
        usuarioBO.buscar(usuarioId);
        return completarUrls(submissaoDAO.listarPorUsuario(usuarioId));
    }

    public List<Submissao> listarPorStatus(String status) throws SQLException {
        if (!STATUS.contains(status)) {
            throw new RegraNegocioException("Status inválido. Use EM_ANALISE, APROVADA ou REJEITADA.");
        }
        return completarUrls(submissaoDAO.listarPorStatus(status));
    }

    public Submissao buscar(Long id) throws SQLException {
        Submissao submissao = submissaoDAO.buscarPorId(id);
        if (submissao == null) {
            throw new NaoEncontradoException("Envio " + id + " não encontrado.");
        }
        completarUrls(List.of(submissao));
        return submissao;
    }

    public Submissao aprovar(Long id) throws SQLException {
        Submissao submissao = buscarEmAnalise(id);
        submissaoDAO.atualizarStatus(id, "APROVADA");
        validacaoDAO.inserir(new ValidacaoEcoguard(id, "APROVADA", 0));
        movimentoDAO.inserir(new MovimentoEcopoints(id, "CREDITO", submissao.getPontos()));
        return buscar(id);
    }

    public Submissao rejeitar(Long id) throws SQLException {
        buscarEmAnalise(id);
        submissaoDAO.atualizarStatus(id, "REJEITADA");
        validacaoDAO.inserir(new ValidacaoEcoguard(id, "REJEITADA", 100));
        return buscar(id);
    }

    public void excluir(Long id) throws SQLException {
        buscar(id);
        submissaoDAO.excluir(id);
    }

    public int reiniciarDoUsuario(Long usuarioId) throws SQLException {
        usuarioBO.buscar(usuarioId);
        int removidos = 0;
        for (Submissao s : submissaoDAO.listarPorUsuario(usuarioId)) {
            if (!missaoBO.buscarMissao(s.getMissaoId()).isAutomatica()) {
                submissaoDAO.excluir(s.getId());
                removidos++;
            }
        }
        return removidos;
    }

    public Arquivo buscarEvidencia(Long id) throws SQLException {
        Arquivo arquivo = evidenciaDAO.buscarArquivoDaSubmissao(id);
        if (arquivo == null) {
            throw new NaoEncontradoException("O envio " + id + " não tem arquivo.");
        }
        return arquivo;
    }

    private Submissao buscarEmAnalise(Long id) throws SQLException {
        Submissao submissao = buscar(id);
        if (!submissao.getStatus().equals("EM_ANALISE")) {
            throw new RegraNegocioException("Este envio já foi avaliado (" + submissao.getStatus() + ").");
        }
        return submissao;
    }

    private void validarEvidencia(Missao missao, String tipoEvidencia, Integer duracaoSegundos,
                                  byte[] arquivo, String tipoArquivo) {
        if (!missao.isAtiva()) {
            throw new RegraNegocioException("Esta missão não está mais disponível.");
        }
        if (missao.isAutomatica()) {
            throw new RegraNegocioException("Esta missão é automática e não aceita envio.");
        }
        if (!missao.getTipoEvidencia().equals(tipoEvidencia)) {
            throw new RegraNegocioException("Esta missão exige evidência do tipo " + missao.getTipoEvidencia() + ".");
        }
        if (arquivo == null || arquivo.length == 0) {
            throw new RegraNegocioException("Envie o arquivo da evidência.");
        }
        if (arquivo.length > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("O arquivo deve ter no máximo 50 MB.");
        }
        if (tipoEvidencia.equals("FOTO") && (tipoArquivo == null || !tipoArquivo.startsWith("image/"))) {
            throw new RegraNegocioException("Envie uma foto.");
        }
        if (tipoEvidencia.equals("VIDEO") && (tipoArquivo == null || !tipoArquivo.startsWith("video/"))) {
            throw new RegraNegocioException("Envie um vídeo.");
        }

        if (tipoEvidencia.equals("VIDEO") && missao.getTempoMaximoSegundos() > 0
                && duracaoSegundos != null && duracaoSegundos > missao.getTempoMaximoSegundos() + 1) {
            throw new RegraNegocioException("O vídeo passou do tempo máximo de " + missao.getTempoMaximoSegundos() + " segundos.");
        }
    }

    private List<Submissao> completarUrls(List<Submissao> lista) {
        for (Submissao s : lista) {
            if (s.getEvidenciaUrl() != null) {
                s.setEvidenciaUrl(urlApi + "/submissoes/" + s.getId() + "/evidencia");
            }
        }
        return lista;
    }
}

package br.com.ecomovement.resource;

import br.com.ecomovement.bo.SubmissaoBO;
import br.com.ecomovement.model.Arquivo;
import br.com.ecomovement.model.Submissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/submissoes")
public class SubmissaoResource {
    @Autowired
    private SubmissaoBO submissaoBO;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Submissao> enviar(@RequestParam Long usuarioId,
                                            @RequestParam Long missaoId,
                                            @RequestParam String tipoEvidencia,
                                            @RequestParam(required = false) Integer duracaoSegundos,
                                            @RequestParam(required = false) MultipartFile arquivo)
            throws SQLException, IOException {
        byte[] dados = arquivo == null ? null : arquivo.getBytes();
        String tipoArquivo = arquivo == null ? null : arquivo.getContentType();
        Submissao criada = submissaoBO.enviar(usuarioId, missaoId, tipoEvidencia, duracaoSegundos, dados, tipoArquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public List<Submissao> listar(@RequestParam(defaultValue = "EM_ANALISE") String status) throws SQLException {
        return submissaoBO.listarPorStatus(status);
    }

    @GetMapping("/{id}")
    public Submissao buscar(@PathVariable Long id) throws SQLException {
        return submissaoBO.buscar(id);
    }

    @GetMapping("/{id}/evidencia")
    public ResponseEntity<byte[]> evidencia(@PathVariable Long id) throws SQLException {
        Arquivo arquivo = submissaoBO.buscarEvidencia(id);

        String tipo = arquivo.getTipo().split(";")[0].trim();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, tipo).body(arquivo.getDados());
    }

    @PutMapping("/{id}/aprovar")
    public Submissao aprovar(@PathVariable Long id) throws SQLException {
        return submissaoBO.aprovar(id);
    }

    @PutMapping("/{id}/rejeitar")
    public Submissao rejeitar(@PathVariable Long id) throws SQLException {
        return submissaoBO.rejeitar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws SQLException {
        submissaoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

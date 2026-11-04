package br.com.ecomovement.resource;

import br.com.ecomovement.bo.MissaoBO;
import model.Arquivo;
import model.Missao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/missoes")
public class MissaoResource {
    @Autowired
    private MissaoBO missaoBO;

    @GetMapping
    public List<Missao> listar(@RequestParam(required = false) Long usuarioId) throws SQLException {
        return missaoBO.listar(usuarioId);
    }

    @GetMapping("/{id}")
    public Missao buscar(@PathVariable Long id, @RequestParam(required = false) Long usuarioId) throws SQLException {
        return missaoBO.buscar(id, usuarioId);
    }

    @PostMapping
    public ResponseEntity<Missao> cadastrar(@RequestBody Missao missao) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(missaoBO.cadastrar(missao));
    }

    @PutMapping("/{id}")
    public Missao atualizar(@PathVariable Long id, @RequestBody Missao missao) throws SQLException {
        return missaoBO.atualizar(id, missao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws SQLException {
        missaoBO.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> imagem(@PathVariable Long id) throws SQLException {
        Arquivo imagem = missaoBO.buscarImagem(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, imagem.getTipo()).body(imagem.getDados());
    }

    @PutMapping(value = "/{id}/imagem", consumes = "image/*")
    public ResponseEntity<Void> enviarImagem(@PathVariable Long id, @RequestBody byte[] dados,
                                             @RequestHeader(HttpHeaders.CONTENT_TYPE) String tipo) throws SQLException {
        missaoBO.salvarImagem(id, dados, tipo);
        return ResponseEntity.noContent().build();
    }
}

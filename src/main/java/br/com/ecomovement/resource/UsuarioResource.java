package br.com.ecomovement.resource;

import br.com.ecomovement.bo.SubmissaoBO;
import br.com.ecomovement.bo.UsuarioBO;
import br.com.ecomovement.model.NovoUsuario;
import br.com.ecomovement.model.Submissao;
import br.com.ecomovement.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioResource {
    @Autowired
    private SubmissaoBO submissaoBO;

    @Autowired
    private UsuarioBO usuarioBO;

    @PostMapping
    public ResponseEntity<Usuario> cadastrar(@RequestBody NovoUsuario novo) throws SQLException {
        Usuario criado = usuarioBO.cadastrar(novo);
        return ResponseEntity.created(URI.create("/usuarios/" + criado.getId())).body(criado);
    }

    @GetMapping("/{usuarioId}")
    public Usuario buscar(@PathVariable Long usuarioId) throws SQLException {
        return usuarioBO.buscar(usuarioId);
    }

    @GetMapping("/{usuarioId}/submissoes")
    public List<Submissao> submissoes(@PathVariable Long usuarioId) throws SQLException {
        return submissaoBO.listarDoUsuario(usuarioId);
    }

    @DeleteMapping("/{usuarioId}/submissoes")
    public Map<String, Integer> reiniciar(@PathVariable Long usuarioId) throws SQLException {
        return Map.of("removidos", submissaoBO.reiniciarDoUsuario(usuarioId));
    }

    @GetMapping("/{usuarioId}/pontos")
    public Map<String, Integer> pontos(@PathVariable Long usuarioId) throws SQLException {
        return Map.of("total", usuarioBO.somarPontos(usuarioId));
    }
}

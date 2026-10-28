package br.com.ecomovement.resource;

import br.com.ecomovement.bo.SubmissaoBO;
import br.com.ecomovement.bo.UsuarioBO;
import br.com.ecomovement.model.Submissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios/{usuarioId}")
public class UsuarioResource {
    @Autowired
    private SubmissaoBO submissaoBO;

    @Autowired
    private UsuarioBO usuarioBO;

    @GetMapping("/submissoes")
    public List<Submissao> submissoes(@PathVariable Long usuarioId) throws SQLException {
        return submissaoBO.listarDoUsuario(usuarioId);
    }

    @DeleteMapping("/submissoes")
    public Map<String, Integer> reiniciar(@PathVariable Long usuarioId) throws SQLException {
        return Map.of("removidos", submissaoBO.reiniciarDoUsuario(usuarioId));
    }

    @GetMapping("/pontos")
    public Map<String, Integer> pontos(@PathVariable Long usuarioId) throws SQLException {
        return Map.of("total", usuarioBO.somarPontos(usuarioId));
    }
}

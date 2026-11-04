package br.com.ecomovement.resource;

import br.com.ecomovement.bo.UsuarioBO;
import br.com.ecomovement.model.LoginAdmin;
import br.com.ecomovement.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminResource {
    @Autowired
    private UsuarioBO usuarioBO;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginAdmin login) throws SQLException {
        Usuario admin = usuarioBO.loginAdmin(login);
        return Map.of("mensagem", "Login realizado com sucesso.", "id", admin.getId(), "nome", admin.getNome());
    }
}

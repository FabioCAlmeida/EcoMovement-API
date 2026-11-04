package br.com.ecomovement.resource;

import br.com.ecomovement.bo.UsuarioBO;
import br.com.ecomovement.model.PosicaoRanking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/ranking")
public class RankingResource {
    @Autowired
    private UsuarioBO usuarioBO;

    @GetMapping
    public List<PosicaoRanking> listar() throws SQLException {
        return usuarioBO.ranking();
    }
}

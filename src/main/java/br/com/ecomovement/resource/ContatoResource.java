package br.com.ecomovement.resource;

import br.com.ecomovement.bo.ContatoBO;
import br.com.ecomovement.model.Contato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/contatos")
public class ContatoResource {
    @Autowired
    private ContatoBO contatoBO;

    @PostMapping
    public ResponseEntity<Void> enviar(@RequestBody Contato contato) throws SQLException {
        contatoBO.enviar(contato);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<Contato> listar() throws SQLException {
        return contatoBO.listar();
    }
}

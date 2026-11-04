package br.com.ecomovement.bo;

import br.com.ecomovement.dao.ContatoDAO;
import br.com.ecomovement.exception.NaoEncontradoException;
import br.com.ecomovement.exception.RegraNegocioException;
import model.Contato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ContatoBO {
    @Autowired
    private ContatoDAO contatoDAO;

    public void enviar(Contato contato) throws SQLException {
        if (contato == null || vazio(contato.getNome()) || vazio(contato.getEmail()) || vazio(contato.getMensagem())) {
            throw new RegraNegocioException("Preencha nome, e-mail e mensagem.");
        }
        if (!contato.getEmail().matches("\\S+@\\S+\\.\\S+")) {
            throw new RegraNegocioException("Informe um e-mail válido.");
        }
        if (contato.getMensagem().length() > 2000) {
            throw new RegraNegocioException("A mensagem deve ter no máximo 2000 caracteres.");
        }
        contatoDAO.inserir(contato);
    }

    public List<Contato> listar() throws SQLException {
        return contatoDAO.listar();
    }

    public Contato buscar(Long id) throws SQLException {
        Contato contato = contatoDAO.buscarPorId(id);
        if (contato == null) {
            throw new NaoEncontradoException("Contato " + id + " não encontrado.");
        }
        return contato;
    }

    public void excluir(Long id) throws SQLException {
        if (contatoDAO.excluir(id) == 0) {
            throw new NaoEncontradoException("Contato " + id + " não encontrado.");
        }
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}

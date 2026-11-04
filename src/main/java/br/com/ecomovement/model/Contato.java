package br.com.ecomovement.model;

public class Contato {
    private Long id;
    private String nome;
    private String email;
    private String mensagem;

    public Contato() {
    }

    public Contato(String nome, String email, String mensagem) {
        this.nome = nome;
        this.email = email;
        this.mensagem = mensagem;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}

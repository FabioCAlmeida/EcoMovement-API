package model;

public class MovimentoEcopoints {
    private Long id;
    private Long submissaoId;
    private String tipo;
    private int quantidade;

    public MovimentoEcopoints() {
    }

    public MovimentoEcopoints(Long submissaoId, String tipo, int quantidade) {
        this.submissaoId = submissaoId;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissaoId() { return submissaoId; }
    public void setSubmissaoId(Long submissaoId) { this.submissaoId = submissaoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}

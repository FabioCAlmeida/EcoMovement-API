package model;

public class ProgressoMissao {
    private Long usuarioId;
    private Long missaoId;
    private int quantidade;

    public ProgressoMissao() {
    }

    public ProgressoMissao(Long usuarioId, Long missaoId, int quantidade) {
        this.usuarioId = usuarioId;
        this.missaoId = missaoId;
        this.quantidade = quantidade;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getMissaoId() { return missaoId; }
    public void setMissaoId(Long missaoId) { this.missaoId = missaoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}

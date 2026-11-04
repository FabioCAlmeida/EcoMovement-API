package model;

public class PosicaoRanking {
    private int posicao;
    private Long usuarioId;
    private String nome;
    private int pontos;

    public PosicaoRanking() {
    }

    public PosicaoRanking(Long usuarioId, String nome, int pontos) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.pontos = pontos;
    }

    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }
}

package br.com.ecomovement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Missao {
    private Long id;
    private String titulo;
    private String categoria;
    private String descricao;
    private String dificuldade;
    private boolean ativa;
    private String instrucoes;
    private int pontos;
    private String tipoEvidencia;
    private int tempoMaximoSegundos;
    private int meta;
    private String imagemIlustrativa;

    private int progresso;
    private String status;

    public Missao() {
    }

    public Missao(Long id, String titulo, String categoria, String descricao, String dificuldade, int pontos,
                  String tipoEvidencia) {
        this.id = id;
        this.titulo = titulo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.dificuldade = dificuldade;
        this.pontos = pontos;
        this.tipoEvidencia = tipoEvidencia;
        this.ativa = true;
        this.meta = 1;
    }

    @JsonIgnore
    public boolean isAutomatica() {
        return "AUTOMATICA".equals(tipoEvidencia);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public String getInstrucoes() { return instrucoes; }
    public void setInstrucoes(String instrucoes) { this.instrucoes = instrucoes; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public String getTipoEvidencia() { return tipoEvidencia; }
    public void setTipoEvidencia(String tipoEvidencia) { this.tipoEvidencia = tipoEvidencia; }

    public int getTempoMaximoSegundos() { return tempoMaximoSegundos; }
    public void setTempoMaximoSegundos(int tempoMaximoSegundos) { this.tempoMaximoSegundos = tempoMaximoSegundos; }

    public int getMeta() { return meta; }
    public void setMeta(int meta) { this.meta = meta; }

    public String getImagemIlustrativa() { return imagemIlustrativa; }
    public void setImagemIlustrativa(String imagemIlustrativa) { this.imagemIlustrativa = imagemIlustrativa; }

    public int getProgresso() { return progresso; }
    public void setProgresso(int progresso) { this.progresso = progresso; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

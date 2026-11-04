package model;

import java.time.LocalDateTime;

public class Submissao {
    private Long id;
    private Long usuarioId;
    private Long missaoId;
    private String status;
    private LocalDateTime enviadaEm;

    private String titulo;
    private int pontos;
    private String tipoEvidencia;
    private Integer duracaoSegundos;
    private LocalDateTime concluidaEm;
    private String evidenciaUrl;

    public Submissao() {
    }

    public Submissao(Long usuarioId, Long missaoId, String status, LocalDateTime enviadaEm) {
        this.usuarioId = usuarioId;
        this.missaoId = missaoId;
        this.status = status;
        this.enviadaEm = enviadaEm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getMissaoId() { return missaoId; }
    public void setMissaoId(Long missaoId) { this.missaoId = missaoId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEnviadaEm() { return enviadaEm; }
    public void setEnviadaEm(LocalDateTime enviadaEm) { this.enviadaEm = enviadaEm; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public String getTipoEvidencia() { return tipoEvidencia; }
    public void setTipoEvidencia(String tipoEvidencia) { this.tipoEvidencia = tipoEvidencia; }

    public Integer getDuracaoSegundos() { return duracaoSegundos; }
    public void setDuracaoSegundos(Integer duracaoSegundos) { this.duracaoSegundos = duracaoSegundos; }

    public LocalDateTime getConcluidaEm() { return concluidaEm; }
    public void setConcluidaEm(LocalDateTime concluidaEm) { this.concluidaEm = concluidaEm; }

    public String getEvidenciaUrl() { return evidenciaUrl; }
    public void setEvidenciaUrl(String evidenciaUrl) { this.evidenciaUrl = evidenciaUrl; }
}

package br.com.ecomovement.model;

import java.time.LocalDateTime;

public class Evidencia {
    private Long id;
    private Long submissaoId;
    private String tipo;
    private String referenciaArquivo;
    private LocalDateTime dataCaptura;
    private byte[] arquivo;
    private String tipoArquivo;
    private Integer duracaoSegundos;

    public Evidencia() {
    }

    public Evidencia(Long submissaoId, String tipo, byte[] arquivo, String tipoArquivo, Integer duracaoSegundos) {
        this.submissaoId = submissaoId;
        this.tipo = tipo;
        this.arquivo = arquivo;
        this.tipoArquivo = tipoArquivo;
        this.duracaoSegundos = duracaoSegundos;
        this.dataCaptura = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissaoId() { return submissaoId; }
    public void setSubmissaoId(Long submissaoId) { this.submissaoId = submissaoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getReferenciaArquivo() { return referenciaArquivo; }
    public void setReferenciaArquivo(String referenciaArquivo) { this.referenciaArquivo = referenciaArquivo; }

    public LocalDateTime getDataCaptura() { return dataCaptura; }
    public void setDataCaptura(LocalDateTime dataCaptura) { this.dataCaptura = dataCaptura; }

    public byte[] getArquivo() { return arquivo; }
    public void setArquivo(byte[] arquivo) { this.arquivo = arquivo; }

    public String getTipoArquivo() { return tipoArquivo; }
    public void setTipoArquivo(String tipoArquivo) { this.tipoArquivo = tipoArquivo; }

    public Integer getDuracaoSegundos() { return duracaoSegundos; }
    public void setDuracaoSegundos(Integer duracaoSegundos) { this.duracaoSegundos = duracaoSegundos; }
}

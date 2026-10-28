package br.com.ecomovement.model;

import java.time.LocalDateTime;

public class ValidacaoEcoguard {
    private Long id;
    private Long submissaoId;
    private String resultado;
    private int scoreRisco;
    private LocalDateTime data;

    public ValidacaoEcoguard() {
    }

    public ValidacaoEcoguard(Long submissaoId, String resultado, int scoreRisco) {
        this.submissaoId = submissaoId;
        this.resultado = resultado;
        this.scoreRisco = scoreRisco;
        this.data = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissaoId() { return submissaoId; }
    public void setSubmissaoId(Long submissaoId) { this.submissaoId = submissaoId; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public int getScoreRisco() { return scoreRisco; }
    public void setScoreRisco(int scoreRisco) { this.scoreRisco = scoreRisco; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
}

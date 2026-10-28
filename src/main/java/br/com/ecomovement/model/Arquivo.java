package br.com.ecomovement.model;

public class Arquivo {
    private byte[] dados;
    private String tipo;

    public Arquivo(byte[] dados, String tipo) {
        this.dados = dados;
        this.tipo = tipo;
    }

    public byte[] getDados() { return dados; }
    public String getTipo() { return tipo; }
}

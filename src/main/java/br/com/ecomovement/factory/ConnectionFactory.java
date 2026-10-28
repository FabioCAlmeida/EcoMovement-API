package br.com.ecomovement.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class ConnectionFactory {
    @Value("${db.url}")
    private String url;

    @Value("${db.usuario}")
    private String usuario;

    @Value("${db.senha}")
    private String senha;

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}

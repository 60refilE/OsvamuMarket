package py.edu.fpuna.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RastreoDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/OSVAMUMARKETDB";
    private static final String USUARIO = "admin";
    private static final String CONTRASENA = "123";

    public boolean existeCompra(int idCompra) {

        String sql = "SELECT id FROM compras WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompra);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al verificar compra: " + e.getMessage());
        }
        return false;
    }
}

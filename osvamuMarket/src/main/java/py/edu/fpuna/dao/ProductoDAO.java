package py.edu.fpuna.dao;

import py.edu.fpuna.entities.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/OSVAMUMARKETDB";
    private static final String USUARIO = "admin";
    private static final String CONTRASENA = "123";

    public List<Producto> obtenerDisponibles(int i) {

        List<Producto> lista = new ArrayList<>();
        String sql =
                "SELECT id, name, quantity, price FROM productos WHERE quantity > 0 ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, 10);
            stmt.setInt(2, i*10);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("price"),
                            rs.getInt("quantity")
                    );
                    lista.add(p);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener productos: " + e.getMessage());
        }

        return lista;
    }
}
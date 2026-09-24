package py.edu.fpuna.dao;

import java.sql.*;
import java.util.Map;

public class CompraDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/OSVAMUMARKETDB";
    private static final String USUARIO = "admin";
    private static final String CONTRASENA = "123";

    /**
     * Registra la compra y sus líneas de detalle en una transacción.
     * @param carrito mapa idProducto -> cantidad
     * @param total monto total calculado por el socket
     * @return id de la compra generada, o -1 si falla
     */
    public int registrarCompra(Map<Integer, Integer> carrito, int total) {
        if (carrito == null || carrito.isEmpty()) {
            return -1;
        }

        String sqlCompra = "INSERT INTO compras(total) VALUES (?)";
        String sqlPrecio = "SELECT price, quantity FROM productos WHERE id = ?";
        String sqlDetalle = "INSERT INTO detalle_compra(compra_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE productos SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, CONTRASENA)) {
            conn.setAutoCommit(false);

            try {
                int idCompra;
                try (PreparedStatement stmt = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, total);
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return -1;
                        }
                        idCompra = rs.getInt(1);
                    }
                }

                // Recorre el carrito e inserta cada línea en detalle_compra
                for (Map.Entry<Integer, Integer> e : carrito.entrySet()) {
                    int idProducto = e.getKey();
                    int cantidad = e.getValue();

                    int precio;
                    try (PreparedStatement stmt = conn.prepareStatement(sqlPrecio)) {
                        stmt.setInt(1, idProducto);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (!rs.next()) {
                                conn.rollback();
                                return -1; // producto no existe
                            }
                            precio = rs.getInt("price");
                            if (rs.getInt("quantity") < cantidad) {
                                conn.rollback();
                                return -1; // stock insuficiente
                            }
                        }
                    }

                    int subtotal = precio * cantidad;
                    try (PreparedStatement stmt = conn.prepareStatement(sqlDetalle)) {
                        stmt.setInt(1, idCompra);
                        stmt.setInt(2, idProducto);
                        stmt.setInt(3, cantidad);
                        stmt.setInt(4, precio);
                        stmt.setInt(5, subtotal);
                        stmt.executeUpdate();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(sqlStock)) {
                        stmt.setInt(1, cantidad);
                        stmt.setInt(2, idProducto);
                        stmt.setInt(3, cantidad);
                        if (stmt.executeUpdate() == 0) {
                            conn.rollback();
                            return -1; // stock insuficiente (carrera)
                        }
                    }
                }

                conn.commit();
                return idCompra;

            } catch (SQLException ex) {
                conn.rollback();
                System.out.println("Error al registrar compra: " + ex.getMessage());
                return -1;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar compra: " + e.getMessage());
            return -1;
        }
    }
}

package py.edu.fpuna.sockets;
import com.google.gson.Gson;
import py.edu.fpuna.dao.CompraDAO;
import py.edu.fpuna.dao.ProductoDAO;
import py.edu.fpuna.dto.MensajeClienteOrdenDeCompra;
import py.edu.fpuna.dto.MensajeServidor;
import py.edu.fpuna.entities.Producto;
import py.edu.fpuna.enums.Opciones;
import py.edu.fpuna.enums.TipoDeMensaje;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdenDeCompraSocket extends Thread {

    private static final int PORT = 8000;

    private Socket socket;

    private final ProductoDAO dao = new ProductoDAO();
    private final CompraDAO compraDAO = new CompraDAO();

    public OrdenDeCompraSocket(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        int pagina = 0;
        Gson gson = new Gson();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            List<Producto> productos = dao.obtenerDisponibles(0);
            String json = gson.toJson(new MensajeServidor(TipoDeMensaje.PRODUCTOS,
                    "Productos disponibles",productos));
            out.println(json);

            MensajeClienteOrdenDeCompra mensaje;


            Map<Integer, Integer> carrito = new HashMap<>(); // idProducto -> cantidad

            boolean conectado = true;
            while (conectado) {

                String jsonIn = in.readLine();

                if (jsonIn == null) {

                    conectado = false;
                    continue;
                }

                mensaje=gson.fromJson(jsonIn, MensajeClienteOrdenDeCompra.class);
                Opciones opcion =  mensaje.getOpcion();

                switch (opcion) {

                    case AGREGAR:
                        Producto productoAgregar = dao.obtenerPorId(mensaje.getIdProducto());
                        if (productoAgregar == null) {

                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "El producto solicitado no existe.",
                                    null));
                            out.println(json);

                            break;
                        }
                        int cantAgregar = mensaje.getCantidad() > 0 ? mensaje.getCantidad() : 1;
                        carrito.merge(productoAgregar.getId(), cantAgregar, Integer::sum);
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.OK,
                                "Operacion exitosa!", null));
                        out.println(json);
                        break;

                    case ELIMINAR:
                        if (!carrito.containsKey(mensaje.getIdProducto())) {
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "El producto no está en el carrito.",
                                    null));
                            out.println(json);
                            break;
                        }
                        carrito.remove(mensaje.getIdProducto());
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.OK,
                                "Operacion exitosa!", null));
                        out.println(json);
                        break;

                    case BORRAR_TODO:
                        carrito.clear();
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.OK,
                                "Carrito vaciado.", null));
                        out.println(json);
                        break;

                    case SIGUIENTE:
                        pagina++;
                        productos = dao.obtenerDisponibles(pagina);
                        if (productos.isEmpty()) {
                            pagina--;
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "No hay más productos.",
                                    null));
                        } else {
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.PRODUCTOS,
                                    "Productos disponibles",
                                    productos));
                        }
                        out.println(json);
                        break;

                    case ANTERIOR:
                        if (!(pagina > 0)) {
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "Ya está en la primera página.",
                                    null));
                            out.println(json);
                            break;
                        }
                        pagina--;
                        productos = dao.obtenerDisponibles(pagina);
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.PRODUCTOS,
                                "Productos disponibles",
                                productos));
                        out.println(json);
                        break;

                    case COMPRAR:
                        if (carrito.isEmpty()) {
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "El carrito está vacío.",
                                    null));
                            out.println(json);
                            break;
                        }
                        // Calcular el monto total con precios vigentes
                        int total = 0;
                        boolean errorStock = false;
                        for (Map.Entry<Integer, Integer> item : carrito.entrySet()) {
                            Producto p = dao.obtenerPorId(item.getKey());
                            if (p == null || p.getQuantity() < item.getValue()) {
                                errorStock = true;
                                break;
                            }
                            total += p.getPrice() * item.getValue();
                        }
                        if (errorStock) {
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "Stock insuficiente o producto inexistente.",
                                    null));
                            out.println(json);
                            break;
                        }
                        int idCompra = compraDAO.registrarCompra(carrito, total);
                        if (idCompra <= 0) {
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "No se pudo registrar la compra.",
                                    null));
                            out.println(json);
                            break;
                        }
                        carrito.clear();
                        System.out.println("Compra " + idCompra + " realizada con exito, cerrando conexion...");
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.OK,
                                "Compra " + idCompra + " realizada con exito. Total: " + total,
                                null));
                        out.println(json);
                        conectado = false;
                        break;

                    case CANCELAR:
                        System.out.println("El cliente ha cancelado la compra, cerrando conexion...");
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.OK,
                                "Conexion cerrada.",
                                null));
                        out.println(json);
                        conectado = false;
                        break;

                    case CARRITO:
                        List<Producto> vistaCarrito = new ArrayList<>();
                        for (Map.Entry<Integer, Integer> item : carrito.entrySet()) {
                            Producto p = dao.obtenerPorId(item.getKey());
                            if (p != null) {
                                p.setQuantity(item.getValue()); // cantidad comprada
                                vistaCarrito.add(p);
                            }
                        }
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.PRODUCTOS,
                                "Su carrito actual:",
                                vistaCarrito));
                        out.println(json);
                        break;

                    default:
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                "Opcion no reconocida.",
                                null));
                        out.println(json);
                        break;
                }
            }


        } catch (IOException e) {
            System.out.println("Error con el cliente: " + e.getMessage());
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado, esperando conexiones...");

        while(true){
            Socket socketCliente = serverSocket.accept();
            System.out.println("Conexion aceptada por el cliente.");

            new OrdenDeCompraSocket(socketCliente).start();

        }
    }
}

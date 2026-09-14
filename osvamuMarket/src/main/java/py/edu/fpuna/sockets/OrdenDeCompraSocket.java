package py.edu.fpuna.sockets;
import com.google.gson.Gson;
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
import java.util.List;

public class OrdenDeCompraSocket extends Thread {

    private static final int PORT = 6767;

    private Socket socket;

    private final ProductoDAO dao = new ProductoDAO();

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
            String json = gson.toJson(new MensajeServidor(TipoDeMensaje.PRODUCTOS,"Productos disponibles",productos));
            out.println(json);

            MensajeClienteOrdenDeCompra mensaje;


            List<Producto> carrito=new ArrayList<Producto>();

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
                        carrito.add(productoAgregar);
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.OK,
                                "Operacion exitosa!", null));
                        out.println(json);
                        break;

                    case ELIMINAR:
                        Producto productoEliminar = dao.obtenerPorId(mensaje.getIdProducto());
                        if (productoEliminar == null) {
                            json = gson.toJson(new MensajeServidor(TipoDeMensaje.ERROR,
                                    "El producto solicitado no existe.",
                                    null));
                            out.println(json);
                            break;
                        }
                        carrito.remove(productoEliminar);
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
                        System.out.println("Compra realizada con exito, cerrando conexion...");
                        // INSERT en compras
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.OK,
                                "Compra realizada con exito.",
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
                        json = gson.toJson(new MensajeServidor(TipoDeMensaje.PRODUCTOS,
                                "Su carrito actual:",
                                carrito));
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

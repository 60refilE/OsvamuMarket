package py.edu.fpuna.sockets;
import com.google.gson.Gson;
import py.edu.fpuna.dao.ProductoDAO;
import py.edu.fpuna.dto.MensajeClienteOrdenDeCompra;
import py.edu.fpuna.entities.Producto;
import py.edu.fpuna.enums.Opciones;

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
            String json = gson.toJson(productos);
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
                        //Mandar error si se ingresa una id invalida(null eb dao.obtenerPodId
                        carrito.add(dao.obtenerPorId(mensaje.getIdProducto()));
                        //OK!
                        break;

                    case ELIMINAR:
                        carrito.remove(dao.obtenerPorId(mensaje.getIdProducto()));
                        //OK!
                        break;

                    case BORRAR_TODO:
                        carrito.clear();
                        //OK!
                        break;

                    case SIGUIENTE:
                        pagina++;//Tratar el caso en el que ya no quedan filas
                        //NOTA
                        productos = dao.obtenerDisponibles(pagina);
                        json = gson.toJson(productos);
                        out.println(json);

                        break;

                    case ANTERIOR:
                        if( !(pagina>0) ){
                            //ERROR/NOTA

                            break;
                        }
                        pagina--;

                        productos = dao.obtenerDisponibles(pagina);
                        json = gson.toJson(productos);
                        out.println(json);

                        break;

                    case COMPRAR:
                        System.out.println("Compra realizada con exito, cerrando conexion...");
                        //OK!
                        conectado = false;

                        break;

                    case CANCELAR:
                        //OK!
                        System.out.println("El cliente ha cancelado la compra, cerrando conexion...");
                        conectado = false;
                        break;


                    case CARRITO:
                        //NOTA
                        json = gson.toJson(carrito);
                        out.println(json);
                        break;
                    default:
                        //ERROR
                        break;
                }
            }

            socket.close();

        } catch (IOException e) {
            System.out.println("Error con el cliente: " + e.getMessage());
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

package py.edu.fpuna.sockets;

import com.google.gson.Gson;
import py.edu.fpuna.dao.ProductoDAO;
import py.edu.fpuna.dto.MensajeClienteStock;
import py.edu.fpuna.dto.MensajeServidor;
import py.edu.fpuna.entities.Producto;
import py.edu.fpuna.enums.TipoDeMensaje;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class DisponibilidadDeStockSocket {

    private static final int PORT = 9000;
    private static final ProductoDAO dao = new ProductoDAO();

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        Gson gson = new Gson();
        byte[] buffer = new byte[1024];

        System.out.println("Servidor UDP de Stock esperando en el puerto " + PORT + "...");

        while (true) {
            try {
                DatagramPacket paqueteEntrada = new DatagramPacket(buffer, buffer.length);
                socket.receive(paqueteEntrada);

                String jsonIn = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength());
                InetAddress direccionCliente = paqueteEntrada.getAddress();
                int puertoCliente = paqueteEntrada.getPort();

                MensajeClienteStock pedido = gson.fromJson(jsonIn, MensajeClienteStock.class);
                int pagina = Math.max(0, pedido.getPagina());

                List<Producto> productos = dao.obtenerDisponibles(pagina);

                MensajeServidor respuesta;
                if (productos.isEmpty()) {
                    respuesta = new MensajeServidor(TipoDeMensaje.ERROR,
                            "No hay más productos en la pagina indicada.",
                            null);
                } else {
                    respuesta = new MensajeServidor(TipoDeMensaje.PRODUCTOS,
                            "Productos disponibles",
                            productos);
                }

                String jsonOut = gson.toJson(respuesta);
                byte[] datosSalida = jsonOut.getBytes();
                DatagramPacket paqueteSalida = new DatagramPacket(
                        datosSalida, datosSalida.length, direccionCliente, puertoCliente);
                socket.send(paqueteSalida);
            } catch (Exception e) {
                System.out.println("Error atendiendo petición de stock: " + e.getMessage());
            }
        }
    }
}
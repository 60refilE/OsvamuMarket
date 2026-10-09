package py.edu.fpuna.sockets;

import com.google.gson.Gson;
import py.edu.fpuna.dao.ProductoDAO;
import py.edu.fpuna.dto.MensajeServidor;
import py.edu.fpuna.dto.MensajeVerificarStock;
import py.edu.fpuna.entities.Producto;
import py.edu.fpuna.enums.TipoDeMensaje;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class VerificacionStockSocket {

    private static final int PORT = 9001;
    private static final ProductoDAO dao = new ProductoDAO();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            Gson gson = new Gson();
            byte[] buffer = new byte[1024];
            System.out.println("[OsvamuMarket] Servidor UDP de Verificación de Stock en puerto " + PORT + ". En espera...");

            while (true) {
                DatagramPacket paqueteEntrada = new DatagramPacket(buffer, buffer.length);
                socket.receive(paqueteEntrada);

                String jsonIn = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength());
                MensajeVerificarStock peticion = gson.fromJson(jsonIn, MensajeVerificarStock.class);
                
                Producto p = dao.obtenerPorId(peticion.getIdProducto());
                MensajeServidor respuesta;
                
                if (p != null) {
                    if (p.getQuantity() > 0) {
                        respuesta = new MensajeServidor(TipoDeMensaje.OK, "Hay stock disponible (" + p.getQuantity() + " unidades en depósito).", null);
                    } else {
                        respuesta = new MensajeServidor(TipoDeMensaje.INFORMACION, "Producto agotado. No hay stock.", null);
                    }
                } else {
                    respuesta = new MensajeServidor(TipoDeMensaje.ERROR, "El producto solicitado no existe.", null);
                }

                byte[] datosSalida = gson.toJson(respuesta).getBytes();
                DatagramPacket paqueteSalida = new DatagramPacket(
                        datosSalida, datosSalida.length, paqueteEntrada.getAddress(), paqueteEntrada.getPort());
                socket.send(paqueteSalida);
            }
        } catch (Exception e) {
            System.err.println("[OsvamuMarket] ERROR en Verificación de Stock: " + e.getMessage());
        }
    }
}

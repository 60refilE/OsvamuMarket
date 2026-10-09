package py.edu.fpuna.sockets;

import com.google.gson.Gson;
import py.edu.fpuna.dao.RastreoDAO;
import py.edu.fpuna.dto.MensajeRastreoLogistico;
import py.edu.fpuna.dto.MensajeServidor;
import py.edu.fpuna.enums.TipoDeMensaje;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RastreoLogisticoSocket {

    private static final int PORT = 9002;
    private static final RastreoDAO dao = new RastreoDAO();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            Gson gson = new Gson();
            byte[] buffer = new byte[1024];
            System.out.println("[OsvamuMarket] Servidor UDP de Rastreo Logístico en puerto " + PORT + ". En espera...");

            while (true) {
                DatagramPacket paqueteEntrada = new DatagramPacket(buffer, buffer.length);
                socket.receive(paqueteEntrada);

                String jsonIn = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength());
                MensajeRastreoLogistico peticion = gson.fromJson(jsonIn, MensajeRastreoLogistico.class);

                MensajeServidor respuesta;

                if (!dao.existeCompra(peticion.getCodigoConfirmacion())) {
                    respuesta = new MensajeServidor(TipoDeMensaje.ERROR, "Código de confirmación no válido. No existe la orden.", null);
                } else {
                    int codigo = peticion.getCodigoConfirmacion();
                    String etapa;
                    String ubicacion;

                    int resto = Math.abs(codigo % 3);
                    if (resto == 1) {
                        etapa = "PREPARACION";
                        ubicacion = "Depósito OsvamuMarket - Asunción";
                        respuesta = new MensajeServidor(TipoDeMensaje.INFORMACION, "Etapa: " + etapa + " | Ubicación: " + ubicacion + " | Actualizado: " + ahora(), null);
                    } else if (resto == 2) {
                        etapa = "EN CAMINO";
                        ubicacion = "Camión en ruta - Av. Eusebio Ayala";
                        respuesta = new MensajeServidor(TipoDeMensaje.INFORMACION, "Etapa: " + etapa + " | Ubicación: " + ubicacion + " | Actualizado: " + ahora(), null);
                    } else {
                        etapa = "ENTREGADO";
                        ubicacion = "Sucursal Super Elian - Recibido";
                        respuesta = new MensajeServidor(TipoDeMensaje.OK, "Etapa: " + etapa + " | Ubicación: " + ubicacion + " | Actualizado: " + ahora(), null);
                    }
                }

                byte[] datosSalida = gson.toJson(respuesta).getBytes();
                DatagramPacket paqueteSalida = new DatagramPacket(
                        datosSalida, datosSalida.length, paqueteEntrada.getAddress(), paqueteEntrada.getPort());
                socket.send(paqueteSalida);
            }
        } catch (Exception e) {
            System.err.println("[OsvamuMarket] ERROR en Rastreo Logístico: " + e.getMessage());
        }
    }

    private static String ahora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

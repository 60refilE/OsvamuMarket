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
            System.out.println("Servidor UDP de Rastreo Logistico esperando en puerto " + PORT + "...");

            while (true) {
                DatagramPacket paqueteEntrada = new DatagramPacket(buffer, buffer.length);
                socket.receive(paqueteEntrada);

                String jsonIn = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength());
                MensajeRastreoLogistico peticion = gson.fromJson(jsonIn, MensajeRastreoLogistico.class);

                MensajeServidor respuesta;

                if (!dao.existeCompra(peticion.getCodigoConfirmacion())) {
                    respuesta = new MensajeServidor(TipoDeMensaje.ERROR, "Codigo de confirmacion no valido. No existe la orden.", null);
                } else {
                    int codigo = peticion.getCodigoConfirmacion();
                    String etapa;
                    String ubicacion;

                    int resto = Math.abs(codigo % 3);
                    if (resto == 1) {
                        etapa = "PREPARACION";
                        ubicacion = "Deposito OsvamuMarket - Asuncion";
                        respuesta = new MensajeServidor(TipoDeMensaje.INFORMACION, "Etapa: " + etapa + " | Ubicacion: " + ubicacion + " | Actualizado: " + ahora(), null);
                    } else if (resto == 2) {
                        etapa = "EN CAMINO";
                        ubicacion = "Camion en ruta - Av. Eusebio Ayala";
                        respuesta = new MensajeServidor(TipoDeMensaje.INFORMACION, "Etapa: " + etapa + " | Ubicacion: " + ubicacion + " | Actualizado: " + ahora(), null);
                    } else {
                        etapa = "ENTREGADO";
                        ubicacion = "Sucursal Super Elian - Recibido";
                        respuesta = new MensajeServidor(TipoDeMensaje.OK, "Etapa: " + etapa + " | Ubicacion: " + ubicacion + " | Actualizado: " + ahora(), null);
                    }
                }

                byte[] datosSalida = gson.toJson(respuesta).getBytes();
                DatagramPacket paqueteSalida = new DatagramPacket(
                        datosSalida, datosSalida.length, paqueteEntrada.getAddress(), paqueteEntrada.getPort());
                socket.send(paqueteSalida);
            }
        } catch (Exception e) {
            System.err.println("Error en Rastreo Logistico: " + e.getMessage());
        }
    }

    private static String ahora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

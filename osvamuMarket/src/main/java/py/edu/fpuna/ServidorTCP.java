package py.edu.fpuna;

import java.io.*;
import java.net.*;

public class ServidorTCP {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1067)) {
            System.out.println("Servidor de Stock TCP esperando en puerto 1067...");
            while (true) {
                try (Socket socket = serverSocket.accept();
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

                    String codigos = entrada.readLine();

                    System.out.println("Consultando stock para: " + codigos);
                   
                    String[] listaCodigos = codigos.split(",");
                    
                    salida.println("DISPONIBILIDAD:");
                    
                    for(String codigo : listaCodigos) {
                        if (codigo.equals("001")) {
                            salida.println(codigo + " -Disponible: 50 cajas");
                        } else if (codigo.equals("002")) {
                            salida.println(codigo + " -Agotado: 0 cajas");
                        } else {
                            salida.println(codigo + " -No encontrado");
                        }
                    }
                    salida.println("FIN_CONSULTA");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package py.edu.fpuna;
import java.util.Scanner;
import java.io.*;
import java.net.*;

public class ClienteTCP {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1067);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Scanner sc = new Scanner(System.in);
            System.out.print("Ingrese los IDs de los productos separados por coma: ");
            String peticion = sc.nextLine();
            salida.println(peticion);

            String lineaRespuesta;
            while ((lineaRespuesta = entrada.readLine()) != null) {
                if (lineaRespuesta.equals("FIN_CONSULTA")) {
                    break;
                }
                System.out.println(lineaRespuesta);
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
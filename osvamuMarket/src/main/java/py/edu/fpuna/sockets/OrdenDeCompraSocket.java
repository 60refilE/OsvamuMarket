package py.edu.fpuna.sockets;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

public class OrdenDeCompraSocket extends Thread {

    private static final int PORT = 8080;

    private Socket socket;

    public OrdenDeCompraSocket(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        Gson gson = new Gson();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // enviar primeros productos

            boolean conectado = true;
            while (conectado) {
                String jsonIn = in.readLine();

                if (jsonIn == null) {

                    conectado = false;
                    continue;
                }
                //logica
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

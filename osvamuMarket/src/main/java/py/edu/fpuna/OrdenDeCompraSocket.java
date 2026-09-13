package py.edu.fpuna;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class OrdenDeCompraSocket extends Thread {

    private static final int PORT = 8080;

    private Socket socket;

    public OrdenDeCompraSocket(Socket socket) {
        this.socket = socket;
    }
    public void run(){}

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado, esperando conexiones...");

        while(true){

        }
    }
}

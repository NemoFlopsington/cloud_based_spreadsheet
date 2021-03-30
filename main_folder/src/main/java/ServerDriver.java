import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDriver {
    ServerSocket serverSocket;

    public static void main() throws IOException{
        ServerSocket serverSocket = new ServerSocket(8666);
        System.out.println("Server started");

        while(true){
            Socket socket = serverSocket.accept();
            ObjectInputStream fromClient = (ObjectInputStream) socket.getInputStream();
            ObjectOutputStream toClient = (ObjectOutputStream) socket.getOutputStream();

            String path = fromClient.readUTF();
        }
    }
}

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerDriver {
    ServerSocket serverSocket;
    private static Connection con;
    private static Statement statement;

    public static void main() throws IOException, SQLException {
        ServerSocket serverSocket = new ServerSocket(8666);
        System.out.println("Server started");
        String connectionUrl = "jdbc:mysql://127.0.0.1";
        con = DriverManager.getConnection(connectionUrl, "root", "password");
        statement = con.createStatement();

        while(true){
            Socket socket = serverSocket.accept();
            ObjectInputStream fromClient = (ObjectInputStream) socket.getInputStream();
            ObjectOutputStream toClient = (ObjectOutputStream) socket.getOutputStream();
            String path = fromClient.readUTF();



        }
    }

    public class getDocument(String path) implements Runnable{
        @Override
        public void run() {
            try {
                statement.executeQuery("Select");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


    }
}

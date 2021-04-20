import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class ServerDriver {
    ServerSocket serverSocket;

    public static void main(String[] args) throws IOException, SQLException {
        ServerSocket serverSocket = new ServerSocket(8666);
        System.out.println("Server started");
        String conString = "jdbc:mysql://g84t6zfpijzwx08q.cbetxkdyhwsb.us-east-1.rds.amazonaws.com:3306";
        Connection con = DriverManager.getConnection(conString, "sill68o7flv9vtch", "pux4n8txd6bdd4j5");
        System.out.println("connected to DB");

        while(true){
            Socket socket = serverSocket.accept( );
            ObjectInputStream fromClient = (ObjectInputStream) socket.getInputStream();
            ObjectOutputStream toClient = (ObjectOutputStream) socket.getOutputStream();

            //operation is an int
            //0 = load file given path
            //1 = save file to path
            int operation = fromClient.readInt();
            Statement statement = con.createStatement();
            if (operation == 0)  {
                //send file given path
                String path = fromClient.readUTF();
                ResultSet rs = statement.executeQuery("SELECT id FROM file_id WHERE path_to_file = '"+path+"';");
                rs.next();
                int fileId = rs.getInt("id");
                rs = statement.executeQuery("SELECT row_column, content, style_flags FROM cells WHERE id = '" + fileId + "';");
                ArrayList<Cell> cells = new ArrayList<>();
                while (rs.next())   {
                    cells.add(new Cell(rs.getString("row_column"), rs.getString("content"), rs.getString("style_flags")));
                }
            }
            else if (operation == 1)    {
                ArrayList<Cell> cells = new ArrayList<>();

            }


        }
    }
}

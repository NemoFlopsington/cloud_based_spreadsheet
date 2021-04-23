import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class ServerDriver {
    //public static Connection con;
    static final String conString = "jdbc:mysql://g84t6zfpijzwx08q.cbetxkdyhwsb.us-east-1.rds.amazonaws.com:3306";
    public static Connection con;
    public static Statement statement;

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(8666);
        System.out.println("Server started");
//TODO: impliment open button
        while(true){

            Socket socket = serverSocket.accept( );
            new Thread(new HandleClient(socket)).start();

        }

    }
    static class HandleClient implements Runnable{
        Socket socket;
        public HandleClient(Socket socket)  {
            this.socket = socket;
        }
        @Override
        public void run() {
            ObjectOutputStream toClient = null;
            ObjectInputStream fromClient;
            try {
                try {
                    con = DriverManager.getConnection(conString, "sill68o7flv9vtch", "pux4n8txd6bdd4j5");
                    statement = con.createStatement();
                    statement.execute("USE j6hlrczca3i3nsqc");
                    System.out.println("connected to DB");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                fromClient = new ObjectInputStream(socket.getInputStream());
                toClient = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("Client connected");

                //operation is an int
                //0 = load file given path
                //1 = save file to path
                Integer operation = (Integer)fromClient.readObject();
                //open function
                if (operation == 0) {
                    String path = (String)fromClient.readObject();
                    ResultSet rs = statement.executeQuery("SELECT id FROM file_id WHERE path_to_file = '" + path + "';");
                    if(rs.next()) {
                        int fileId = rs.getInt("id");
                        rs = statement.executeQuery("SELECT row_column, content, style_flags FROM cells WHERE id = '" + fileId + "';");
                        while (rs.next()) {
                            //cells.add(new Cell(rs.getString("row_column"), rs.getString("content"), rs.getString("style_flags")));
                            toClient.writeObject(new Cell(rs.getString("row_column"), rs.getString("content"), rs.getString("style_flags")));
                        }
                        toClient.writeObject(new Cell("","-1",""));
                        System.out.println("file " + path + " succesfully sent");
                    }
                    else    {
                        //send error cell if path not found
                        toClient.writeObject(new Cell("","-2",""));
                    }

                }
                //save function
                else if (operation == 1) {
                    int fileId;
                    //accept path from client
                    String path = fromClient.readUTF();
                    ResultSet rs = statement.executeQuery("SELECT id FROM file_id WHERE path_to_file = '" + path + "'");
                    //if rs has next, then the path exists we must delete all cells from database first
                    if (rs.next()) {
                        fileId = rs.getInt("id");
                        PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM cells WHERE id = ?");
                        preparedStatement.setInt(1,fileId);
                        preparedStatement.execute();


                        //statement.execute("DELETE FROM cells WHERE id = " + fileId);
                    } else {
                        statement.executeUpdate("INSERT INTO file_id(path_to_file) VALUES ('" + path + "')");
                        rs = statement.executeQuery("SELECT id FROM file_id WHERE path_to_file = '" + path + "'");
                        rs.next();
                        fileId = rs.getInt("id");
                    }
                    do {
                        Cell currentCell = (Cell) fromClient.readObject();
                        if (currentCell.content.equals("-1"))
                            break;
                        statement.executeUpdate("INSERT INTO cells(row_column, content, style_flags, id) VALUES ('" + currentCell.row_column +
                                "', '" + currentCell.content + "', '" + currentCell.style_flags + "', '" + fileId + "')");
                        //cells.add((Cell) fromClient.readObject());
                    } while (true);

                    System.out.println("file"+ path + " succesfully saved");
                    toClient.writeObject(new Integer(1));
                    System.out.println("sucessfully wrote to client status message");
                }

                //write status back to client

                con.close();

            } catch(Exception e)    {
                try {
                    toClient.writeInt(-1);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
    public static void initializeDb()    {
        try {
            con = DriverManager.getConnection(conString, "sill68o7flv9vtch", "pux4n8txd6bdd4j5");
            Statement statement = con.createStatement();
            System.out.println("connected to DB");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}

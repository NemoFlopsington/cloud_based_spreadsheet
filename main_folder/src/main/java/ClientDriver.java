import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientDriver extends Application {
    final int width = 100;
    final int height = 100;
    String currentSelected;
    Cell[][] cells;
    TextField[][] cellsTf;


    @Override
    public void start(Stage stage) throws Exception {
        //menu bar
        Menu fileMenu = new Menu("file");
        MenuItem save = new MenuItem("save");
        save.setOnAction(saveAction());
        MenuItem open = new MenuItem("open");
        cells = new Cell[100][100];
        cellsTf = new TextField[100][100];
        fileMenu.getItems().addAll(save, open);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu);
        menuBar.setStyle("-fx-background-color: green");

        //cells display
        GridPane cellsDisplay = new GridPane();
        TextField tf = new TextField();
        tf.setEditable(false);
        tf.setStyle("-fx-background-color: grey;");
        cellsDisplay.add(tf,0,0);
        for(int i =0; i < width;i++)  {
            tf = new TextField(numToString(i));
            tf.setEditable(false);
            tf.setStyle("-fx-background-color: grey; -fx-border-color: black");
            cellsDisplay.add(tf,i+1,0);
        }
        for(int i = 0; i < height; i++)    {
            tf = new TextField(String.valueOf(i));
            tf.setEditable(false);
            tf.setStyle("-fx-background-color: grey; -fx-border-color: black");
            cellsDisplay.add(tf,0,i+1);
            for(int j = 0; j < width; j++)  {
                cells[i][j] = new Cell(String.valueOf(i) + numToString(j),"", "");
                cellsTf[i][j] = new TextField("");
                cellsTf[i][j].setStyle("-fx-border-color: black");
                cellsTf[i][j].setOnMouseClicked(selected(cells[i][j]));
                cellsTf[i][j].setOnAction(cellChanged(cellsTf[i][j], cells[i][j]));
                cellsDisplay.add(cellsTf[i][j],j+1,i+1);
            }
        }





        GridPane main = new GridPane();
        main.add(menuBar,0,0);
        main.add(new ScrollPane(cellsDisplay),0,1);

        Scene scene = new Scene(main, 1000 , 1000);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Cloud Based spreadsheet");
        primaryStage.setScene(scene);
        primaryStage.show();	//display stage


    }

    private EventHandler<ActionEvent> cellChanged(TextField textField, Cell cell) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                cell.content = textField.getText();
            }
        };
    }

    private EventHandler<? super MouseEvent> selected(Cell cell) {
        //when cell is selected, change the previous cells border to black and change the current cells border to green
        return new EventHandler<>()    {
            @Override
            public void handle(MouseEvent event) {
                if(currentSelected != null) {
                    int[] oldCellRowColumn = row_columnToIndex(currentSelected);
                    cellsTf[oldCellRowColumn[0]][oldCellRowColumn[1]].setStyle("-fx-border-color: #000000; -fx-border-width: 1");
                }
                int[] newCellRowColumn = row_columnToIndex(cell.row_column);
                cellsTf[newCellRowColumn[0]][newCellRowColumn[1]].setStyle("-fx-border-color: green; -fx-border-width: 2");
                currentSelected = numToString(newCellRowColumn[0]) + newCellRowColumn[1];
            }
        };

    }



    private EventHandler<ActionEvent> saveAction() {
        //TODO: impliment save button
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GridPane main = new GridPane();
                Scene saveScene = new Scene(main, 400,300);
                Stage saveStage = new Stage();
                saveStage.setScene(saveScene);
                TextField pathTf = new TextField("Path to file");
                Button cancelBtn = new Button("Cancel");
                Button saveBtn = new Button("Save");
                saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            Socket socket = new Socket("localhost",8666);
                            ObjectOutputStream toServer = (ObjectOutputStream) socket.getOutputStream();
                            ObjectInputStream fromServer = (ObjectInputStream) socket.getInputStream();
                            //write 1 to let server know we are saving a file
                            toServer.write(1);
                            toServer.writeChars(pathTf.getText());
                            for(Cell[] cellRows : cells)  {
                                for(Cell cell: cellRows)    {
                                    if(cell.content != "")toServer.writeObject(cell);
                                }

                            }
                            //send terminator cell
                            toServer.writeObject(new Cell("-1","",""));

                        } catch (IOException e) {
                            main.add(new Label("connection with server unable to be established"),0,10);
                            e.printStackTrace();
                        }
                        pathTf.getText();
                    }
                });
                main.add(pathTf, 0,0);
                main.add(saveBtn, 0,1);
                main.add(cancelBtn, 1, 1);



                saveStage.setTitle("Save");
                saveStage.show();
            }
        };
    }

    private int[] row_columnToIndex(String row_column)  {
        Pattern p = Pattern.compile("[A-Z]+");
        Matcher m = p.matcher(row_column);
        m.find();
        String column = m.group();
        p = Pattern.compile("\\d+");
        m = p.matcher(row_column);
        m.find();
        int row = Integer.parseInt(m.group());
        return new int[]{row, StringToNum(column)};
    }

    private int StringToNum(String in)  {
        //A = 0 Z = 25 AA = 26
        int returnVal = 0;
        for(int i =0; i < in.length(); i++) {
            returnVal += (in.charAt(i) - 64) * Math.pow(26, in.length() - i-1);
        }
        return returnVal-1;
    }
    private String numToString(int in)  {
        in++;
        return numToStringHelper(in);
    }
    private String numToStringHelper(int in)    {
        StringBuilder returnVal = new StringBuilder();
        if(in > 26) {
            returnVal.append(numToStringHelper((in-1) / 26));
            in %= 26;
            if(in == 0)
                in = 26;
        }
        returnVal.append((char)('@' + in));
        return returnVal.toString();
    }
}

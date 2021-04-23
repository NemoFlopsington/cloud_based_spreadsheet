import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientDriver extends Application {
    final int width = 100;
    final int height = 100;
    String currentSelected;
    TextField lastSelected;
    Cell[][] cells;
    TextField[][] cellsTf;
    Scene scene;
    TextField header;
    Stage primaryStage;


    @Override
    public void start(Stage stage) throws Exception {
        //menu bar
        Menu fileMenu = new Menu("file");
        MenuItem save = new MenuItem("save");
        MenuItem saveAs = new MenuItem("save as");
        header = new TextField("Untitled Spreadsheet");
        header.setStyle("-fx-font-size: 16");
        saveAs.setOnAction(saveAsAction());
        //TODO: impliment save function for save
        save.setOnAction(saveAction());
        MenuItem open = new MenuItem("open");
        open.setOnAction(openAction());
        cells = new Cell[100][100];
        cellsTf = new TextField[100][100];
        fileMenu.getItems().addAll(save, saveAs, open);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu);
        menuBar.setStyle("-fx-background-color: green");
        GridPane main = new GridPane();
        scene = new Scene(main, 1000 , 1000);

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
                //cellsTf[i][j].addEventHandler(tab());
                cellsTf[i][j].setOnMouseClicked(selected());
                cellsTf[i][j].setOnKeyPressed(tabbed());
                int finalI = i;
                int finalJ = j;
                cellsTf[i][j].textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        cells[finalI][finalJ].content = newValue;
                    }
                });
                cellsDisplay.add(cellsTf[i][j],j+1,i+1);
            }
        }
        main.add(header,0,0);
        main.add(menuBar,0,1);
        main.add(new ScrollPane(cellsDisplay),0,2);

        primaryStage = new Stage();
        primaryStage.setTitle("Cloud Based spreadsheet");
        primaryStage.setScene(scene);
        primaryStage.show();	//display stage


    }

    private EventHandler<? super KeyEvent> tabbed() {
        return new EventHandler<>() {

            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.TAB)) {
                    if(lastSelected != null)    {
                        lastSelected.setStyle("-fx-border-color: #000000; -fx-border-width: 1");
                    }
                    scene.getFocusOwner().setStyle("-fx-border-color: green; -fx-border-width: 2");
                    lastSelected = (TextField) scene.getFocusOwner();
                    System.out.println("tabPressed");
                }
            }
        };
    }


    private EventHandler<? super MouseEvent> selected() {
        //when cell is selected, change the previous cells border to black and change the current cells border to green
        return new EventHandler<>()    {
            @Override
            public void handle(MouseEvent event) {
                if(lastSelected != null)    {
                    lastSelected.setStyle("-fx-border-color: #000000; -fx-border-width: 1");
                }
                scene.getFocusOwner().setStyle("-fx-border-color: green; -fx-border-width: 2");
                lastSelected = (TextField) scene.getFocusOwner();
            }
        };

    }

    private EventHandler<ActionEvent> openAction() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GridPane main = new GridPane();
                Scene openScene = new Scene(main, 400,300);
                Stage openStage = new Stage();
                TextField pathTf = new TextField("Path to File");
                Button submitBtn = new Button("Open File");
                Label statusLbl = new Label();
                submitBtn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        clearPage();
                        header.setText(pathTf.getText());
                        Socket socket = null;
                        ObjectOutputStream toServer = null;
                        ObjectInputStream fromServer = null;
                        try {
                            socket = new Socket("localhost",8666);
                            toServer = new ObjectOutputStream(socket.getOutputStream());
                            fromServer = new ObjectInputStream(socket.getInputStream());
                            toServer.writeObject(new Integer(0));
                            toServer.writeObject(pathTf.getText());
                            do {
                                Cell currentCell = (Cell)fromServer.readObject();
                                if(currentCell.content.equals("-1"))    //no more content found
                                    break;
                                if(currentCell.content.equals("-2")) {  //path not found
                                    statusLbl.setText("file not found");
                                    break;
                                }
                                int[] cellIndexes = row_columnToIndex(currentCell.row_column);
                                cells[cellIndexes[0]][cellIndexes[1]] = currentCell;
                                cellsTf[cellIndexes[0]][cellIndexes[1]].setText(currentCell.content);
                            } while(true);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                });

                main.add(submitBtn, 0, 1);
                main.add(pathTf,0,0);
                openStage.setScene(openScene);
                openStage.show();
                openStage.setTitle("Open");
            }
        };
    }

    private EventHandler<ActionEvent> saveAction() throws IOException, ClassNotFoundException {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Label statusLbl = new Label();
                    statusLbl.setStyle("-fx-font-size: 16");
                    Socket socket = new Socket("localhost", 8666);
                    ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
                    //write 1 to let server know we are saving a file
                    toServer.writeObject(new Integer(1));
                    toServer.writeUTF(header.getText());
                    for (Cell[] cellRows : cells) {
                        for (Cell cell : cellRows) {
                            if (cell.content != "") toServer.writeObject(cell);
                        }

                    }
                    //send terminator cell
                    toServer.writeObject(new Cell("", "-1", ""));
                    Integer status = (Integer) fromServer.readObject();
                    if (status == -1) {
                        statusLbl.setText("There was an error uploading your file");
                    } else if (status == 1) {
                        statusLbl.setText("file uploaded succesfully");
                    }
                    //status popup
                    GridPane statusPopupGridPane = new GridPane();
                    statusPopupGridPane.add(statusLbl,0,0);
                    Scene statusPopupScene = new Scene(statusPopupGridPane);
                    Stage statusPopupStage = new Stage();
                    statusPopupStage.setScene(statusPopupScene);
                    statusPopupStage.show();
                } catch (Exception e)    {
                    e.printStackTrace();
                }

            }
        };

    }

    private EventHandler<ActionEvent> saveAsAction() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                GridPane main = new GridPane();
                Scene saveScene = new Scene(main, 400,300);
                Stage saveStage = new Stage();
                saveStage.setScene(saveScene);
                Label statusLbl = new Label();
                TextField pathTf = new TextField("Path to file");
                Button cancelBtn = new Button("Cancel");
                Button saveBtn = new Button("Save");
                saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        header.setText(pathTf.getText());
                        try {
                            Socket socket = new Socket("localhost",8666);
                            ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                            ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
                            //write 1 to let server know we are saving a file
                            toServer.writeObject(new Integer(1));
                            toServer.writeUTF(pathTf.getText());
                            for(Cell[] cellRows : cells)  {
                                for(Cell cell: cellRows)    {
                                    if(cell.content != "")toServer.writeObject(cell);
                                }

                            }
                            //send terminator cell
                            toServer.writeObject(new Cell("","-1",""));
                            Integer status = (Integer) fromServer.readObject();
                            if(status == -1)    {
                                statusLbl.setText("There was an error uploading your file");
                            }
                            else if(status == 1)    {
                                statusLbl.setText("file uploaded succesfully");
                            }

                        } catch (IOException | ClassNotFoundException e) {
                            statusLbl.setText("error connecting to server");
                            e.printStackTrace();
                        }
                        pathTf.getText();
                    }
                });
                main.add(pathTf, 0,0);
                main.add(saveBtn, 0,1);
                main.add(cancelBtn, 1, 1);
                main.add(statusLbl,0, 10);



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
    private void clearPage()    {
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++)  {
                cells[i][j].content = "";
                cellsTf[i][j].setText("");
            }
        }
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

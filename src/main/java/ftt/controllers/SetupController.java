package ftt.controllers;

import ftt.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.NumberStringConverter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SetupController implements Initializable, ActionListener {

    // FXML -----
    @FXML
    TableView<LineItem> tableView;
    @FXML
    TableColumn<LineItem,String> osColumn;
    @FXML
    TableColumn<LineItem,LineItem> pColumn;
    @FXML
    TableColumn<LineItem,Double> lColumn;
    @FXML
    TableColumn<LineItem,LineItem> lineColumn;
//    @FXML
//    TableColumn<LineItem,Color> colorColumn;
    @FXML
    TextField filename;
    @FXML
    TextArea description;
    @FXML
    ComboBox<String> writeType;
    //  -----

    // LineItem work -----
    LineItem currentLineItem;
    LineItem xLine   = new LineItem(LineItem.X,"X", this);
    LineItem addLine = new LineItem(LineItem.ADD,"",this);
    //  -----

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof LineItem) {
            currentLineItem = (LineItem) e.getSource();
            switch (e.getActionCommand()) {
                case LineCell.SET_COMAND:
                    mainController.startSelect(
                        currentLineItem.getType() == LineItem.ADD ? MainController.MULTI : MainController.SINGLE
                    );
                    break;
                case LineCell.DEL_COMAND:
                    tableView.getItems().remove(currentLineItem);
                    currentLineItem = null;
                    break;
            }
        }
    }


    MainController mainController;

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setSelected(List<Line> selected) {
        if(currentLineItem != null) {
            if(currentLineItem.getType() == LineItem.ADD) {
                tableView.getItems().remove(addLine);
                for (Line l : selected) {
                    LineItem li = new LineItem(LineItem.ITEM,"Y",this);
                    li.setLine(l);
                    tableView.getItems().add(li);
                }
                tableView.getItems().add(addLine);
            } else {
                currentLineItem.setLine(selected.get(0));
                int indexUp = tableView.getItems().indexOf(currentLineItem);
                tableView.getItems().set(indexUp,null);
                tableView.getItems().set(indexUp,currentLineItem);
            }
        }
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        writeType.getItems().add("после изменения одного");
        writeType.getItems().add("после изменения всех");
        writeType.getSelectionModel().selectFirst();



        osColumn.setCellValueFactory(param -> param.getValue().osProperty());
//        colorColumn.setCellValueFactory(param -> param.getValue().colorProperty());
        lColumn.setCellValueFactory(param -> param.getValue().limitProperty());
        lineColumn.setCellValueFactory(param -> new SimpleObjectProperty<LineItem>(param.getValue()));
        lineColumn.setCellFactory(new Callback<TableColumn<LineItem, LineItem>, TableCell<LineItem, LineItem>>() {
            @Override
            public TableCell<LineItem, LineItem> call(TableColumn<LineItem, LineItem> p) {
                return new LineCell();
            }
        });
        pColumn.setCellValueFactory(param -> new SimpleObjectProperty<LineItem>(param.getValue()));
        pColumn.setCellFactory(new Callback<TableColumn<LineItem, LineItem>, TableCell<LineItem, LineItem>>() {
            @Override
            public TableCell<LineItem, LineItem> call(TableColumn<LineItem, LineItem> p) {
                return new FTTCheckBoxTableCell(FTTCheckBoxTableCell.WRITE);
            }
        });
        lColumn.setCellFactory(new Callback<TableColumn<LineItem, Double>, TableCell<LineItem, Double>>() {
            @Override
            public TableCell<LineItem, Double> call(TableColumn<LineItem, Double> p) {
                return new TextFieldTableCell<LineItem, Double>(new DoubleStringConverter()) {
                    @Override
                    public void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setGraphic(null);
                        }
                    }
                };
            }
        });
//        colorColumn.setCellFactory(new Callback<TableColumn<LineItem, Color>, TableCell<LineItem, Color>>() {
//            @Override
//            public TableCell<LineItem, Color> call(TableColumn<LineItem, Color> p) {
//                return new ColorTableCell<LineItem>(colorColumn);
//            }
//        });

        pColumn.setEditable(true);
        lColumn.setEditable(true);

//        addLine.setColor(null);
//        xLine.setColor(null);
        addLine.setWrite(null);
        addLine.setLimit(null);
        xLine.setWrite(new Boolean(true));

        tableView.getItems().add(xLine);
        tableView.getItems().add(addLine);

        tableView.setEditable(true);
    }

    public List<LineItem> getListLineItem(boolean withX) {
        return tableView.getItems().subList(withX?0:1,tableView.getItems().size()-1);
    }

    public void updateTable() {
        for(LineItem li: mainController.getRecController().getItemList()) {
            tableView.getItems().set(tableView.getItems().indexOf(li),li);
        }
    }

    public int getWriteType() {
        return writeType.getSelectionModel().getSelectedIndex();
    }

    public String getFileName() {
        return filename.getText();
    }

    public String getDesc() {
        return description.getText();
    }
}

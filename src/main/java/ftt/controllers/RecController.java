package ftt.controllers;

import ftt.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.*;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class RecController implements Initializable {

    @FXML
    ToggleButton play;
    @FXML
    ToggleButton pause;
    @FXML
    TableView<LineItem> tableView;
    @FXML
    Button save;

    @FXML
    TableColumn<LineItem, LineItem> visColumn;
    @FXML
    TableColumn<LineItem, String> namColumn;
    @FXML
    TableColumn<LineItem,Double> valueColumn;

    @FXML
    BorderPane pane;

    @FXML
    javafx.scene.control.Label info;

    File workDir = new File("result");

    boolean bPlay = false;

    MainController mainController;
    Agregator agregator;

    List<LineItem> list;

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.getConnector().addDataReceiveListener(agregator);
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        agregator = new Agregator(pane,this);
        ToggleGroup group = new ToggleGroup();
        play.setToggleGroup(group);
        pause.setToggleGroup(group);
        save.setDisable(true);

        namColumn.setCellValueFactory(new PropertyValueFactory<LineItem, String>("line"));
        valueColumn.setCellValueFactory(param -> param.getValue().lastYProperty());
        visColumn.setCellValueFactory(param -> new SimpleObjectProperty<LineItem>(param.getValue()));
        visColumn.setCellFactory(new Callback<TableColumn<LineItem, LineItem>, TableCell<LineItem, LineItem>>() {
            @Override
            public TableCell<LineItem, LineItem> call(TableColumn<LineItem, LineItem> p) {
                return new FTTCheckBoxTableCell(FTTCheckBoxTableCell.VISIBLE);
            }
        });
        info.setText("");
//        colorColumn.setCellValueFactory(param -> param.getValue().colorProperty());
//        colorColumn.setCellFactory(new Callback<TableColumn<LineItem, Color>, TableCell<LineItem, Color>>() {
//            @Override
//            public TableCell<LineItem, Color> call(TableColumn<LineItem, Color> p) {
//                return new ColorTableCell<LineItem>(colorColumn);
//            }
//        });
        tableView.setEditable(true);
        if(!workDir.isDirectory()) workDir.mkdirs();
    }

    public void play() {
        agregator.setPause(false);
        if(!bPlay) {
            list = mainController.getSetupController().getListLineItem(true);
            HashMap<Client, ArrayList<String>> propMap = new HashMap<>();
            for (LineItem li : list) {
                if (propMap.containsKey(li.getLine().getClient())) {
                    propMap.get(li.getLine().getClient()).add(li.getLine().getProp());
                } else {
                    ArrayList<String> ll = new ArrayList<>();
                    ll.add(li.getLine().getProp());
                    propMap.put(li.getLine().getClient(), ll);
                }
            }
            for (Client c : propMap.keySet()) {
                mainController.getConnector().setup(c, propMap.get(c));
            }
            agregator.reset();
            agregator.setSyncType(mainController.getSetupController().getWriteType());
            agregator.setX(list.get(0));
            agregator.addY(list.subList(1, list.size()));
            bPlay = true;
        }
        save.setDisable(!agregator.getPause());
    }

    public void pause() {
        if(bPlay) {
            agregator.setPause(true);
        }
        save.setDisable(!agregator.getPause());
    }

    public void stop() {
        if(bPlay) {
            bPlay = false;
            play.setSelected(false);
            pause.setSelected(false);
            mainController.close();
            save.setDisable(false);
        }
    }

    public void initData() {
        tableView.getItems().removeAll(tableView.getItems());
        tableView.getItems().addAll(mainController.getSetupController().getListLineItem(false));
    }

    public List<LineItem> getItemList() {
        return tableView.getItems();
    }

    public void save() {
        WritableWorkbook workbook = null;
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        try {
            if(list == null) return;
            Date data = new Date();
            String fn = getMainController().getSetupController().getFileName();
            File out = new File(workDir,fn+(fn.isEmpty()?"":" ")+df.format(data) + ".xls");
            workbook = Workbook.createWorkbook(out);
//            WritableSheet sheet = workbook.createSheet(fn.isEmpty()?"Регистрация данных":fn, 0);
            WritableSheet sheet = workbook.createSheet("Самописец", 0);
            ArrayList<Client> cList = new ArrayList<>();
            for(LineItem item: list) {
                if(!cList.contains(item.getLine().getClient())) {
                    cList.add(item.getLine().getClient());
                }
            }

            int row = 1;

            sheet.addCell( new jxl.write.Label(1,row++,"Данные с приборов:"));
            for(Client c: cList) {
                String lines = "";
                for(LineItem item: list) {
                    if(item.getLine().getClient().equals(c)) {
                        lines += " "+item.getLine().getProp()+";";
                    }
                }
                sheet.addCell( new jxl.write.Label(1,row++,c.getName()+" ("+lines+")"));
            }
            row++;
            if(!getMainController().getSetupController().getDesc().isEmpty()) {
                sheet.addCell( new jxl.write.Label(1,row++,"Описание:"));
                StringTokenizer tokenizer = new StringTokenizer(getMainController().getSetupController().getDesc(),"\r\n");
                while(tokenizer.hasMoreTokens()) {
                    sheet.addCell( new jxl.write.Label(1,row++,tokenizer.nextToken()));
                }
                row++;
            }
            int col = 0;
            ArrayList<XYChart.Series> sl = new ArrayList<>();
            for(LineItem lineItem: list) {
                sheet.addCell( new jxl.write.Label(col,row,lineItem.getLine().getProp()));
                sl.add(lineItem.getSeries());
                col++;
            }
            col = 0;
            row++;
            for(XYChart.Series s : sl.subList(1,sl.size())) {
                int curRow = row;
                for(Object d: s.getData()){
                    XYChart.Data vd = (XYChart.Data)d;
                    if(col == 0) {
                        sheet.addCell(new jxl.write.Number(col,curRow,(Double)vd.getXValue()));
                        sheet.addCell(new jxl.write.Number(col+1,curRow++,(Double)vd.getYValue()));
                    } else {
                        sheet.addCell(new jxl.write.Number(col,curRow++,(Double)vd.getYValue()));
                    }
                }
                if(col == 0) col++;
                col++;
            }

            workbook.write();
            workbook.close();

            Desktop dt = Desktop.getDesktop();
            dt.open(out);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    public void updateXInfo(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                info.setText(text);
            }
        });
    }

}

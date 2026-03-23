package ftt.controllers;

import ftt.Client;
import ftt.FTTConnector;
import ftt.Line;
import ftt.LineItem;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

public class TreeViewController implements Initializable, ListChangeListener<Client> {

    @FXML
    TreeView treeView;
    @FXML
    Button select;

    FTTConnector connector;

    MainController mainController;

    TreeItem rootItem;

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private final Node rootIcon = new ImageView(
            new Image(getClass().getResourceAsStream("/images/all-24.png"))
    );

    private Node createDevIcon() {
        return new ImageView(new Image(getClass().getResourceAsStream("/images/dev-24.png")));
    }

    private final Node createLineIcon() {
        return new ImageView(new Image(getClass().getResourceAsStream("/images/line-24.png")));
    }

    public void setConnector(FTTConnector connector) {
        this.connector = connector;
        connector.getDetective().getClients().addListener(this);
        connector.find();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootItem = new TreeItem ("Виртуальные устройства", rootIcon);
        treeView.setRoot(rootItem);
    }

    @FXML
    public void update() {
        System.out.println("update");
        rootItem.getChildren().clear();
        connector.find();
    }

    @FXML
    public void select() {
        TreeItem sItem = (TreeItem) treeView.getSelectionModel().getSelectedItems().get(0);
        if(sItem == null) return;
        Object selectedItem = sItem.getValue();
        if(selectedItem != null && !sItem.equals(rootItem)) {
            ArrayList<Line> list = new ArrayList<>();
            if(selectedItem instanceof Client ) {

                for(String l: ((Client) selectedItem).getProperties().keySet()) {
                    list.add( new Line((Client)selectedItem,l));
                }
            } else {
                Client parent  = (Client)(sItem.getParent().getValue());
                list.add(new Line(parent, selectedItem.toString()));
            }
            if(list.size() > 1 && mainController.getMode() == mainController.SINGLE || list.size() == 0) return;
            list.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));
            mainController.endSelect(list);
        }
    }


    @Override
    public void onChanged(Change<? extends Client> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    //permutate
                }
            } else if (c.wasUpdated()) {
                //update item
            } else {
                for (Client remitem : c.getRemoved()) {

                }
                for (Client additem : c.getAddedSubList()) {
                    TreeItem  dev = new TreeItem (additem, createDevIcon());

                    for(String name: additem.getProperties().keySet()) {
                        dev.getChildren().add(new TreeItem<String>(name,createLineIcon()));
                    }
                    dev.getChildren().sort(Comparator.comparing(Object::toString));
                    rootItem.getChildren().add(dev);
                }
            }
        }
    }

    public void setSelect(boolean s) {
        select.setDisable(!s);
    }
}

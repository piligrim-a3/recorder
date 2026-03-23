package ftt.controllers;

import ftt.FTTConnector;
import ftt.Line;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    FTTConnector connector;

    TreeViewController treeViewController;
    SetupController setupController;
    RecController recController;

    public static final int SINGLE = 0;
    public static final int MULTI  = 1;

    public int mode;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    // FXML -----
    @FXML
    TabPane tabPane;
    @FXML
    Tab gtab;
    @FXML
    Tab stab;
    @FXML
    Tab ctab;
    //  -----


    public FTTConnector getConnector() {
        return connector;
    }

    public void setConnector(FTTConnector connector) {
        this.connector = connector;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        connector = new FTTConnector("V1",FTTConnector.CLIENT);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/detective.fxml"));
        try {
            Node detective = fxmlLoader.load();
            ctab.setContent(detective);
            treeViewController = fxmlLoader.getController();
            treeViewController.setConnector(connector);
            treeViewController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/setup.fxml"));
        try {
            Node setup = fxmlLoader.load();
            stab.setContent(setup);
            setupController = fxmlLoader.getController();
            setupController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/rec.fxml"));
        try {
            Node rec = fxmlLoader.load();
            gtab.setContent(rec);
            recController = fxmlLoader.getController();
            recController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if(oldValue.equals(new Integer(2))) {
                        treeViewController.setSelect(false);
                    } else if(oldValue.equals(new Integer(1)) && newValue.equals(new Integer(0))) {
                        recController.initData();
                    } else if(oldValue.equals(new Integer(0)) && newValue.equals(new Integer(1))) {
                        setupController.updateTable();
                    }
                }
        );
    }

    public void startSelect(int mode) {
        this.mode = mode;
        tabPane.getSelectionModel().select(ctab);
        treeViewController.setSelect(true);
    }

    public void endSelect(java.util.List<Line> list) {
        tabPane.getSelectionModel().select(stab);
        setupController.setSelected(list);
    }

    public TreeViewController getTreeViewController() {
        return treeViewController;
    }

    public void setTreeViewController(TreeViewController treeViewController) {
        this.treeViewController = treeViewController;
    }

    public SetupController getSetupController() {
        return setupController;
    }

    public void setSetupController(SetupController setupController) {
        this.setupController = setupController;
    }

    public RecController getRecController() {
        return recController;
    }

    public void setRecController(RecController recController) {
        this.recController = recController;
    }

    public void close() {
        connector.close();
    }
}

package ftt;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

public class LineCell  extends TableCell<LineItem, LineItem> {

    BorderPane box = new BorderPane();

    Button set =  new Button("⇒");
    HBox hBox = new HBox();
    Button del =  new Button("☓");
    Label info = new Label();

    public static final String SET_COMAND  = "SET";
    public static final String DEL_COMAND  = "DEL";

    LineItem lineItem;

    public LineCell() {
        hBox.getChildren().add(set);
        hBox.getChildren().add(del);
        box.setRight(hBox);
        box.setLeft(info);
        info.setTextAlignment(TextAlignment.LEFT);
        info.setAlignment(Pos.CENTER_LEFT);
        set.getStyleClass().add("setButton");
        set.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (lineItem != null) {
                    lineItem.runAction(SET_COMAND);
                }
            }
        });
        del.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if(lineItem != null) {
                    lineItem.runAction(DEL_COMAND);
                }
            }
        });
    }

    @Override
    protected void updateItem(LineItem item, boolean empty) {
        super.updateItem(item, empty);
        if(!empty) {
            lineItem = item;
            if(item.getType() == LineItem.ADD) {
                info.setText("Добавить позиции...");
                set.setText("+");
                del.setVisible(false);
            } else {
                set.setText("⇒");
                del.setVisible(item.getType() == LineItem.ITEM);
                if(item.getLine() != null) {
                    info.setText(item.getLine().getProp() + " → " + String.valueOf(item.getLine().getClient()));
                } else {
                    info.setText("");
                }
            }
            setGraphic(box);
        } else {
            setGraphic(null);
        }

    }
}
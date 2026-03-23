package ftt;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;


public class FTTCheckBoxTableCell extends TableCell<LineItem, LineItem>  {

        CheckBox checkBox = new CheckBox();

        LineItem lineItem;

        public static final int WRITE = 0;
        public static final int VISIBLE = 1;
        public static final int RECORD = 2;


        int type = 0;


        public FTTCheckBoxTableCell(int type) {
            this.type = type;
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if(lineItem != null) {
                        switch (type) {
                            case WRITE: lineItem.setWrite(newValue); break;
                            case VISIBLE: lineItem.setVisible(newValue); break;
                            case RECORD: lineItem.setRecord(newValue); break;
                        }
                    }
                }
            });
        }

        @Override
        protected void updateItem(LineItem item, boolean empty) {
        super.updateItem(item, empty);
        if(!empty && item.getWrite() != null) {
            lineItem = item;
            switch (type) {
                case WRITE: checkBox.setSelected(item.getWrite()); break;
                case VISIBLE: checkBox.setSelected(item.getVisible()); break;
                case RECORD: checkBox.setSelected(item.getRecord()); break;
            }

            setGraphic(checkBox);
        } else {
            setGraphic(null);
        }

    }
    }
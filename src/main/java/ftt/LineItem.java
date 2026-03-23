package ftt;


import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.XYChart;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class LineItem implements ChangeListener<Boolean> {


    public static final int ADD  = 0;
    public static final int ITEM = 1;
    public static final int X    = 2;

    ActionListener listener;
    Agregator agregator;

    double lastX = 0;
    SimpleObjectProperty<Double> lastY = new SimpleObjectProperty<>(0d);
    double writeY = Double.MIN_VALUE;

    int type = ADD;

    // Property
    SimpleStringProperty os = new SimpleStringProperty("Y");
    SimpleObjectProperty<Line> line = new SimpleObjectProperty<>();
//    SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(
//            Color.valueOf(randomColors[new Random().nextInt(randomColors.length)])
//    );
    SimpleObjectProperty<Double> limit = new SimpleObjectProperty<>(0d);
    SimpleObjectProperty<Boolean> write = new SimpleObjectProperty(new Boolean(false));
    SimpleObjectProperty<Boolean> visible = new SimpleObjectProperty(new Boolean(true));
    SimpleObjectProperty<Boolean> record = new SimpleObjectProperty(new Boolean(true));

    XYChart.Series series = new XYChart.Series();

    public LineItem() {}

    public LineItem(int type, String os, ActionListener listener) {
        this.type = type;
        this.listener = listener;
        setOs(os);
        visible.addListener(this);
    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    public String getOs() {
        return os.get();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SimpleStringProperty osProperty() {
        return os;
    }

    public void setOs(String os) {
        this.os.set(os);
    }

    public Line getLine() {
        return line.get();
    }

    public SimpleObjectProperty<Line> lineProperty() {
        return line;
    }

    public void setLine(Line line) {
        this.line.set(line);
    }

//    public Color getColor() {
//        return color.get();
//    }
//
//    public SimpleObjectProperty<Color> colorProperty() {
//        return color;
//    }
//
//    public void setColor(Color color) {
//        this.color.set(color);
//    }

    public double getLimit() {
        return limit.get();
    }

    public SimpleObjectProperty<Double> limitProperty() {
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit.set(limit);
    }

    public Boolean getWrite() {
        return write.get();
    }

    public SimpleObjectProperty<Boolean> writeProperty() {
        return write;
    }

    public void setWrite(Boolean write) {
        this.write.set(write);
    }

    public Boolean getVisible() {
        return visible.get();
    }

    public SimpleObjectProperty<Boolean> visibleProperty() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible.set(visible);
    }

    public Boolean getRecord() {
        return record.get();
    }

    public SimpleObjectProperty<Boolean> recordProperty() {
        return record;
    }

    public void setRecord(Boolean record) {
        this.record.set(record);
    }

    public XYChart.Series getSeries() {
        return series;
    }

    public void runAction(String comand) {
        if(listener != null) {
            listener.actionPerformed(new ActionEvent(this,0,comand));
        }
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if(agregator != null) {
//            System.out.println("setVisible:  "+newValue);
            if(newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        agregator.getLineChart().getData().add(series);
                    }
                });
//                agregator.styleSeries(this);
            }

            else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        agregator.getLineChart().getData().remove(series);
                    }
                });
            }
        }
    }


    public void setAgregator(Agregator agregator) {
        this.agregator = agregator;
    }

    public double getLastX() {
        return lastX;
    }

    public void setLastX(double lastX) {
        this.lastX = lastX;
    }

    public Double getLastY() {
        return lastY.get();
    }

    public SimpleObjectProperty<Double> lastYProperty() {
        return lastY;
    }

    public void setLastY(Double lastY) {
        this.lastY.set(lastY);
    }

    public double getWriteY() {
        return writeY;
    }

    public void setWriteY(double writeY) {
        this.writeY = writeY;
    }
}

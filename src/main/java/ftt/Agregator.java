package ftt;

import ftt.controllers.RecController;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Agregator implements DataReceiveListener {

    Sinhronizer sinhronizer = new Sinhronizer();

    BorderPane pane;
    LineChart lineChart;
    double curentX = Double.MIN_VALUE;
    double writeX = Double.MIN_VALUE;
    boolean run = false;

    boolean pause = false;

    double startLower;

    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();

    LineItem x;
    ArrayList<LineItem> y = new ArrayList<>();

    Double minX = Double.MAX_VALUE;
    Double maxX = -Double.MAX_VALUE;

    RecController controller;

    final StackPane chartContainer = new StackPane();
    final Rectangle zoomRect = new Rectangle();

    public Agregator(BorderPane pane, RecController controller) {
        this.pane = pane;
        this.controller = controller;
        lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setAnimated(false);

        zoomRect.setManaged(false);
        zoomRect.setFill(Color.LIGHTSEAGREEN.deriveColor(0, 1, 1, 0.5));
        chartContainer.getChildren().add(lineChart);
        chartContainer.getChildren().add(zoomRect);

        setUpZooming(zoomRect, lineChart);

        pane.setCenter(chartContainer);
//        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);
    }

    public LineChart getLineChart() {
        return lineChart;
    }

    public LineItem getX() {
        return x;
    }

    public void setX(LineItem x) {
        this.x = x;
    }

    public ArrayList<LineItem> getY() {
        return y;
    }

    public void addY(List<LineItem> y) {
        ArrayList<LineItem> sList = new ArrayList<>();
        this.y.addAll(y);
        String style = "";
        for(LineItem li: y) {
            sList.add(li);
            li.getSeries().setName(li.getLine().getProp());
            li.setAgregator(this);
            if(li.getVisible()) {
                lineChart.getData().add(li.getSeries());
            }
        }
        sList.add(x);
        sinhronizer.init(sList);
    }

    public void setSyncType(int type) {
        sinhronizer.setType(type);
    }


    public void reset() {
        for(LineItem li: y) {
            lineChart.getData().remove(li.getSeries());
            li.getSeries().getData().removeAll(li.getSeries().getData());
            li.setAgregator(null);
        }
        minX = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        y.removeAll(y);
        run = false;
    }

    @Override
    public void receiveData(Client client, Map<String, Object> map) {
        if(pause) return;
        if(client.equals(x.getLine().getClient()) && map.keySet().contains(x.getLine().getProp())) {
            curentX = ((Double) map.get(x.getLine().getProp())).doubleValue();
            controller.updateXInfo(String.format(x.getLine().getProp() + " = %.4g", curentX));
            if(Math.abs(writeX-curentX) > x.getLimit()) {
                sync(x);
            }
        }
        for(LineItem li: y) {
            if(client.equals(li.getLine().getClient()) && map.keySet().contains(li.getLine().getProp())) {
                double ly = ((Double)map.get(li.getLine().getProp())).doubleValue();
                li.setLastY(ly);
                if(Math.abs(ly-li.getWriteY()) > li.getLimit()) {
                    sync(li);
                }
            }
        }
    }

    private void sync(LineItem item) {
        if(sinhronizer.sync(item)) {
            writeX = curentX;
            if(minX > curentX) minX = curentX;
            if(maxX < curentX) maxX = curentX;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
//                    int lb = (int)(minX - Math.abs((maxX-minX))/20);
//                    int ub = (int)(maxX + Math.abs((maxX-minX))/20);
//                    xAxis.setUpperBound(ub);
//                    xAxis.setLowerBound(lb);
//                    xAxis.setTickUnit((int)((Math.abs(ub - lb) / 15)));
                    for(LineItem li: y) {
                        li.setWriteY(li.getLastY());
                        XYChart.Data data = new XYChart.Data(curentX, li.getLastY());
                        data.setNode(new HoveredThresholdNode(data, li));
                        li.getSeries().getData().add(data);
                    }
                }
            });

        }
    }

    public boolean getPause() {
        return pause;
    }

    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(XYChart.Data data,LineItem lit) {
            setPrefSize(10, 10);

            final Label label = createDataThresholdLabel((Double)data.getYValue(),lit);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent mouseEvent) {
                    int index = y.indexOf(lit);
                    label.getStyleClass().removeAll(label.getStyleClass());
                    label.getStyleClass().addAll( "chart-line-symbol", "chart-series-line");
                    lit.getSeries().getNode().getStyleClass().stream()
                            .filter(s -> s.startsWith("default-color"))
                            .forEach(s1 -> label.getStyleClass().add(s1));
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                }
            });
            setOnMousePressed(event -> {
                if(event.getButton().equals(MouseButton.MIDDLE)) {
                    delete(lit.getSeries().getData().indexOf(data));
                }
            });
        }

        private Label createDataThresholdLabel(double value,LineItem lineItem) {
            final Label label = new Label(String.format("%.4g",value) + "");
            int index = 0;
            for(LineItem li: y) {
                if(li.equals(lineItem)) break;
                else if(li.getVisible()) index++;
            }
            label.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }

    public void delete(int index) {
        for(LineItem li: y) {
            li.getSeries().getData().remove(index);
        }
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    private void setUpZooming(final Rectangle r, final Node zoomingNode) {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        AtomicBoolean zoom = new AtomicBoolean(false);
        zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton().equals(MouseButton.PRIMARY)) {
                    zoom.set(true);
                } else if(event.getButton().equals(MouseButton.SECONDARY)){
                    xAxis.setAutoRanging(true);
                    yAxis.setAutoRanging(true);
                }
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                r.setWidth(0);
                r.setHeight(0);
            }
        });
        zoomingNode.setOnMouseDragged(event -> {
            if (zoom.get()) {
                double x = event.getX();
                double y = event.getY();
                r.setX(Math.min(x, mouseAnchor.get().getX()));
                r.setY(Math.min(y, mouseAnchor.get().getY()));
                r.setWidth(Math.abs(x - mouseAnchor.get().getX()));
                r.setHeight(Math.abs(y - mouseAnchor.get().getY()));
            }

        });
        zoomingNode.setOnMouseReleased(event -> {
            if (zoom.get()) {
                xAxis.setAutoRanging(false);
                yAxis.setAutoRanging(false);
                doZoom();
                zoom.set(false);
            }
        });
    }

    private void doZoom() {
        Point2D zoomTopLeft = new Point2D(zoomRect.getX(), zoomRect.getY());
        Point2D zoomBottomRight = new Point2D(zoomRect.getX() + zoomRect.getWidth(), zoomRect.getY() + zoomRect.getHeight());
       // final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToParent(0, 0);
      //  final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToParent(0, 0);
        double xOffset = zoomTopLeft.getX() - xAxisInScene.getX() ;
        double yOffset = yAxisInScene.getY()+yAxis.getHeight() - zoomBottomRight.getY();
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();

        xAxis.setLowerBound(xAxis.getLowerBound() + xOffset / xAxisScale);
        xAxis.setUpperBound(xAxis.getLowerBound() + zoomRect.getWidth() / xAxisScale);

        System.out.println(yAxis.getLowerBound() + " " + yAxis.getUpperBound());

        yAxis.setLowerBound(yAxis.getLowerBound() - yOffset / yAxisScale);
        yAxis.setUpperBound(yAxis.getLowerBound() - zoomRect.getHeight() / yAxisScale);

        System.out.println(yAxis.getLowerBound() + " " + yAxis.getUpperBound());
        zoomRect.setWidth(0);
        zoomRect.setHeight(0);
    }
}

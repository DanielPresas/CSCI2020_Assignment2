package com.danielpresas_100699431;

import java.io.*;
import java.nio.charset.*;
import java.util.Arrays;

import org.apache.commons.csv.*;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class JavaFXApp extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("CSCI2020U Assignment 2 - Daniel Presas - 100699431");

        var vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20.0);

        var scrollPane = new ScrollPane();
        scrollPane.setPrefSize(1600, 1800);
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 1600.0);
        AnchorPane.setRightAnchor(scrollPane, 900.0);

        var xAxis = new NumberAxis(0, 16, 2);
        xAxis.setMinorTickVisible(false);
        xAxis.setLabel("Fatal Incidents");

        var yAxis = new CategoryAxis();
        yAxis.setPrefWidth(200);
        yAxis.setLabel("Airline");
        yAxis.setTickLabelGap(3);
        yAxis.setTickLength(10);

        var barChart = new BarChart<Number, String>(xAxis, yAxis);
        barChart.setBarGap(0);
        barChart.setCategoryGap(5);
        barChart.setPrefSize(1500, 1800);
        barChart.setLegendSide(Side.RIGHT);
        barChart.setTitle("Fatal Incidents per Airline");
        barChart.setTitleSide(Side.TOP);

        var series85_99 = new XYChart.Series<Number, String>();
        var series00_14 = new XYChart.Series<Number, String>();
        series85_99.setName("1985-1999");
        series00_14.setName("2000-2014");

        var csvPath = App.class.getResource("airline_safety.csv");
        try {
            var csvFormatBuilder = CSVFormat.DEFAULT.builder().setHeader();
            var csvParser = CSVParser.parse(csvPath, Charset.defaultCharset(), csvFormatBuilder.build());
            var records = csvParser.getRecords();

            for (int i = records.size() - 1; i >= 0; --i) {
                var r = records.get(i);

                var airline = r.get(0);             // Column 0 = Airline
                var accidents85_99 = r.get(3);      // Column 3 = Fatal accidents 1985-99
                var accidents00_14 = r.get(6);      // Column 6 = Fatal accidents 2000-14

                series85_99.getData().add(new XYChart.Data<Number, String>(Integer.parseInt(accidents85_99), airline));
                series00_14.getData().add(new XYChart.Data<Number, String>(Integer.parseInt(accidents00_14), airline));
            }

            barChart.getData().addAll(Arrays.asList(series85_99, series00_14));
            scrollPane.setContent(barChart);
            vbox.getChildren().add(scrollPane);
        }
        catch(Exception e) {
            System.out.println("-----\n" + e.toString() + "\n-----");
            e.printStackTrace();
        }

        scene = new Scene(vbox, 1600, 900);
        stage.setScene(scene);
        stage.show();
    }

    public static void main() {
        launch();
    }
}

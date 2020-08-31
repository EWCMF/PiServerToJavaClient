
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class Client {
    @FXML
    private AnchorPane root;

    private final TimeSeriesCollection tempCollection = new TimeSeriesCollection();
    private final TimeSeries tempData = new TimeSeries("Temperatur");
    private final TimeSeriesCollection humidCollection = new TimeSeriesCollection();
    private final TimeSeries humidData = new TimeSeries("Luftfugtighed");
    private final JFreeChart chart = ChartFactory.createTimeSeriesChart("Temperatur og luftfugtighed", "Tid", "Temperatur (°)", tempCollection);
    private final SwingNode swingNode = new SwingNode();

    public void read() {
        ChartPanel chartPanel = new ChartPanel(chart);
        swingNode.setContent(chartPanel);
        root.getChildren().add(swingNode);
        AnchorPane.setLeftAnchor(swingNode, 16.0);
        AnchorPane.setRightAnchor(swingNode, 16.0);

        tempData.add(new Minute(1, new Hour()), 5);
        tempData.add(new Minute(2, new Hour()), 12);
        humidData.add(new Minute(1, new Hour()), 3);
        humidData.add(new Minute(2, new Hour()), 9);

        tempCollection.addSeries(tempData);
        humidCollection.addSeries(humidData);

        chart.getXYPlot().getRangeAxis(0).setRange(0, 50);
        final XYPlot plot = chart.getXYPlot();
        final NumberAxis numberAxis2 = new NumberAxis("Luftfugtighed (%)");
        numberAxis2.setRange(0, 100);
        plot.setRangeAxis(1, numberAxis2);
        plot.setDataset(1, humidCollection);
        plot.setRenderer(new XYLineAndShapeRenderer());

        final XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        plot.setRenderer(1, renderer2);

        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);

        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.MINUTE, 1));
        axis.setUpperMargin(3);
        axis.setLowerMargin(3);


        final SimpleDateFormat hourFmt = new SimpleDateFormat("HH:mm");
        final SimpleDateFormat datFmt = new SimpleDateFormat("d.MMM");

        axis.setDateFormatOverride(new DateFormat() {
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
                    return datFmt.format(date, toAppendTo, fieldPosition);
                } else {
                    return hourFmt.format(date, toAppendTo, fieldPosition);
                }
            }

            @Override
            public Date parse(String source, ParsePosition pos) {
                return hourFmt.parse(source, pos);
            }
        });

//        new Thread(() -> {
//            while (Platform.isAccessibilityActive()) {
//                try {
//                    Socket socket = new Socket("localhost", 12346);
//
//                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    String data = inputStream.readLine();
//
//                    double temp = Double.parseDouble(data.substring(0, data.indexOf(' ')));
//                    double humid = Double.parseDouble(data.substring(data.indexOf(' ') + 1));
//
//                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
//                    String time = dateFormat.format(new Date());
//
//                    System.out.println("Temperaturen er: " + temp + " grader");
//                    System.out.println("Luftfugtigheden er: " + humid + "%");
//                    System.out.println(time);
//
//                    Platform.runLater(() -> {
//                        tempData.add(new TimeSeriesDataItem(new Minute(), temp));
//                        humidData.add(new TimeSeriesDataItem(new Minute(), humid));
//                    });
//
//                } catch (IOException io) {
//                    io.printStackTrace();
//                    return;
//                }
//            }
//        }).start();
    }

}

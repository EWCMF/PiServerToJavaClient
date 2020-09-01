
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
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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

        // Dummy data kan udkommenteres.
        tempData.add(new Hour(20, new Day(30, 8, 2020)), 12);
        tempData.add(new Hour(21, new Day(30, 8, 2020)), 11);
        tempData.add(new Hour(22, new Day(30, 8, 2020)), 9);
        tempData.add(new Hour(23, new Day(30, 8, 2020)), 8);
        tempData.add(new Hour(0, new Day(31, 8, 2020)), 6);
        tempData.add(new Hour(1, new Day(31, 8, 2020)), 5);
        humidData.add(new Hour(20, new Day(30, 8, 2020)), 33);
        humidData.add(new Hour(21, new Day(30, 8, 2020)), 30);
        humidData.add(new Hour(22, new Day(30, 8, 2020)), 29);
        humidData.add(new Hour(23, new Day(30, 8, 2020)), 27);
        humidData.add(new Hour(0, new Day(31, 8, 2020)), 24);
        humidData.add(new Hour(1, new Day(31, 8, 2020)), 21);

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
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
        axis.setUpperMargin(1);
//        axis.setLowerMargin(1);


        final SimpleDateFormat hourFmt = new SimpleDateFormat("HH:mm");
        final SimpleDateFormat datFmt = new SimpleDateFormat("d. MMMM");

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

        while (true) {
            LocalTime now = LocalTime.now();
            long nextHour = now.until(now.plusHours(1).withMinute(0), ChronoUnit.MINUTES);

            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            service.schedule(new ReadTask(), nextHour, TimeUnit.MINUTES);
        }
    }

    class ReadTask implements Runnable {

        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 12346);

                BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String data = inputStream.readLine();

                double temp = Double.parseDouble(data.substring(0, data.indexOf(' ')));
                double humid = Double.parseDouble(data.substring(data.indexOf(' ') + 1));

                Platform.runLater(() -> {
                    tempData.add(new TimeSeriesDataItem(new Hour(), temp));
                    humidData.add(new TimeSeriesDataItem(new Hour(), humid));
                });

                Thread.sleep(60000);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

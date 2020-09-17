
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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
//        tempData.add(new Hour(20, new Day(30, 8, 2020)), 12);
//        tempData.add(new Hour(21, new Day(30, 8, 2020)), 11);
//        tempData.add(new Hour(22, new Day(30, 8, 2020)), 9);
//        tempData.add(new Hour(23, new Day(30, 8, 2020)), 8);
//        tempData.add(new Hour(0, new Day(31, 8, 2020)), 6);
//        tempData.add(new Hour(1, new Day(31, 8, 2020)), 5);
//        humidData.add(new Hour(20, new Day(30, 8, 2020)), 33);
//        humidData.add(new Hour(21, new Day(30, 8, 2020)), 30);
//        humidData.add(new Hour(22, new Day(30, 8, 2020)), 29);
//        humidData.add(new Hour(23, new Day(30, 8, 2020)), 27);
//        humidData.add(new Hour(0, new Day(31, 8, 2020)), 24);
//        humidData.add(new Hour(1, new Day(31, 8, 2020)), 21);

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
        //axis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.MINUTE, 1));
        axis.setUpperMargin(1);
        axis.setLowerMargin(1);

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

        new Thread(new ReadTask()).start();
    }

    private void scheduleTask() {
        LocalTime now = LocalTime.now();
        //long nextHour = now.until(now.plusHours(1).withMinute(0), ChronoUnit.MINUTES);
        long nextMinute = now.until(now.plusMinutes(1).withSecond(0), ChronoUnit.SECONDS);

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        //service.schedule(new ReadTask(), nextHour, TimeUnit.MINUTES);
        service.schedule(new ReadTask(), nextMinute, TimeUnit.SECONDS);
    }

    class ReadTask implements Runnable {

        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 12346);

                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                byte[] bytes = inputStream.readAllBytes();
                String ct = new String(bytes, StandardCharsets.UTF_8);

                System.out.println("Encrypted text received:");
                System.out.println(ct);

                String code = "franskhotdog1234";
                SecretKey key = new SecretKeySpec(code.getBytes(), "AES");

                byte[] cipherBytes = Base64.getDecoder().decode(ct);

                byte[] iv = Arrays.copyOf(cipherBytes, 16);
                byte[] textBytes = Arrays.copyOfRange(cipherBytes, 16, cipherBytes.length);

                String data = decrypt(textBytes, key, iv);
                System.out.println("Decrypted text:");
                System.out.println(data);

                double temp = Double.parseDouble(data.substring(0, data.indexOf(' ')));
                double humid = Double.parseDouble(data.substring(data.indexOf(' ') + 1));

                Platform.runLater(() -> {
                    //tempData.add(new TimeSeriesDataItem(new Hour(), temp));
                    //humidData.add(new TimeSeriesDataItem(new Hour(), humid));
                    tempData.add(new TimeSeriesDataItem(new Minute(), temp));
                    humidData.add(new TimeSeriesDataItem(new Minute(), humid));
                });

                //Thread.sleep(60000);
                Thread.sleep(1000);
                scheduleTask();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String decrypt (byte[] cipherText, SecretKey key, byte[] IV) throws Exception
    {
        //Get Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

        //Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

        //Create IvParameterSpec
        IvParameterSpec ivSpec = new IvParameterSpec(IV);

        //Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        //Perform Decryption
        byte[] decryptedText = cipher.doFinal(cipherText);

        return new String(decryptedText);
    }
}

package util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static util.ColoredPrint.printRed;

public class CSVHandler {
    // see: http://opencsv.sourceforge.net/#writing

    public static final String FILE_PATH = "src/main/resources/";

    public static void deleteFile(String fileName) {
        if (new File(FILE_PATH + fileName + ".csv").delete()) {
            printRed("WARNING: " + fileName + ".csv was deleted successfully.");
        }
    }

    public static void createCSV(String fileName, List<String[]> data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", false))) {
            csvw.writeAll(data);
        } catch (IOException ignore) {}
    }

    public static void createCSVWithHeader(String fileName,  String[] header, List<String[]> data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", false))) {
            csvw.writeNext(header);
            csvw.writeAll(data);
        } catch (IOException ignore) {}
    }

    public static void appendCSV(String fileName, String[] data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", true))) {
            csvw.writeNext(data);
        } catch (IOException ignore) {}
    }

    public static void appendCSV(String fileName, String data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", true))) {
            csvw.writeNext(new String[] {data});
        } catch (IOException ignore) {}
    }

    public static int getSize(String fileName) {
        int size = 0;
        try (CSVReader csvr = new CSVReader(new FileReader(FILE_PATH + fileName + ".csv"))) {
            while (csvr.readNext() != null) {
                size++;
            }
        } catch (IOException | CsvValidationException ignore) {}
        return size;
    }
}

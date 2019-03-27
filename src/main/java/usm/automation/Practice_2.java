package usm.automation;

import com.thoughtworks.xstream.XStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Practice_2 {

    private static final String INPUT_DATES_FILE = "./Practice_2.txt";
    private static final String FILE_DATE_FORMAT = "dd.MM.yyyy";
    private static final String REQUEST_DATE_FORMAT = "dd.MM.yyyy";
    private static final String OUTPUT_VALUTES_EXCEL = "./Practice_2.xlsx";

    public static void main(String[] args) throws IOException, ParseException {

        // 1. Read the dates form txt file
        List<Date> dates = getDates(INPUT_DATES_FILE, FILE_DATE_FORMAT);

        //2. Request currency rates for above dates
        //3. Deserialize rates
        Map<Date, Valutes> dateValutesMap = requestValutes(dates);

        // Convert to Excel
        Workbook workbook = toExcelWorkbook(dateValutesMap);

        //4. Write Excel to file
        writeWorkbookToFile(OUTPUT_VALUTES_EXCEL, workbook);
    }

    private static Map<Date, Valutes> requestValutes(List<Date> dates) throws IOException {

        Map<Date, Valutes> dateValutesMap = new LinkedHashMap<>();
        CloseableHttpClient client = HttpClientBuilder.create().build();

        for (Date date : dates) {

            HttpUriRequest request = new HttpGet("https://bnm.md/en/official_exchange_rates?get_xml=1&date=" +
                    dateToString(date, REQUEST_DATE_FORMAT));
            CloseableHttpResponse response = client.execute(request);
            InputStream responseStream = response.getEntity().getContent();

            dateValutesMap.put(date, toValutes(responseStream));

            response.close();
        }

        return dateValutesMap;
    }

    private static Workbook toExcelWorkbook(Map<Date, Valutes> dateValutesMap) {

        Workbook workbook = new XSSFWorkbook();
        String dateFormat = "dd.MM.yyyy";

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setBorderTop(BorderStyle.THICK);
        headerStyle.setBorderBottom(BorderStyle.THICK);
        headerStyle.setBorderLeft(BorderStyle.THICK);
        headerStyle.setBorderRight(BorderStyle.THICK);
        headerStyle.setFont(headerFont);

        for (Map.Entry<Date, Valutes> entry : dateValutesMap.entrySet()) {

            String date = dateToString(entry.getKey(), dateFormat);
            Sheet dateSheet = workbook.createSheet(date);

            Row headerRow = dateSheet.createRow(0);

            createCell(headerRow, 1, "ID", headerStyle);
            createCell(headerRow, 2, "NumCode", headerStyle);
            createCell(headerRow, 3, "CharCode", headerStyle);
            createCell(headerRow, 4, "Nominal", headerStyle);
            createCell(headerRow, 5, "Name", headerStyle);
            createCell(headerRow, 6, "Value", headerStyle);

            Valute[] valutes = entry.getValue().getValutes().toArray(new Valute[entry.getValue().getValutes().size()]);

            for (int i = 0; i < valutes.length; i++) {

                Row valuteRow = dateSheet.createRow(i + 1);
                Valute valute = valutes[i];
                createCell(valuteRow, 1, String.valueOf(valute.getId()));
                createCell(valuteRow, 2, String.valueOf(valute.getNumCode()));
                createCell(valuteRow, 3, String.valueOf(valute.getCharCode()));
                createCell(valuteRow, 4, String.valueOf(valute.getNominal()));
                createCell(valuteRow, 5, String.valueOf(valute.getName()));
                createCell(valuteRow, 6, String.valueOf(valute.getValue()));
            }

            dateSheet.autoSizeColumn(1);
            dateSheet.autoSizeColumn(2);
            dateSheet.autoSizeColumn(3);
            dateSheet.autoSizeColumn(4);
            dateSheet.autoSizeColumn(5);
            dateSheet.autoSizeColumn(6);
        }

        return workbook;
    }

    private static void writeWorkbookToFile(String outputFile, Workbook workbook) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            workbook.write(outputStream);
        }
    }

    private static void createCell(Row row, int column, String value) {
        createCell(row, column, value, null);
    }

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);

        if (style != null)
            cell.setCellStyle(style);
    }

    private static Valutes toValutes(InputStream inputStream) {

        XStream xstream = new XStream();
        xstream.processAnnotations(Valutes.class);

        return (Valutes) xstream.fromXML(inputStream);
    }

    private static List<Date> getDates(String inputFile, String dateFormat) throws IOException {

        // I know it's not perfect :))))
        return getFileContent(inputFile).stream()
                .map(line -> stringToDate(line, dateFormat))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static List<String> getFileContent(final String fileName) throws IOException {
        return Files.readAllLines(Paths.get(fileName));
    }

    private static Optional<Date> stringToDate(final String date, final String dateFormat) {
        try {
            return Optional.of(new SimpleDateFormat(dateFormat).parse(date));
        } catch (ParseException pe) {
            System.err.println("Failed to parse date " + date);
            return Optional.empty();
        }
    }

    private static String dateToString(final Date date, final String format) {
        return new SimpleDateFormat(format).format(date);
    }
}

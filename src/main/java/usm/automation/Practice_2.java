package usm.automation;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

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

    public static void main(String[] args) throws IOException, ParseException, JDOMException {

        // 1. Read the dates form txt file
        List<Date> dates = getDates(INPUT_DATES_FILE, FILE_DATE_FORMAT);

        //2. Request currency rates for above dates
        //3. Deserialize rates
        Map<Date, List<Valute>> dateValutesMap = requestValutes(dates);

        // Convert to Excel
        Workbook workbook = toExcelWorkbook(dateValutesMap);

        //4. Write Excel to file
        writeWorkbookToFile(OUTPUT_VALUTES_EXCEL, workbook);
    }

    private static Map<Date, List<Valute>> requestValutes(List<Date> dates) throws JDOMException, IOException {

        Map<Date, List<Valute>> dateValutesMap = new LinkedHashMap<>();
        CloseableHttpClient client = HttpClientBuilder.create().build();

        for (Date date : dates) {

            HttpUriRequest request = new HttpGet("https://bnm.md/en/official_exchange_rates?get_xml=1&date=" +
                    dateToString(date, REQUEST_DATE_FORMAT));
            CloseableHttpResponse response = client.execute(request);
            InputStream responseStream = response.getEntity().getContent();

            dateValutesMap.put(date, toValuteList(responseStream));

            response.close();
        }

        return dateValutesMap;
    }

    private static Workbook toExcelWorkbook(Map<Date, List<Valute>> dateValutesMap) throws IOException {

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

        for (Map.Entry<Date, List<Valute>> entry : dateValutesMap.entrySet()) {

            Sheet dateSheet = workbook.createSheet(dateToString(entry.getKey(), dateFormat));

            Row headerRow = dateSheet.createRow(0);

            createCell(headerRow, 1, "ID", headerStyle);
            createCell(headerRow, 2, "NumCode", headerStyle);
            createCell(headerRow, 3, "CharCode", headerStyle);
            createCell(headerRow, 4, "Nominal", headerStyle);
            createCell(headerRow, 5, "Name", headerStyle);
            createCell(headerRow, 6, "Value", headerStyle);

            List<Valute> valutes = entry.getValue();

            for (int i = 0; i < valutes.size(); i++) {

                Row valuteRow = dateSheet.createRow(i + 1);
                Valute valute = valutes.get(i);
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

    private static List<Valute> toValuteList(InputStream inputStream) throws JDOMException, IOException {
        return new SAXBuilder().build(inputStream).getRootElement().getChildren().stream()
                .map((element) -> new Valute(
                        Long.parseLong(element.getAttributeValue("ID")),
                        Integer.parseInt(element.getChildText("NumCode")),
                        element.getChildText("CharCode"),
                        Integer.parseInt(element.getChildText("Nominal")),
                        element.getChildText("Name"),
                        Double.parseDouble(element.getChildText("Value"))))
                .collect(Collectors.toList());
    }

//    private static CloseableHttpResponse requestBnmRate(Date date) {
//
//        String protocol = "https";
//        String host = "bnm.md";
//        String resource = "/en/official_exchange_rates";
//
//        List<NameValuePair> params = new ArrayList<>();
//        params.add(new BasicNameValuePair("get_xml", "1"));
//        params.add(new BasicNameValuePair("date", dateToString(date, REQUEST_DATE_FORMAT)));
//
//        HttpGet request = new HttpGet(String.format("%s://%s%s", protocol, host, resource));
//        new HttpPost().setEntity
//        CloseableHttpResponse response = client.execute(request);
//
//    }

    private static List<Date> getDates(String inputFile, String dateFormat) throws IOException, ParseException {

        List<String> fileContent = getFileContent(inputFile);
        List<Date> dates = new ArrayList<>();

        for (String line : fileContent)
            dates.add(stringToDate(line, dateFormat));

        return dates;
    }

    private static List<String> getFileContent(final String fileName) throws IOException {
        return Files.readAllLines(Paths.get(fileName));
    }

    private static Date stringToDate(final String date, final String dateFormat) throws ParseException {
        return new SimpleDateFormat(dateFormat).parse(date);
    }

    private static String dateToString(final Date date, final String format) {
        return new SimpleDateFormat(format).format(date);
    }
}

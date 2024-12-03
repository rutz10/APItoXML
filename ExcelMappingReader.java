import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelMappingReader {

    public static class XmlMapping {
        private String group;
        private String apiFieldName;
        private String apiDataType;
        private String xmlElementName;
        private String xmlDataType;
        private String xmlPath;

        // Constructor
        public XmlMapping(String group, String apiFieldName, String apiDataType, String xmlElementName, String xmlDataType, String xmlPath) {
            this.group = group;
            this.apiFieldName = apiFieldName;
            this.apiDataType = apiDataType;
            this.xmlElementName = xmlElementName;
            this.xmlDataType = xmlDataType;
            this.xmlPath = xmlPath;
        }

        // Getters
        public String getGroup() {
            return group;
        }

        public String getApiFieldName() {
            return apiFieldName;
        }

        public String getApiDataType() {
            return apiDataType;
        }

        public String getXmlElementName() {
            return xmlElementName;
        }

        public String getXmlDataType() {
            return xmlDataType;
        }

        public String getXmlPath() {
            return xmlPath;
        }
    }

    /**
     * Reads the Excel file and returns a list of XmlMapping objects.
     *
     * @param excelFilePath Path to the Excel file.
     * @return List of XmlMapping.
     * @throws IOException If an I/O error occurs.
     */
    public static List<XmlMapping> readMappings(String excelFilePath) throws IOException {
        List<XmlMapping> mappings = new ArrayList<>();
        FileInputStream fis = new FileInputStream(new File(excelFilePath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

        // Iterate over rows, skipping the header
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from row 1 to skip header
            Row row = sheet.getRow(i);
            if (row == null) continue; // Skip empty rows

            String group = getCellValueAsString(row.getCell(0));
            String apiFieldName = getCellValueAsString(row.getCell(1));
            String apiDataType = getCellValueAsString(row.getCell(2));
            String xmlElementName = getCellValueAsString(row.getCell(3));
            String xmlDataType = getCellValueAsString(row.getCell(4));
            String xmlPath = getCellValueAsString(row.getCell(5));

            if (apiFieldName.isEmpty()) continue; // Skip if API Field Name is empty

            mappings.add(new XmlMapping(group, apiFieldName, apiDataType, xmlElementName, xmlDataType, xmlPath));
        }

        workbook.close();
        fis.close();
        return mappings;
    }

    /**
     * Helper method to get cell value as String.
     *
     * @param cell The cell to read.
     * @return String representation of the cell value.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // Customize date format as needed
                } else {
                    double num = cell.getNumericCellValue();
                    if (num == (long) num)
                        return String.valueOf((long) num);
                    else
                        return String.valueOf(num);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }
}

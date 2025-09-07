package com.example.demo.Utils;

import com.example.demo.Enum.UserRole;
import com.example.demo.Model.Users;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.batch.item.ItemReader;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelStreamingReader implements ItemReader<Users> {

    private final List<Users> buffer = new ArrayList<>();
    private int currentIndex = 0;

    public ExcelStreamingReader(InputStream inputStream) throws Exception {
        OPCPackage pkg = OPCPackage.open(inputStream);
        XSSFReader reader = new XSSFReader(pkg);
        ReadOnlySharedStringsTable sharedStringsTable = new ReadOnlySharedStringsTable(pkg);
        StylesTable stylesTable = reader.getStylesTable();

        XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
        if (!sheets.hasNext()) {
            throw new IllegalArgumentException("Excel file không có sheet nào");
        }
        InputStream sheetInputStream = sheets.next();
        System.out.println("=====================Reading sheet: " + sheets.getSheetName());

        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true); // QUAN TRỌNG
        XMLReader parser = saxFactory.newSAXParser().getXMLReader();

        XSSFSheetXMLHandler.SheetContentsHandler handler = new XSSFSheetXMLHandler.SheetContentsHandler() {
            private Map<Integer, String> currentRowValues = new LinkedHashMap<>();
            private Map<Integer, String> headerIndexMap = new LinkedHashMap<>();
            private boolean firstRowSkipped = false;

            @Override
            public void startRow(int rowNum) {
                System.out.println("======================= Start row " + rowNum);
                currentRowValues.clear();
            }

            @Override
            public void endRow(int rowNum) {
                System.out.println("======= End row " + rowNum + " -> values = " + currentRowValues);
                if (!firstRowSkipped) {
                    for (Map.Entry<Integer, String> entry : currentRowValues.entrySet()) {
                        String header = entry.getValue();
                        headerIndexMap.put(entry.getKey(), header.toLowerCase().trim());
                        System.out.println("=====================Header[" + entry.getKey() + "] = " + header);
                    }
                    firstRowSkipped = true;
                    return;
                }

                Map<String, String> rowMap = new HashMap<>();
                for (Map.Entry<Integer, String> entry : currentRowValues.entrySet()) {
                    String header = headerIndexMap.get(entry.getKey());
                    if (header != null && !"id".equalsIgnoreCase(header)) {
                        rowMap.put(header, entry.getValue());
                    }
                }

                System.out.println("Row map: " + rowMap);
                Users user = mapRowToUser(rowMap);
                if (user != null) {
                    buffer.add(user);
                }
            }

            @Override
            public void cell(String cellReference, String formattedValue,
                    org.apache.poi.xssf.usermodel.XSSFComment comment) {
                System.out.println("===============Cell " + cellReference + " = '" + formattedValue + "'");
                int colIndex = new CellReference(cellReference).getCol();
                currentRowValues.put(colIndex, formattedValue);
            }

            @Override
            public void headerFooter(String text, boolean isHeader, String tagName) {
            }
        };
        DataFormatter formatter = new DataFormatter();
        parser.setContentHandler(
                new XSSFSheetXMLHandler(stylesTable, null, sharedStringsTable, handler, formatter, false));
        parser.parse(new InputSource(sheetInputStream));
        System.out.println(">>>>> Total users parsed = " + buffer.size());
        sheetInputStream.close();
        pkg.close();
    }

    private UserRole parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank())
            return null;
        roleStr = roleStr.trim();
        for (UserRole r : UserRole.values()) {
            if (r.name().equalsIgnoreCase(roleStr))
                return r;
        }
        return null;
    }

    private Users mapRowToUser(Map<String, String> row) {
        try {
            Users user = new Users();
            user.setEmail(row.get("email"));
            user.setPassword(row.get("password"));
            user.setFullname(row.get("fullname"));
            user.setRole(parseRole(row.get("role")));
            return user;
        } catch (Exception e) {
            return null; // bỏ dòng lỗi
        }
    }

    @Override
    public Users read() {
        if (currentIndex < buffer.size()) {
            return buffer.get(currentIndex++);
        }
        return null; // hết dữ liệu
    }
}

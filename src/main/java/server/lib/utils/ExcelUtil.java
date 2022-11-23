package server.lib.utils;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import server.lib.interfacing.ExcelColumn;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ExcelUtil {

    public interface WriteCallBack {
        void onWrite(XSSFSheet sheet) throws Throwable;
    }

    public static void write(ArrayList<String> strings, ArrayList<HashMap<String, String>> hashMaps, File filePath, String sheetName) {
        write(new WriteCallBack() {
            @Override
            public void onWrite(XSSFSheet sheet) throws Throwable {
                if (hashMaps.size() > 0) {
                    int counter = 0;
                    XSSFRow row = sheet.createRow(counter);
                    for (String header : strings) {
                        XSSFCell cell = row.createCell(strings.indexOf(header));
                        cell.setCellValue(header);
                    }
                    counter++;
                    for (HashMap<String, String> object : hashMaps) {
                        row = sheet.createRow(counter);
                        for (String header : strings) {
                            XSSFCell cell = row.createCell(strings.indexOf(header));
                            cell.setCellValue(object.get(header));
                        }
                        counter++;
                    }
                }
            }
        }, sheetName, filePath);
    }

    public static <T> void write(Class<T> classType, ArrayList<T> list, File filePath, String sheetName) throws Throwable {
        write(new WriteCallBack() {
            @Override
            public void onWrite(XSSFSheet sheet) throws Throwable {
                if (list.size() > 0) {
                    int counter = 0;
                    XSSFRow row = sheet.createRow(counter);
                    for (DataSet set : compile(list.get(counter), classType)) {
                        XSSFCell cell = row.createCell(set.index);
                        cell.setCellValue(set.name);
                    }
                    counter++;
                    for (T object : list) {
                        row = sheet.createRow(counter);
                        for (DataSet set : compile(object, classType)) {
                            XSSFCell cell = row.createCell(set.index);
                            cell.setCellValue(set.value != null ? set.value : "");
                        }
                        counter++;
                    }
                }
            }
        }, sheetName, filePath);
    }

    private static void write(WriteCallBack callBack, String sheetName, File filePath) {
        FileOutputStream fileOut = null;
        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(sheetName);
            callBack.onWrite(sheet);
            fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static <T> ArrayList<DataSet> compile(T object, Class<T> classType) throws Throwable {
        ArrayList<DataSet> dataSets = new ArrayList<>();
        for (Field declaredField : classType.getDeclaredFields()) {
            if (declaredField != null && declaredField.getAnnotation(ExcelColumn.class) != null) {
                ExcelColumn annotation = declaredField.getAnnotation(ExcelColumn.class);
                DataSet dataSet = new ExcelUtil().new DataSet();
                dataSet.index = annotation.index();
                dataSet.name = annotation.value();
                declaredField.setAccessible(true);
                Object val = declaredField.get(object);
                dataSet.value = val == null ? null : String.valueOf(val);
                declaredField.setAccessible(false);
                dataSets.add(dataSet);
            }
        }
        Collections.sort(dataSets, new Comparator<DataSet>() {
            @Override
            public int compare(DataSet o1, DataSet o2) {
                return Double.compare(o1.index, o2.index);
            }
        });
        return dataSets;
    }

    public class DataSet {
        int index;
        String value;
        String name;
    }
}

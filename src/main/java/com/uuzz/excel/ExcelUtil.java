package com.uuzz.excel;


import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author zj
 * @desc excel导出工具
 * @date 2017/11/8
 * @since 1.7
 */
public class ExcelUtil<T> implements Serializable {


    /**
     * 每个sheet页显示数据条数
     */
    public final static int sheetSize = 500;
    /**
     * 一次性在内存中加载的最大数据，超过该数据则缓存至硬盘
     */
    public final static int diskSize = 100;

    /**
     * 获取相应的类
     */
    private Class<T> clazz;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * @param resultList   将写入EXCEL的数据
     * @param sheetName    工作表名字
     * @param outputStream 输出流
     * @return
     */
    public boolean writeExcelFromList(List<T> resultList, String sheetName, OutputStream outputStream) {
        //返回标示
        Boolean sign = Boolean.FALSE;
        try {
            // 得到所有定义字段
            Field[] allFields = clazz.getDeclaredFields();
            List<Field> fields = new ArrayList<Field>();
            // 得到所有field并存放到一个list中
            for (Field field : allFields) {
                if (field.isAnnotationPresent(ExcelAttribute.class)) {
                    fields.add(field);
                }
            }
            // 产生工作薄对象
            Workbook workbook = new SXSSFWorkbook(diskSize);

            //数据源数量
            int listSize = 0;
            if (resultList != null && resultList.size() >= 0) {
                listSize = resultList.size();
            }
            //工作簿页数
            double sheetNo = Math.ceil(listSize / sheetSize);

            for (int i = 0; i <= sheetNo; i++) {
                //创建工作簿
                Sheet sheet = workbook.createSheet();
                //设置工作表的名称
                workbook.setSheetName(i, sheetName + "" + i);
                //创建标题
                createTitleCell(fields, i, sheet);
                //创建内容列
                creatDataCell(resultList, fields, listSize, i, sheet);
            }

            outputStream.flush();
            workbook.write(outputStream);
            outputStream.close();
            sign = Boolean.TRUE;
        } catch (Exception e) {
            System.out.println("Excel writeExcelFromList Exception" + e);
        } finally {
            return sign;
        }
    }

    /**
     * 创建数据单元格
     *
     * @param resultList
     * @param fields
     * @param listSize
     * @param sheetNo
     * @param sheet
     * @throws IllegalAccessException
     * @throws ParseException
     */
    private void creatDataCell(List<T> resultList, List<Field> fields, int listSize, int sheetNo, Sheet sheet) throws IllegalAccessException, ParseException {

        int startNo = sheetNo * sheetSize;
        int endNo = Math.min(startNo + sheetSize, listSize);
        for (int j = startNo; j < endNo; j++) {
            Row row = sheet.createRow(j + 1 - startNo);
            // 得到导出对象.
            T vo = (T) resultList.get(j);
            for (int k = 0; k < fields.size(); k++) {
                // 获得field
                Field field = fields.get(k);
                // 设置实体类私有属性可访问
                field.setAccessible(true);
                ExcelAttribute attr = field.getAnnotation(ExcelAttribute.class);
                int col = k;
                // 根据指定的顺序获得列号
                if (!StringUtils.isEmpty(attr.column())) {
                    col = getExcelCol(attr.column());
                }

                Cell cell = row.createCell(col);
                // 如果数据存在就填入,不存在填入空格
                Class<?> classType = field.getType();
                String value = null;
                if (field.get(vo) != null && classType.isAssignableFrom(Date.class)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    value = sdf.format(sdf.parse(String.valueOf(field.get(vo))));
                }
                cell.setCellValue(field.get(vo) == null ? "" : value == null ? String.valueOf(field.get(vo)) : value);

            }
        }
    }

    /**
     * 创建标题单元格
     *
     * @param fields
     * @param sheetNo
     * @param sheet
     */
    private void createTitleCell(List<Field> fields, int sheetNo, Sheet sheet) {

        Row row = sheet.createRow(0);
        for (int cellNum = 0; cellNum < fields.size(); cellNum++) {
            //
            Field field = fields.get(cellNum);
            //获取注解信息
            ExcelAttribute attr = field.getAnnotation(ExcelAttribute.class);
            int col = cellNum;
            // 根据指定的顺序获得列号
            if (!StringUtils.isEmpty(attr.column())) {
                col = getExcelCol(attr.column());
            }
            // 创建列
            Cell cell = row.createCell(col);

            sheet.setColumnWidth(sheetNo, (int) ((attr.name().getBytes().length <= 4 ? 6 : attr.name().getBytes().length) * 1.5 * 256));

            // 设置列中写入内容为String类型
            cell.setCellType(Cell.CELL_TYPE_STRING);
            // 写入列名
            cell.setCellValue(attr.name());

            // 如果设置了提示信息则鼠标放上去提示.
            if (!StringUtils.isEmpty(attr.prompt())) {
                setHSSFPrompt(sheet, "", attr.prompt(), 1, 100, col, col);
            }
            // 如果设置了combo属性则本列只能选择不能输入
            if (attr.combo().length > 0) {
                setHSSFValidation(sheet, attr.combo(), 1, 100, col, col);
            }

        }
    }


    /**
     * 将EXCEL中A,B,C,D,E列映射成0,1,2,3
     *
     * @param col
     */
    public static int getExcelCol(String col) {
        col = col.toUpperCase();
        // 从-1开始计算,字母重1开始运算。这种总数下来算数正好相同。
        int count = -1;
        char[] cs = col.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            count += (cs[i] - 64) * Math.pow(26, cs.length - 1 - i);
        }
        return count;
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet    要设置的sheet.
     * @param textlist 下拉框显示的内容
     * @param firstRow 开始行
     * @param endRow   结束行
     * @param firstCol 开始列
     * @param endCol   结束列
     * @return 设置好的sheet.
     */
    public static Sheet setHSSFValidation(Sheet sheet,
                                          String[] textlist, int firstRow, int endRow, int firstCol,
                                          int endCol) {
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint.createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_list = new HSSFDataValidation(regions, constraint);
        sheet.addValidationData(data_validation_list);
        return sheet;
    }

    /**
     * 设置单元格上提示
     *
     * @param sheet         要设置的sheet.
     * @param promptTitle   标题
     * @param promptContent 内容
     * @param firstRow      开始行
     * @param endRow        结束行
     * @param firstCol      开始列
     * @param endCol        结束列
     * @return 设置好的sheet.
     */
    public static Sheet setHSSFPrompt(Sheet sheet, String promptTitle,
                                      String promptContent, int firstRow, int endRow, int firstCol, int endCol) {
        // 构造constraint对象
        DVConstraint constraint = DVConstraint.createCustomFormulaConstraint("BB1");
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_view = new HSSFDataValidation(regions, constraint);
        data_validation_view.createPromptBox(promptTitle, promptContent);
        sheet.addValidationData(data_validation_view);
        return sheet;
    }
}
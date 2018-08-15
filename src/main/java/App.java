import com.uuzz.Debit;
import com.uuzz.excel.ExcelUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * 程序启动入口
 * 扫描订单图片导出发票信息
 *
 * @author zj
 * @time 2018-8-14
 */
public class App {

    public static void main(String[] args) throws FileNotFoundException {

        //人机交互
        System.out.println("请输入存放订单html页面绝对路径，然后回车");
        Scanner s = new Scanner(System.in);
        File htmlDir = new File(s.nextLine());
        String reportFile;
        String excelName = "发票汇总.xls";
        List<File> htmlS = new ArrayList();
        if (htmlDir.isDirectory()) {
            htmlS = Arrays.asList(htmlDir.listFiles());
            reportFile = htmlDir.getAbsolutePath() + File.separator + excelName;
        } else {
            htmlS.add(htmlDir);
            reportFile = htmlDir.getParentFile().getAbsolutePath() + File.separator + excelName;
        }
        //解析页面识别发票信息
        List<Debit> debits = loadFromHtml(htmlS);
        //导出excel
        reportExcel(debits, reportFile);
        System.out.println("发票已汇总完毕，请在订单页面的目录中查看文件”发票汇总.xls“");
    }

    /**
     * 解析html内容封装发票信息
     *
     * @param htmlS
     * @return
     */
    private static List<Debit> loadFromHtml(List<File> htmlS) {

        List<Debit> debits = new ArrayList<>();
        for (File html : htmlS) {
            debits.add(Debit.parse(html));
        }
        return debits;
    }

    /**
     * 导出excel
     */
    private static void reportExcel(List<Debit> data, String reportFile) throws FileNotFoundException {

        try {
            ExcelUtil<Debit> excelUtil = new ExcelUtil(Debit.class);

            OutputStream out = new FileOutputStream(new File(reportFile));

            String sheetName = "发票信息表";

            excelUtil.writeExcelFromList(data, sheetName, out);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}

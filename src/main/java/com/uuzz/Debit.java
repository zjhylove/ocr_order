package com.uuzz;

import com.uuzz.excel.ExcelAttribute;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ResourceBundle;

/**
 * 发票信息
 *
 * @author zj
 * @time 2018-8-14
 */
public class Debit {

    @ExcelAttribute(name = "邮箱", isExport = true)
    private String email;

    @ExcelAttribute(name = "地址", isExport = true)
    private String address;

    @ExcelAttribute(name = "名称", isExport = true)
    private String company;

    @ExcelAttribute(name = "纳税人识别号", isExport = true)
    private String taxNumber;

    @ExcelAttribute(name = "订单号", isExport = true)
    private String orderNumber;

    @ExcelAttribute(name = "商品型号", isExport = true)
    private String productName;

    @ExcelAttribute(name = "数量", isExport = true)
    private String num;

    @ExcelAttribute(name = "订单金额", isExport = true)
    private String money;

    @ExcelAttribute(name = "收件人", isExport = true)
    private String reciever;


    public static Debit parse(File html) {

        try {
            Debit debit = new Debit();
            Document doc = Jsoup.parse(html, "UTF-8", StringUtils.EMPTY);
            Elements orderElem = doc.select("span.state-fs18");
            String order = orderElem.get(0).text();
            debit.setOrderNumber(StringUtils.trim(order));

            //发票信息
            Elements debitInfo = doc.select("td.pubwhite");
            for (Element di : debitInfo) {
                if (StringUtils.equals(di.text().trim(), "发票抬头:")) {
                    debit.setCompany(di.nextElementSibling().text().trim());
                }
                if (StringUtils.equals(di.text().trim(), "纳税人识别号:")) {
                    debit.setTaxNumber(di.nextElementSibling().text().trim());
                }
                if (StringUtils.equals(di.text().trim(), "收货人:")) {
                    debit.setReciever(di.nextElementSibling().text().trim());
                }
                if (StringUtils.equals(di.text().trim(), "地址:")) {
                    debit.setAddress(di.nextElementSibling().text().trim());
                }
            }

            //商品信息
            Element goodTbl = doc.getElementsByClass("mtb20").get(0);
            Elements goods = goodTbl.getElementsByTag("tbody").get(0).getElementsByTag("tr");
            for (Element g : goods) {
                String goodName = g.child(1).getElementsByTag("a").text().replace("[商品快照]", "");
                goodName += getGoodsNumber(goodName);
                debit.setProductName(debit.getProductName() + "\r\n" + goodName);
                String money = g.child(2).text();
                String num = g.child(6).text();
                debit.setNum(debit.getNum() + "\r\n" + money + "*" + num);
            }
            //订单金额
            Element foncol = doc.getElementsByClass("oderamo-col").get(0);
            String momey = foncol.childNode(2).outerHtml();
            debit.setMoney(momey);
            //发票收件人邮件地址
            debit.setEmail(html.getName().replace(".html", ""));
            return debit;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getGoodsNumber(String goodsName) {
        try {
            ResourceBundle goodsProp = ResourceBundle.getBundle("goods");
            return goodsProp.getString(goodsName.replaceAll("\\s*", ""));
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getProductName() {
        return productName == null ? StringUtils.EMPTY : productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getNum() {
        return num == null ? StringUtils.EMPTY : num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getReciever() {
        return reciever == null ? StringUtils.EMPTY : reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    @Override
    public String toString() {
        return new JSONObject(this).toString();
    }
}

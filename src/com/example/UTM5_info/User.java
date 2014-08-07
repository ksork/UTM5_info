package com.example.UTM5_info;

import android.os.AsyncTask;
import android.util.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by k on 25.07.14.
 */
public class User {
    private final String LOG_TAG = "User_log";
    private final String IMG_CHECKED = String.valueOf(R.drawable.galochka64x64);
    private String userLogin;
    private String userPassword;
    private Document mainHtml;

    // Конструктор
    public User(String userLogin, String userPassword) {
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        this.refreshMainHtml();
    }

    // Логинимся на сайт, получаем главную старничку кабинета
    public void refreshMainHtml() {
        mainHtml = Net.getMainPage(userLogin, userPassword);
    }

    // Проверка коректности логина/пароля
    public boolean isLoginOk() {
        return (mainHtml != null);
    }

    // Лицевой счет
    public String getAccountId() {
        return grabTable(mainHtml, 0, 1);
    }

    // Баланс
    public String getBalance() {
        String[] data = grabTable(mainHtml, 9, 1).split(" ");
        float num = Float.parseFloat(data[0]);
        int in = (int) (num + 0.5f);
        return String.valueOf(in);
    }

    // Стоимость суток
    public String getDayPrice() {
        return grabTable(mainHtml, 14, 5);
    }

    // Остаток дней
    public String getDaysLeft() {
        float balance = Float.parseFloat(getBalance());
        int price = Integer.parseInt(getDayPrice());
        int daysLeft = (int) (balance + 0.5) / price;
        return String.valueOf(daysLeft);
    }

    // Текущий тариф
    public String getCurrentTariffName() {
        return grabTable(mainHtml, 8, 0);
    }

    // Следующий тариф
    public String getNextTariffName() {
        return grabTable(mainHtml, 8, 1);
    }

    // Берем кредит если возможно, или возвращаем дату последнего кредита
    public String addCredit() {
        Map<String, String> addCreditData = new HashMap<String, String>();
        addCreditData.put("credit_sum", "500");
        addCreditData.put("accepted", "1");
        Document creditPossibility = Net.getRequest("user/promise-payment/");
        // Форма отсутствует, возвращаем дату последнего платежа
        if (creditPossibility.select("form").isEmpty()){
            return grabTable(creditPossibility, 0, 1).split(" ")[0];
        }
        Net.postRequest("user/promise-payment/", addCreditData);
        Net.getRequest("user/change-status/int_status/1/");
        return null;
    }

    // Список доступных тарифов
    public ArrayList<HashMap<String, String>> getTariffList() {
        ArrayList<HashMap<String, String>> tariffList = new ArrayList<HashMap<String, String>>();
        Document tariffHtml = Net.getRequest("user/change-tariff/");
        Elements tariffLabels = tariffHtml.select("label");
        Elements tariffData = tariffHtml.select("table");

        for (int i = 0; i < tariffLabels.size() - 1; i++) {
            HashMap<String, String> tariff = new HashMap<String, String>();
            String tariffName = tariffLabels.get(i).select("span").text();
            String tariffDescription = tariffLabels.get(i).text().split(" ", 4)[3];
            Log.d(LOG_TAG, tariffLabels.get(i).text());
            String tariffPrice = tariffData.get(i + 1).select("td").get(3).text();
            tariff.put("tariffName", tariffName);
            tariff.put("tariffDescription", tariffPrice + " " + tariffDescription);

            if (tariffName.equals(getCurrentTariffName())) {
                tariff.put("tariffEnable", IMG_CHECKED);
            } else {
                tariff.put("tariffEnable", null);
            }
            tariffList.add(tariff);
        }
        return tariffList;
    }

    // Смена тарифа
    public void changeTariff(String nextTariffId) {
        Map<String, String> changeTariffData = new HashMap<String, String>();
        changeTariffData.put("next_tp", nextTariffId);
        changeTariffData.put("accepted", "1");
        Net.postRequest("user/change-tariff/", changeTariffData);
    }

    // Парсер таблиц
    private String grabTable(Document html, int row, int column) {
        Element rowData = html.select("tr").get(row);
        Element columnData = rowData.select("td").get(column);
        return columnData.text();
    }
}
